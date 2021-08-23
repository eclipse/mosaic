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

package org.eclipse.mosaic.fed.sumo.bridge.facades;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleGetParameter;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleGetRouteId;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleGetVehicleTypeId;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetColor;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetHighlight;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetImperfection;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetLane;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetLaneChangeMode;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetMaxAcceleration;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetMaxDeceleration;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetMaxSpeed;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetMinGap;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetMoveToXY;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetParameter;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetReactionTime;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetResume;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetRouteById;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetSlowDown;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetSpeed;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetSpeedFactor;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetSpeedMode;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetStop;
import org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetVehicleLength;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.SumoLaneChangeMode;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.SumoSpeedMode;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

public class VehicleFacade {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Bridge bridge;

    private final VehicleGetRouteId getRouteId;
    private final VehicleGetVehicleTypeId getVehicleTypeId;
    private final VehicleGetParameter getParameter;


    private final VehicleSetLane changeLane;
    private final VehicleSetSlowDown slowDown;
    private final VehicleSetStop stop;
    private final VehicleSetResume resume;
    private final VehicleSetRouteById setRouteById;
    private final VehicleSetSpeed setSpeed;

    private final VehicleSetMoveToXY moveToXY;

    private final VehicleSetHighlight highlight;
    private final VehicleSetColor setColor;
    private final VehicleSetMaxSpeed setMaxSpeed;
    private final VehicleSetMaxAcceleration setMaxAcceleration;
    private final VehicleSetMaxDeceleration setMaxDeceleration;
    private final VehicleSetSpeedFactor setSpeedFactor;
    private final VehicleSetImperfection setImperfection;
    private final VehicleSetReactionTime setReactionTime;
    private final VehicleSetMinGap setMinGap;
    private final VehicleSetVehicleLength setVehicleLength;
    private final VehicleSetLaneChangeMode setLaneChangeMode;
    private final VehicleSetSpeedMode setSpeedMode;
    private final VehicleSetParameter setParameter;


    /**
     * Creates a new {@link VehicleFacade} object.
     *
     * @param bridge Connection to Traci.
     */
    public VehicleFacade(Bridge bridge) {
        this.bridge = bridge;

        getRouteId = bridge.getCommandRegister().getOrCreate(VehicleGetRouteId.class);
        getVehicleTypeId = bridge.getCommandRegister().getOrCreate(VehicleGetVehicleTypeId.class);
        getParameter = bridge.getCommandRegister().getOrCreate(VehicleGetParameter.class);

        changeLane = bridge.getCommandRegister().getOrCreate(VehicleSetLane.class);
        slowDown = bridge.getCommandRegister().getOrCreate(VehicleSetSlowDown.class);
        stop = bridge.getCommandRegister().getOrCreate(VehicleSetStop.class);
        resume = bridge.getCommandRegister().getOrCreate(VehicleSetResume.class);
        setRouteById = bridge.getCommandRegister().getOrCreate(VehicleSetRouteById.class);
        setSpeed = bridge.getCommandRegister().getOrCreate(VehicleSetSpeed.class);
        moveToXY = bridge.getCommandRegister().getOrCreate(VehicleSetMoveToXY.class);

        setColor = bridge.getCommandRegister().getOrCreate(VehicleSetColor.class);
        setMaxSpeed = bridge.getCommandRegister().getOrCreate(VehicleSetMaxSpeed.class);
        setMaxAcceleration = bridge.getCommandRegister().getOrCreate(VehicleSetMaxAcceleration.class);
        setMaxDeceleration = bridge.getCommandRegister().getOrCreate(VehicleSetMaxDeceleration.class);
        setSpeedFactor = bridge.getCommandRegister().getOrCreate(VehicleSetSpeedFactor.class);
        setImperfection = bridge.getCommandRegister().getOrCreate(VehicleSetImperfection.class);
        setReactionTime = bridge.getCommandRegister().getOrCreate(VehicleSetReactionTime.class);
        setMinGap = bridge.getCommandRegister().getOrCreate(VehicleSetMinGap.class);
        setVehicleLength = bridge.getCommandRegister().getOrCreate(VehicleSetVehicleLength.class);
        setLaneChangeMode = bridge.getCommandRegister().getOrCreate(VehicleSetLaneChangeMode.class);
        setSpeedMode = bridge.getCommandRegister().getOrCreate(VehicleSetSpeedMode.class);
        setParameter = bridge.getCommandRegister().getOrCreate(VehicleSetParameter.class);

        highlight = bridge.getCommandRegister().getOrCreate(VehicleSetHighlight.class);
    }

