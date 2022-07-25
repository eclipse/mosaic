/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.app.examples.commuter;

import org.eclipse.mosaic.fed.application.ambassador.navigation.INavigationModule;
import org.eclipse.mosaic.fed.application.ambassador.navigation.RoadPositionFactory;
import org.eclipse.mosaic.fed.application.ambassador.util.UnitLogger;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.Application;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.enums.VehicleStopMode;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.util.scheduling.EventProcessor;
import org.eclipse.mosaic.rti.TIME;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This application simulates the behaviour of a commuting vehicle.
 * The vehicle will spend a specified amount of time at the goal position.
 */
public class SimpleCommuterApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {

    private final long activityDuration;

    private boolean initialTripPlanned = false;
    private boolean returnTripPlanned = false;

    private StopWatch stopWatch;

    /**
     * Initializes an instance of the {@link SimpleCommuterApp}.
     *
     * @param activityDurationInHours the time the vehicle spends at the goal position before driving back to the start
     *                                position
     */
    public SimpleCommuterApp(double activityDurationInHours) {
        this.activityDuration =
                Math.max(1, (long) (activityDurationInHours * TIME.SECOND * 3600) + (getRandom().nextLong(0, 3600) - 1800) * TIME.SECOND);
    }

    /**
     * Sets up the routes for the vehicle to commute between the start position and goal position of it's initial route.
     *
     * @param previousVehicleData the previous state of the vehicle
     * @param updatedVehicleData  the updated state of the vehicle
     */
    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        if (!initialTripPlanned) {
            IRoadPosition roadPosition =
                    RoadPositionFactory.createAtEndOfRoute(getOs().getNavigationModule().getCurrentRoute(), 0);
            getOs().stop(roadPosition, VehicleStopMode.PARK_ON_ROADSIDE, Long.MAX_VALUE);
            initialTripPlanned = true;
            stopWatch.start();
        }

        if (!returnTripPlanned && getOs().getVehicleData().isStopped()) {
            stopWatch.stop(getLog());
            double distanceToTarget = getOs().getPosition().distanceTo(getOs().getNavigationModule().getTargetPosition());
            if (distanceToTarget > 50) {
                getLog().warn(
                        "Vehicle stopped but is not close to it's target. This should not happen. (distance is {} m)",
                        distanceToTarget
                );
            }

            for (Application app : getOs().getApplications()) {
                Event event = new DriveBackEvent(
                        getOs().getSimulationTime() + activityDuration, app,
                        getOs().getPosition(), getOs().getInitialPosition()
                );
                getOs().getEventManager().addEvent(event);
            }
            returnTripPlanned = true;
        }
    }

    /**
     * Sets the route of the vehicle so it will travel from the goal position back to the start position.
     *
     * @param event the event to process
     */
    private void driveBack(DriveBackEvent event) {
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
        stopWatch.start();
    }

    /**
     * Processes the {@link DriveBackEvent} to call the setup for the vehicle's return trip (from the goal position to
     * the start position).
     *
     * @param event the event to process
     */
    @Override
    public void processEvent(Event event) {
        if (event instanceof DriveBackEvent) {
            driveBack((DriveBackEvent) event);
        }
    }

    @Override
    public void onStartup() {
        this.stopWatch = new StopWatch(this.getOs());
    }

    @Override
    public void onShutdown() {
        stopWatch.stop(getLog());
    }

    /**
     * Event is called to signal that the vehicle reached it's goal position and should now travel back to it's
     * start position.
     */
    static class DriveBackEvent extends Event {

        private final GeoPoint currentPosition;
        private final GeoPoint homePosition;

        private DriveBackEvent(long time, @Nonnull EventProcessor processor, GeoPoint currentPosition, GeoPoint homePosition) {
            super(time, processor);
            this.currentPosition = currentPosition;
            this.homePosition = homePosition;
        }

        GeoPoint getCurrentPosition() {
            return currentPosition;
        }

        GeoPoint getTargetPosition() {
            return homePosition;
        }

    }

    /**
     * Class to keep track of the travel distance and activity duration.
     */
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
