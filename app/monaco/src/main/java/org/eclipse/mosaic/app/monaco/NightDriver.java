/*
 * Copyright (c) 2020 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.app.monaco;

import org.eclipse.mosaic.fed.application.ambassador.util.UnitLogger;
import org.eclipse.mosaic.fed.application.ambassador.navigation.RoadPositionFactory;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.Application;
import org.eclipse.mosaic.fed.application.ambassador.navigation.INavigationModule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingResponse;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.util.scheduling.EventProcessor;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoUtils;
import org.eclipse.mosaic.lib.database.spatial.NodeFinder;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.enums.VehicleStopMode;
import org.eclipse.mosaic.lib.routing.database.LazyLoadingNode;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import java.io.File;
import java.util.Collection;
import java.awt.Color;
import java.util.Objects;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NightDriver extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication, CommunicationApplication{


    private final long activityDuration;
    private StopWatch stopWatch;


    private boolean initialTripPlanned = false;
    private boolean returnTripPlanned = false;


    private Database database = Database.loadFromFile("Monaco.db");
    private ArrayList<Node> nodeList = database.getNodes().stream().collect(Collectors.toCollection(ArrayList::new));
    // List<String> borderNodes = database.getBorderNodeIds();
    private Integer sizeNodes = nodeList.size();
    private RandomNumberGenerator rng;

    public VehicleRoutApp(double activityDurationInHours) {
        this.activityDuration =
                Math.max(1, (long) (activityDurationInHours * TIME.SECOND * 3600) +
                (getRandom().nextLong(0, 3600) - 1800) * TIME.SECOND);
    }


    @Override
    public void onStartup() {
        this.stopWatch = new StopWatch(this.getOs());
    }

    @Override
    public void onShutdown() {
        stopWatch.stop(getLog());
    }

    @Override
    public void processEvent(Event event) throws Exception {
        if (event instanceof NightDriveEvent) {
            nightDrive((NightDriveEvent) event);
        }
    }

    private GeoPoint randomNightDrive(){
        int rngRoll = rng.nextInt(sizeNodes);
        Node destNode = nodeList.get(rngRoll);

        String destId = destNode.getId();
        GeoPoint destGeoPoint = destNode.getPosition();

        return destGeoPoint;
    }

    private void nightDrive(NightDriveEvent event) {
        final INavigationModule navigationModule = getOs().getNavigationModule();
        final RoutingPosition targetPosition = new RoutingPosition(event.getTargetPosition());
        final RoutingParameters routingParameters =
                new RoutingParameters().vehicleClass(getOs().getInitialVehicleType().getVehicleClass());

        final CandidateRoute routeBack = navigationModule.calculateRoutes(targetPosition, routingParameters).getBestRoute();
        if (routeBack == null) {
            getLog().error("Could not calculate route from {} to {}.", getOs().getPosition(),
                    event.getTargetPosition());
            return;
        }

        navigationModule.switchRoute(routeBack);
        getOs().resume();
    }


    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        if (!initialTripPlanned) {
            IRoadPosition roadPosition =
                    RoadPositionFactory.createAtEndOfRoute(getOs().getNavigationModule().getCurrentRoute(), 0);
            getOs().stop(roadPosition, VehicleStopMode.PARK_ON_ROADSIDE, Integer.MAX_VALUE);
            initialTripPlanned = true;
            stopWatch.start();

        }

        if (!returnTripPlanned && getOs().getVehicleData().isStopped()) {
            double distanceToTarget = getOs().getPosition().distanceTo(getOs().getNavigationModule().getTargetPosition());
            if (distanceToTarget > 50) {
                getLog().warn(
                        "Vehicle stopped but is not close to it's target. This should not happen. (distance is {} m)",
                        distanceToTarget
                );
            }

            for (Application app : getOs().getApplications()) {
                Event event = new NightDriveEvent(
                        getOs().getSimulationTime() + activityDuration, app,
                        getOs().getPosition(), randomNightDrive()
                );
                getOs().getEventManager().addEvent(event);
            }
            returnTripPlanned = true;
        }
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgement) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {
        // TODO Auto-generated method stub
        
    }

    static class NightDriveEvent extends Event {

        private final GeoPoint currentPosition;
        private final GeoPoint nextPosition;

        private NightDriveEvent(long time, @Nonnull EventProcessor processor, GeoPoint currentPosition, GeoPoint nextPosition) {
            super(time, processor);
            this.currentPosition = currentPosition;
            this.nextPosition = nextPosition;
        }

        GeoPoint getCurrentPosition() {
            return currentPosition;
        }

        GeoPoint getTargetPosition() {
            return nextPosition;
        }

    }

    static class StopWatch {
        private final VehicleOperatingSystem os;
        private long startTime;

        StopWatch(VehicleOperatingSystem os) {
            this.os = os;
        }

        public StopWatch start() {
            startTime = os.getSimulationTime();
            return this;
        }

        public double stop(UnitLogger logger) {
            double timeInS = ((double) (os.getSimulationTime() - startTime)) / TIME.SECOND;
            logger.info("Trip finished. Duration: {} s, Distance driven: {}", timeInS, os.getVehicleData().getDistanceDriven());
            return timeInS;
        }
    }
}