    /**
     * Getter for the route Id.
     *
     * @param vehicleId The Id of the vehicle.
     * @return The route Id.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public String getRouteId(String vehicleId) throws InternalFederateException {
        try {
            return getRouteId.execute(bridge, vehicleId);
        } catch (IllegalArgumentException | CommandException e) {
            throw new InternalFederateException("Could not request route for vehicle " + vehicleId, e);
        }
    }

    /**
     * Getter for the Vehicle type Id.
     *
     * @param vehicleId The Id of the vehicle.
     * @return The Id of the vehicle type.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public String getVehicleTypeId(String vehicleId) throws InternalFederateException {
        try {
            return getVehicleTypeId.execute(bridge, vehicleId);
        } catch (IllegalArgumentException | CommandException e) {
            throw new InternalFederateException("Could not request route for vehicle " + vehicleId, e);
        }
    }

    /**
     * This method enables a vehicle to change the lane.
     *
     * @param vehicle    The Id of the vehicle.
     * @param lane       The number of the lane.
     * @param durationMs The duration in [ms].
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void changeLane(String vehicle, int lane, int durationMs) throws InternalFederateException {
        try {
            changeLane.execute(bridge, vehicle, lane, durationMs);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not change lane for vehicle {}", vehicle);
        }
    }

    /**
     * This method enables a vehicle to slow down for a explicitly time.
     *
     * @param vehicle     The if of the vehicle.
     * @param newSpeedMps The new speed in [m/s].
     * @param durationMs  The duration of slow down in [m/s].
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void slowDown(String vehicle, double newSpeedMps, int durationMs) throws InternalFederateException {
        try {
            slowDown.execute(bridge, vehicle, newSpeedMps, durationMs);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not slow down vehicle {}", vehicle);
        }
    }

    /**
     * This method enables a vehicle to stop for an explicitly duration.
     *
     * @param vehicle   The Id of the vehicle,
     * @param edgeId    The id of the edge.
     * @param position  The position of the stop.
     * @param laneIndex The index of the lane on which to stop.
     * @param duration  The duration for stop in [ms].
     * @param stopFlag  The flag indicating the type of the stop.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void stop(String vehicle, String edgeId, double position, int laneIndex, int duration, int stopFlag) throws InternalFederateException {
        try {
            stop.execute(bridge, vehicle, edgeId, position, laneIndex, duration, stopFlag);
        } catch (IllegalArgumentException | CommandException e) {
            throw new InternalFederateException("Could not stop vehicle " + vehicle, e);
        }
    }

    /**
     * This method enables a vehicle to resume the previous simulation properties after interrupting due to stop, park, slow down mode.
     *
     * @param vehicle The Id of the vehicle.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void resume(String vehicle) throws InternalFederateException {
        try {
            resume.execute(bridge, vehicle);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not resume vehicle {}", vehicle);
        }
    }

    /**
     * Setter for the route by given Id.
     *
     * @param vehicle Id of the vehicle.
     * @param routeId Id of the route.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setRouteById(String vehicle, String routeId) throws InternalFederateException {
        try {
            setRouteById.execute(bridge, vehicle, routeId);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Route for vehicle " + vehicle + " could not be changed", e);
        }
    }

    public void highlight(String vehicleId, Color color) throws InternalFederateException {
        try {
            highlight.execute(bridge, vehicleId, color);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not highlight vehicle {}", vehicleId);
        }
    }


    /**
     * Setter for the maximum speed.
     *
     * @param vehicleId Id of the vehicle.
     * @param speed     The maximum speed.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setMaxSpeed(String vehicleId, double speed) throws InternalFederateException {
        try {
            setMaxSpeed.execute(bridge, vehicleId, speed);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set max speed for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the vehicle imperfection.
     *
     * @param vehicleId         The Id of the vehicle.
     * @param imperfectionValue The imperfection factor.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setImperfection(String vehicleId, double imperfectionValue) throws InternalFederateException {
        try {
            setImperfection.execute(bridge, vehicleId, imperfectionValue);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set imperfection for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the maximum vehicle acceleration.
     *
     * @param vehicleId       The Id of the vehicle.
     * @param maxAcceleration The maximum acceleration.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setMaxAcceleration(String vehicleId, double maxAcceleration) throws InternalFederateException {
        try {
            setMaxAcceleration.execute(bridge, vehicleId, maxAcceleration);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set maximum acceleration for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the maximum vehicle deceleration.
     *
     * @param vehicleId       The Id of the vehicle.
     * @param maxDeceleration the maximum deceleration.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setMaxDeceleration(String vehicleId, double maxDeceleration) throws InternalFederateException {
        try {
            setMaxDeceleration.execute(bridge, vehicleId, maxDeceleration);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set maximum deceleration for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the minimum vehicle gap.
     *
     * @param vehicleId The Id of the vehicle.
     * @param minGap    The minimum gap.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setMinimumGap(String vehicleId, double minGap) throws InternalFederateException {
        try {
            setMinGap.execute(bridge, vehicleId, minGap);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set minimum gap for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the vehicle reaction time.
     *
     * @param vehicleId    The Id of the vehicle.
     * @param reactionTime The vehicle reaction time.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setReactionTime(String vehicleId, double reactionTime) throws InternalFederateException {
        try {
            setReactionTime.execute(bridge, vehicleId, reactionTime);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set reaction time for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the vehicle's length.
     *
     * @param vehicleId    The Id of the vehicle.
     * @param vehicleLength The new length to set.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setVehicleLength(String vehicleId, double vehicleLength) throws InternalFederateException {
        try {
            setVehicleLength.execute(bridge, vehicleId, vehicleLength);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set vehicle length for vehicle {}", vehicleId);
        }
    }


    /**
     * Setter for the vehicle's speed factor.
     *
     * @param vehicleId   The Id of the vehicle.
     * @param speedFactor The speed factor.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setSpeedFactor(String vehicleId, double speedFactor) throws InternalFederateException {
        try {
            setSpeedFactor.execute(bridge, vehicleId, speedFactor);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set speed factor for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the vehicle's lane change mode.
     *
     * @param vehicleId      The Id of the vehicle.
     * @param laneChangeMode The vehicle's lane change mode.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setLaneChangeMode(String vehicleId, SumoLaneChangeMode laneChangeMode) throws InternalFederateException {
        try {
            setLaneChangeMode.execute(bridge, vehicleId, laneChangeMode.getAsInteger());
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set lane change mode for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the vehicle's speed mode.
     *
     * @param vehicleId The Id of the vehicle.
     * @param speedMode The vehicle's speed mode.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setSpeedMode(String vehicleId, SumoSpeedMode speedMode) throws InternalFederateException {
        try {
            setSpeedMode.execute(bridge, vehicleId, speedMode.getAsInteger());
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set speed mode for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the vehicle's color.
     *
     * @param vehicleId The Id of the vehicle.
     * @param red       Red value [0-255]
     * @param green     Green value [0-255]
     * @param blue      Blue value [0-255]
     * @param alpha     Alpha value [0-255]
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setColor(String vehicleId, int red, int green, int blue, int alpha) throws InternalFederateException {
        try {
            setColor.execute(bridge, vehicleId, red, green, blue, alpha);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set color for vehicle {}", vehicleId);
        }
    }

    /**
     * Setter for the vehicle's speed.
     *
     * @param vehicleId The Id of the vehicle.
     * @param speed     The new speed.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setSpeed(String vehicleId, double speed) throws InternalFederateException {
        try {
            setSpeed.execute(bridge, vehicleId, speed);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set speed for vehicle {}", vehicleId);
        }
    }

    /**
     * Sets a vehicle parameter for the vehicle.
     *
     * @param vehicleId The Id of the vehicle.
     * @param parameter The parameter key to set.
     * @param value     The parameter value to set.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setParameter(String vehicleId, String parameter, String value) throws InternalFederateException {
        try {
            setParameter.execute(bridge, vehicleId, parameter, value);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set parameter {} for vehicle {}", parameter, vehicleId);
        }
    }

    /**
     * Sets a vehicle parameter for the vehicle.
     *
     * @param vehicleId The Id of the vehicle.
     * @param parameter The parameter key to set.
     * @param value     The parameter value to set.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setParameter(String vehicleId, String parameter, double value) throws InternalFederateException {
        try {
            setParameter.execute(bridge, vehicleId, parameter, value);
        } catch (IllegalArgumentException | CommandException e) {
            log.warn("Could not set parameter {} for vehicle {}", parameter, vehicleId);
        }
    }

    /**
     * Returns the current value for the specified parameter name.
     *
     * @param vehicleId     The Id of the vehicle.
     * @param parameterName The parameter key to set.
     * @return the parameter value, or {@code null} if not present
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public String getParameter(String vehicleId, String parameterName) throws InternalFederateException {
        try {
            return getParameter.execute(bridge, vehicleId, parameterName);
        } catch (IllegalArgumentException | CommandException e) {
            log.error("Could not retrieve or transform parameter {} for vehicle {}.", parameterName, vehicleId);
            return null;
        }
    }

    /**
     * Returns the current value for the specified parameter name.
     *
     * @param vehicleId     The Id of the vehicle.
     * @param parameterName The parameter key to set.
     * @return the parameter value, or {@code 0.0} if not present
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public double getParameterAsDouble(String vehicleId, String parameterName) throws InternalFederateException {
        try {
            String parameter = getParameter(vehicleId, parameterName);
            return parameter != null ? Double.parseDouble(parameter) : 0d;
        } catch (NumberFormatException e) {
            log.error("Could not retrieve or transform parameter {} for vehicle {}.", parameterName, vehicleId);
            return 0d;
        }
    }


    /**
     * This method enables the vehicle to move to an explicit position.
     *
     * @param vehicleId      The Id of the vehicle.
     * @param cartesianPoint The cartesian point to which the vehicle moves.
     * @param angle          The angle of the new position.
     * @param mode           The mode of the movement.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void moveToXY(String vehicleId, CartesianPoint cartesianPoint, double angle, VehicleSetMoveToXY.Mode mode) throws InternalFederateException {
        try {
            log.trace("Trying to move vehicle {} to position {} with angle {} and mode {}", vehicleId, cartesianPoint, angle, mode);
            moveToXY.execute(bridge, vehicleId, "", 0, cartesianPoint, angle, mode);
        } catch (IllegalArgumentException | CommandException e) {
            throw new InternalFederateException("Could not move vehicle " + vehicleId, e);
        }
    }
}
