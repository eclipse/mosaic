/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contact: mosaic@fokus.fraunhofer.de
 */

package org.eclipse.mosaic.lib.routing.pt;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.routing.config.CPublicTransportRouting;

import com.google.common.collect.Iterables;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.ResponsePath;
import com.graphhopper.Trip;
import com.graphhopper.config.Profile;
import com.graphhopper.gtfs.GraphHopperGtfs;
import com.graphhopper.gtfs.PtRouter;
import com.graphhopper.gtfs.PtRouterImpl;
import com.graphhopper.gtfs.Request;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.TranslationMap;
import org.apache.commons.lang3.Validate;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PtRouting {

    private static final Logger LOG = LoggerFactory.getLogger(PtRouting.class);

    private final ExecutorService routingExecution = Executors.newSingleThreadExecutor();

    private PtRouter ptRouter;
    private GraphHopperGtfs graphHopperGtfs;

    private LocalDateTime scheduleDateTime;
    private ZoneId timeZone;

    /**
     * Initializes the pt routing if it is enabled in the provided {@link CPublicTransportRouting}.
     * All paths defined in the provided config are expcted to be relative to the provided configuration
     * location.
     */
    public void initialize(CPublicTransportRouting routingConfiguration, File configurationLocation) {
        if (!routingConfiguration.enabled) {
            return;
        }

        scheduleDateTime = LocalDateTime.parse(routingConfiguration.scheduleDateTime, DateTimeFormatter.ISO_DATE_TIME);
        timeZone = ZoneId.of(ZoneId.SHORT_IDS.get(routingConfiguration.timeZone));

        final Path baseDirectory = configurationLocation.toPath();

        GraphHopperConfig ghConfig = new GraphHopperConfig()
                .putObject("import.osm.ignored_highways", "motorway,trunk,primary")
                .putObject("graph.location", baseDirectory.resolve("ptgraph").toAbsolutePath().toString())
                .putObject("datareader.file", baseDirectory.resolve(routingConfiguration.osmFile).toAbsolutePath().toString())
                .putObject("gtfs.file", baseDirectory.resolve(routingConfiguration.gtfsFile).toAbsolutePath().toString())
                .setProfiles(Collections.singletonList(new Profile("foot").setVehicle("foot")));

        final StopWatch sw = new StopWatch();
        sw.start();
        graphHopperGtfs = new GraphHopperGtfs(ghConfig);
        graphHopperGtfs.init(ghConfig);
        graphHopperGtfs.importOrLoad();
        sw.stop();
        LOG.debug("Took {} ms to load public transport router.", sw.getMillis());

        LOG.info("setting ptRouter");
        ptRouter = new PtRouterImpl.Factory(ghConfig,
                new TranslationMap().doImport(),
                graphHopperGtfs.getBaseGraph(),
                graphHopperGtfs.getEncodingManager(),
                graphHopperGtfs.getLocationIndex(),
                graphHopperGtfs.getGtfsStorage()
        ).createWithoutRealtimeFeed();
    }

    /**
     * Calculates a public transport route according to the given request.
     * The request must contain a valid start and target position, as well as valid request time.
     */
    public PtRoutingResponse findPtRoute(PtRoutingRequest request) {
        if (ptRouter == null) {
            throw new IllegalStateException("PT Routing is not available. Must be enabled in application_config.json.");
        }
        Validate.notNull(request.getStartingGeoPoint(), "Starting point must not be null.");
        Validate.notNull(request.getTargetGeoPoint(), "Target point must not be null.");
        Validate.isTrue(request.getRequestTime() >= 0, "Invalid request time.");
        Validate.isTrue(request.getRoutingParameters().getWalkingSpeedMps() > 0, "Walking speed must be greater than 0.");

        Instant departureTime = toScheduleTime(request.getRequestTime());

        final Request ghRequest = new Request(
                request.getStartingGeoPoint().getLatitude(),
                request.getStartingGeoPoint().getLongitude(),
                request.getTargetGeoPoint().getLatitude(),
                request.getTargetGeoPoint().getLongitude()
        );
        // ghRequest.setBlockedRouteTypes(request.getRoutingParameters().excludedPtModes);//FIXME generalize this
        ghRequest.setEarliestDepartureTime(departureTime);
        ghRequest.setWalkSpeedKmH(request.getRoutingParameters().getWalkingSpeedMps() * 3.6);

        final Future<GHResponse> responseFuture = routingExecution.submit(() -> ptRouter.route(ghRequest));
        final GHResponse route;
        try {
            final StopWatch sw = new StopWatch();
            sw.start();
            route = responseFuture.get(30, TimeUnit.SECONDS);
            sw.stop();
            LOG.debug("Took {} ms to calculate public transport route.", sw.getMillis());
        } catch (TimeoutException e) {
            responseFuture.cancel(true);
            throw new RuntimeException("Could not finish route calculation. Exceeded timeout.");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Could not finish route calculation. Exceeded timeout.", e);
        }

        final List<MultiModalLeg> legs = convertToMultiModalLegs(route.getBest());

        return new PtRoutingResponse(new MultiModalRoute(legs));
    }

    private List<MultiModalLeg> convertToMultiModalLegs(ResponsePath ghBestRoute) {
        List<MultiModalLeg> legs = new ArrayList<>();
        for (Trip.Leg leg : ghBestRoute.getLegs()) {
            if (leg instanceof Trip.PtLeg ptLeg) {
                List<PtLeg.PtStop> newStops = new ArrayList<>();
                for (Trip.Stop stop : ptLeg.stops) {
                    newStops.add(new PtLeg.PtStop(
                            GeoPoint.lonLat(stop.geometry.getX(), stop.geometry.getY()),
                            fromScheduleTime(stop.departureTime),
                            fromScheduleTime(stop.arrivalTime)
                    ));
                }
                legs.add(new MultiModalLeg(
                        new PtLeg(newStops),
                        newStops.get(0).departureTime(),
                        Iterables.getLast(newStops).arrivalTime()
                ));
            } else if (leg instanceof Trip.WalkLeg walkLeg) {
                List<GeoPoint> waypoints = new ArrayList<>();
                for (Coordinate coordinate : walkLeg.geometry.getCoordinates()) {
                    waypoints.add(GeoPoint.lonLat(coordinate.x, coordinate.y));
                }
                legs.add(new MultiModalLeg(
                        new WalkLeg(waypoints),
                        fromScheduleTime(leg.getDepartureTime()),
                        fromScheduleTime(leg.getArrivalTime())
                ));
            }
        }
        return legs;
    }

    private Instant toScheduleTime(long simTime) {
        return scheduleDateTime.plusNanos(simTime).atZone(timeZone).toInstant();
    }

    private Long fromScheduleTime(@Nullable Date date) {
        if (date == null) {
            return null;
        }
        return fromScheduleTime(date.toInstant());
    }

    private long fromScheduleTime(@Nonnull Instant instant) {
        return scheduleDateTime.until(LocalDateTime.ofInstant(instant, timeZone), ChronoUnit.NANOS);
    }

    public void close() {
        if (graphHopperGtfs != null) {
            graphHopperGtfs.close();
        }
    }
}