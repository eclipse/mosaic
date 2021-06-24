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

package org.eclipse.mosaic.fed.application.app.api.os;

import org.eclipse.mosaic.fed.application.ambassador.navigation.INavigationModuleOwner;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleParameters;
import org.eclipse.mosaic.interactions.vehicle.VehicleDistanceSensorActivation.DistanceSensors;
import org.eclipse.mosaic.interactions.vehicle.VehicleLaneChange;
import org.eclipse.mosaic.interactions.vehicle.VehicleStop;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;

import javax.annotation.Nullable;

/**
 * This interface extends the basic {@link OperatingSystem} and
 * is implemented by the {@link org.eclipse.mosaic.fed.application.ambassador.simulation.AbstractSimulationUnit}
 * {@link org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleUnit}.
 */
public interface VehicleOperatingSystem extends OperatingSystem, INavigationModuleOwner {

    /**
     * Returns the current vehicle data state.
     *
     * @return the current vehicle data state.
     */
    @Nullable
    VehicleData getVehicleData();

    /**
     * Forces a lane change to the lane with the given index; If successful, the
     * lane will be chosen for the given amount of time.
     *
     * @param targetLaneIndex index of target lane
     * @param duration        the duration for the lane change in millisecond
     */
    void changeLane(int targetLaneIndex, int duration);

    /**
     * Forces a lane change to the lane according to the specified mode; If
     * successful, the lane will be chosen for the given amount of time.
     * <p></p>
     * Note: use {@link #changeLane(int targetLaneIndex, int duration) changeLane()}, for
     * mode {@code VehicleLaneChangeMode.index}.
     *
     * @param vehicleLaneChangeMode mode to change lane
     * @param duration              the duration for the lane change in millisecond
     */
    void changeLane(VehicleLaneChange.VehicleLaneChangeMode vehicleLaneChangeMode, int duration);

    /**
     * Slow down the vehicle with the given Id to the given speed until the
     * given time (the vehicle is not guaranteed to have the given speed at the end of the given time).
     * With this command it is possible to simulate e.g., instant brakes, slowing down, etc.
     *
     * @param speed    new speed
     * @param interval Time interval for which the slow down should be valid, after that
     *                 interval the vehicle will accelerate again. Unit: [ms].
     */
    void slowDown(float speed, int interval);

    /**
     * Resets the speed of the vehicle to car-following rules after the speed has been set
     * with {@link #changeSpeedWithInterval(double, int)}.
     */
    void resetSpeed();

    /**
     * Allows control over the vehicles speed.
     *
     * @param newSpeed is the target speed in [m/s]
     * @param interval the new speed should be reached in [ms]
     */
    void changeSpeedWithInterval(double newSpeed, int interval);

    /**
     * Allows control over the vehicle speed and the acceleration.
     *
     * @param newSpeed           is the target speed in [m/s]
     * @param forcedAcceleration is the acceleration in m/s², forced acceleration with highest priority
     */
    void changeSpeedWithForcedAcceleration(double newSpeed, double forcedAcceleration);

    /**
     * Allows control over the vehicle speed. The pleasantAcceleration set in DriverProperties will be used.
     *
     * @param newSpeed is the target speed in [m/s]
     */
    void changeSpeedWithPleasantAcceleration(double newSpeed);

    /**
     * Sends a stop message to stop the vehicle along the road or by the road side.
     *
     * @param stopPosition    Position on road where the vehicle is supposed to stop at
     * @param vehicleStopMode Stop mode
     * @param durationInMs    Duration of the stop, unit: [ms]
     */
    void stop(IRoadPosition stopPosition, VehicleStop.VehicleStopMode vehicleStopMode, int durationInMs);

    /**
     * Sends a stop message to stop the vehicle along the road or by the road side. The
     * vehicle is tried to stop as soon as possible.
     *
     * @param vehicleStopMode Stop mode
     * @param durationInMs    Duration of the stop, unit: [ms]
     */
    void stopNow(VehicleStop.VehicleStopMode vehicleStopMode, int durationInMs);

    /**
     * Resumes a previously stopped vehicle.
     */
    void resume();

    /**
     * Returns the road position of the vehicle containing all information about the
     * vehicle's position within the road network, such as previous and upcoming node or
     * the way the vehicle is currently driving on.
     *
     * @return the current road position
     */
    IRoadPosition getRoadPosition();

    /**
     * Returns the type of the vehicle.
     *
     * @return the type of the vehicle.
     */
    VehicleType getInitialVehicleType();

    /**
     * Returns vehicle parameters.
     *
     * @return the behavioral parameters of the vehicle (such as maximum speed or lane-change-behavior)
     */
    VehicleParameters getVehicleParameters();

    /**
     * Returns a request object, which can be used to change vehicle parameters. After
     * changing relevant parameters, this request object must be passed to
     * {@link VehicleOperatingSystem#applyVehicleParametersChange(VehicleParameters.VehicleParametersChangeRequest)} in order
     * to apply the vehicle parameters.
     *
     * @return an request object, which can be used to change vehicle parameters.
     */
    VehicleParameters.VehicleParametersChangeRequest requestVehicleParametersUpdate();

    /**
     * Applies vehicle parameter changes which are defined in {@param vehicleParametersRequest}.
     *
     * @param vehicleParametersRequest The request, which has been created using
     *                                 {@link VehicleOperatingSystem#requestVehicleParametersUpdate()}.
     */
    void applyVehicleParametersChange(VehicleParameters.VehicleParametersChangeRequest vehicleParametersRequest);

    /**
     * Activates the detection of the leading vehicle within in a given distance.
     *
     * @param sensorRange The maximum distance to look ahead for leading vehicles.
     */
    void activateVehicleDistanceSensors(double sensorRange, DistanceSensors... sensors);
}
