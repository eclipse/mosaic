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

package org.eclipse.mosaic.lib.objects.vehicle;

import org.eclipse.mosaic.lib.enums.DriveDirection;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.UnitData;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.util.gson.PolymorphismTypeAdapterFactory;

import com.google.gson.annotations.JsonAdapter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.concurrent.Immutable;

/**
 * This class is used as a container for any vehicle data.
 */
@Immutable
public class VehicleData extends UnitData {

    private static final long serialVersionUID = 1L;

    /**
     * Contains total emissions and current emission.
     */
    private final VehicleEmissions vehicleEmissions;

    /**
     * Contains total energy consumption and current consumption.
     */
    private final VehicleConsumptions vehicleConsumptions;

    /**
     * All vehicle specific signals e.g. car flasher, hazard flasher, see in {@link VehicleSignals}
     */
    private final VehicleSignals vehicleSignals;

    /**
     * electric information for a vehicle including recuperation indices.
     */
    private final VehicleBatteryState electricInfo;

    /**
     * projected position.
     */
    private final CartesianPoint projectedPosition;

    /**
     * Direction/Heading of the vehicle in degrees from north clockwise.
     * Supported Traffic simulator: [SUMO, PHA]
     */
    private final Double heading;

    /**
     * Speed of the vehicle. Unit: [m/s].
     * Supported Traffic simulator: [SUMO, PHA]
     */
    private final double speed;

    /**
     * The slope at the current vehicle position in degrees.
     */
    private final double slope;

    /**
     * The distance, the vehicle has already driven. Unit: [m]
     * Supported Traffic simulator: [SUMO]
     */
    private final double distanceDriven;

    /**
     * Flag, indicating if the vehicle is currently stopped.
     */
    private final boolean stopped;

    /**
     * The ID of the route the vehicle currently follows.
     * Supported Traffic simulator: [SUMO, PHA]
     */
    private final String routeId;

    /**
     * Contains information about the {@link IRoadPosition} the vehicle is currently driving on.
     */
    @JsonAdapter(PolymorphismTypeAdapterFactory.class)
    private final IRoadPosition roadPosition;

    /**
     * This represents the acceleration of a vehicle in [m/s^2].
     */
    private final Double longitudinalAcceleration;

    /**
     * This dimensionless value represents the position of a brake pedal in [0 1].
     * Supported traffic simulator: [PHA]
     */
    private final Double brake;

    /**
     * This dimensionless value represents the position of a throttle pedal in [0 1].
     * Supported traffic simulator: [PHA]
     */
    private final Double throttle;

    /**
     * The direction currently driven on from the point of view of the vehicle.
     * This means forward/backward/unavailable.
     * If you are looking for an angle see {@link VehicleData#heading}.
     * Supported Traffic simulator: [PHA]
     */
    private final DriveDirection driveDirection;

    /**
     * Contains information about the vehicle sensor (Lidar-, Radar- or Distance sensor).
     */
    private final VehicleSensors vehicleSensors;

    /**
     * ID of the Lane area.
     */
    private final String laneAreaId;

    /**
     * Arbitrary additional vehicle data produced by the vehicle or traffic simulator.
     */
    @JsonAdapter(PolymorphismTypeAdapterFactory.class)
    private final Object additionalData;

    /**
     * Getter for the vehicle heading in degrees from north clockwise.
     * Supported Traffic simulators: [SUMO]
     *
     * @return the heading of the vehicle
     */
    public Double getHeading() {
        return heading;
    }

    /**
     * Getter for the speed. Unit: [m/s]. <br />
     * Supported Traffic simulators: [PHABMACS, SUMO]
     *
     * @return The speed.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns information about the emissions ejected by the vehicle, e.g. CO2.
     * Supported Traffic simulators: [SUMO]
     *
     * @return emissions ejected by the vehicle
     */
    public VehicleEmissions getVehicleEmissions() {
        return vehicleEmissions;
    }

    /**
     * Returns the slope of the current vehicle position in degrees.
     */
    public double getSlope() {
        return slope;
    }

    /**
     * Returns information about the consumptions of the vehicle, e.g. fuel consumption.
     * Supported Traffic simulators: [SUMO]
     *
     * @return consumptions of the vehicle
     */
    public VehicleConsumptions getVehicleConsumptions() {
        return vehicleConsumptions;
    }

    /**
     * Returns information about the signal state of the vehicle, e.g. right/left indicator.
     * Supported simulators: [SUMO]
     *
     * @return the state of the signals of the vehicle
     */
    public VehicleSignals getVehicleSignals() {
        return vehicleSignals;
    }

    /**
     * Returns the projected position of the vehicle in Cartesian coordinates.
     * Supported Traffic simulators: [SUMO]
     *
     * @return the projected position of the vehicle (x-y coordinates)
     */
    public CartesianPoint getProjectedPosition() {
        return projectedPosition;
    }

    /**
     * Getter for the current vehicle route id.
     * Supported Traffic simulators: [PHABMACS, SUMO]
     *
     * @return Current vehicle route id. Unitless.
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Returns the longitudinal acceleration of the vehicle.
     * Supported Traffic simulators: [PHABMACS, SUMO]
     *
     * @return the longitudinal acceleration of the vehicle
     */
    public Double getLongitudinalAcceleration() {
        return longitudinalAcceleration;
    }

    public Double getBrake() {
        return brake;
    }

    public Double getThrottle() {
        return throttle;
    }

    public VehicleBatteryState getElectricInfo() {
        return electricInfo;
    }

    /**
     * Returns the driving direction of the vehicle (forward/backward).
     * Supported simulators: [PHABMACS]
     *
     * @return the drive direction of the vehicle
     */
    public DriveDirection getDriveDirection() {
        return driveDirection;
    }

    /**
     * Returns information about the sensors of the vehicle.
     * Supported simulators: [PHABMACS, SUMO (only front/rear sensor, opt-in)]
     *
     * @return the sensor state of the vehicle
     */
    public VehicleSensors getVehicleSensors() {
        return vehicleSensors;
    }

    /**
     * Returns the distance, the vehicle has already driven. Unit: [m]
     * Supported Traffic simulator: [SUMO]
     *
     * @return the distance, the vehicle has already driven. Unit: [m]
     */
    public double getDistanceDriven() {
        return distanceDriven;
    }

    /**
     * Getter for the stopped state of a vehicle.
     * Supported Traffic simulator: [PHABMACS, SUMO]
     *
     * @return Flag, indicating if the vehicle is currently stopped
     * <code>True</code>, if the vehicle is currently stopped, otherwise <code>false</code>
     * @see VehicleData#stopped
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * Returns the position of the vehicle in the road network.
     */
    public IRoadPosition getRoadPosition() {
        return roadPosition;
    }

    /**
     * Returns the identifier of the lane area in which
     * the vehicle is currently driving. Might be <code>null</code>.
     */
    public String getLaneAreaId() {
        return laneAreaId;
    }

    /**
     * Returns additional vehicle data produced by the traffic or vehicle simulator.
     * Can be of any arbitrary type, and is <code>null</code> if the producer has not been
     * implemented in a way to add this additional data.
     */
    public Object getAdditionalData() {
        return additionalData;
    }

    /**
     * Private constructor, use {@link VehicleData.Builder} instead.
     */
    private VehicleData(
            long time,
            String name,
            GeoPoint position,
            CartesianPoint projectedPosition,
            double speed,
            Double heading,
            VehicleSignals vehicleSignals,
            VehicleEmissions vehicleEmissions,
            VehicleConsumptions vehicleConsumptions,
            VehicleBatteryState electricInfo,
            IRoadPosition roadPosition,
            boolean stopped,
            String routeId,
            Double longitudinalAcceleration,
            Double brake,
            Double throttle,
            DriveDirection driveDirection,
            VehicleSensors vehicleSensors,
            double distanceDriven,
            double slope,
            String laneAreaId,
            Object additionalData
    ) {
        super(time, name, position);
        this.vehicleSignals = vehicleSignals;
        this.vehicleEmissions = vehicleEmissions;
        this.vehicleConsumptions = vehicleConsumptions;
        this.electricInfo = electricInfo;
        this.speed = speed;
        this.heading = heading;
        this.projectedPosition = projectedPosition;
        this.stopped = stopped;
        this.routeId = routeId;
        this.longitudinalAcceleration = longitudinalAcceleration;
        this.driveDirection = driveDirection;
        this.brake = brake;
        this.throttle = throttle;
        this.vehicleSensors = vehicleSensors;
        this.distanceDriven = distanceDriven;
        this.roadPosition = roadPosition;
        this.slope = slope;
        this.laneAreaId = laneAreaId;
        this.additionalData = additionalData;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 89)
                .append(vehicleEmissions)
                .append(vehicleConsumptions)
                .append(electricInfo)
                .append(vehicleSignals)
                .append(projectedPosition)
                .append(heading)
                .append(speed)
                .append(stopped)
                .append(roadPosition)
                .append(routeId)
                .append(longitudinalAcceleration)
                .append(driveDirection)
                .append(vehicleSensors)
                .append(distanceDriven)
                .append(brake)
                .append(throttle)
                .append(slope)
                .append(laneAreaId)
                .append(additionalData)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        VehicleData other = (VehicleData) obj;
        return new EqualsBuilder()
                .append(this.vehicleEmissions, other.vehicleEmissions)
                .append(this.vehicleConsumptions, other.vehicleConsumptions)
                .append(this.electricInfo, other.electricInfo)
                .append(this.vehicleSignals, other.vehicleSignals)
                .append(this.projectedPosition, other.projectedPosition)
                .append(this.heading, other.heading)
                .append(this.speed, other.speed)
                .append(this.stopped, other.stopped)
                .append(this.roadPosition, other.roadPosition)
                .append(this.routeId, other.routeId)
                .append(this.longitudinalAcceleration, other.longitudinalAcceleration)
                .append(this.brake, other.brake)
                .append(this.throttle, other.throttle)
                .append(this.driveDirection, other.driveDirection)
                .append(this.vehicleSensors, other.vehicleSensors)
                .append(this.distanceDriven, other.distanceDriven)
                .append(this.slope, other.slope)
                .append(this.laneAreaId, other.laneAreaId)
                .append(this.additionalData, other.additionalData)
                .isEquals();
    }

    @Override
    public String toString() {
        return "VehicleInfo{"
                + "vehicleEmissions=" + this.vehicleEmissions
                + ", vehicleConsumptions=" + this.vehicleConsumptions
                + ", vehicleSignals=" + this.vehicleSignals
                + ", projectedPosition=" + this.projectedPosition
                + ", heading=" + this.heading
                + ", speed=" + this.speed
                + ", distanceDriven=" + this.distanceDriven
                + ", slope=" + this.slope
                + ", roadPosition=" + this.roadPosition
                + ", stopped=" + this.stopped
                + ", routeId=" + this.routeId
                + ", longitudinalAcceleration=" + this.longitudinalAcceleration
                + ", driveDirection=" + this.driveDirection
                + ", vehicleSensors=" + this.vehicleSensors
                + '}';
    }

    /**
     * A builder for creating {@link VehicleData} objects without using the monstrous constructor.
     */
    public static class Builder {
        private final long time;
        private final String name;
        private GeoPoint position;
        private CartesianPoint projectedPosition;
        private double speed;
        private Double heading;
        private VehicleSignals vehicleSignals;
        private VehicleSensors vehicleSensors;
        private VehicleEmissions vehicleEmissions;
        private VehicleConsumptions vehicleConsumptions;
        private VehicleBatteryState electricInfo;
        private IRoadPosition roadPosition;
        private boolean stopped = false;
        private String routeId;
        private double longitudinalAcceleration;
        private DriveDirection driveDirection = DriveDirection.UNAVAILABLE;
        private double distanceDriven;
        private double slope;
        private String laneArea;
        private Object additionalData;

        /**
         * Init the builder with the current simulation time [ns] and name of the vehicle.
         */
        public Builder(long time, String name) {
            this.time = time;
            this.name = name;
        }

        /**
         * Set position related values for the vehicle info.
         */
        public Builder position(GeoPoint position, CartesianPoint projectedPosition) {
            this.position = position;
            this.projectedPosition = projectedPosition;
            return this;
        }

        /**
         * Set movement related values, such as speed and acceleration.
         *
         * @param speed          the speed of the vehicle in [m/s]
         * @param acceleration   the acceleration of the vehicle in [m/s^2]
         * @param distanceDriven the total distance driven by the vehicle
         */
        public Builder movement(double speed, double acceleration, double distanceDriven) {
            this.speed = speed;
            this.longitudinalAcceleration = acceleration;
            this.distanceDriven = distanceDriven;
            return this;
        }

        /**
         * Set orientation relative values of the vehicles, such as heading.
         *
         * @param driveDirection the drive direction, use {@link DriveDirection#UNAVAILABLE} if not known
         * @param heading        the heading of the vehicle in degrees
         * @param slope          the slope of the vehicle in degrees
         */
        public Builder orientation(DriveDirection driveDirection, double heading, double slope) {
            this.driveDirection = ObjectUtils.defaultIfNull(driveDirection, DriveDirection.UNAVAILABLE);
            this.heading = heading;
            this.slope = slope;
            return this;
        }

        /**
         * Set the position on the road of the vehicle.
         */
        public Builder road(IRoadPosition roadPosition) {
            this.roadPosition = roadPosition;
            return this;
        }

        /**
         * Set information about the route.
         */
        public Builder route(String routeId) {
            this.routeId = routeId;
            return this;
        }

        /**
         * Set information about the emissions of the vehicle.
         */
        public Builder emissions(VehicleEmissions emissions) {
            this.vehicleEmissions = emissions;
            return this;
        }

        /**
         * Set information about the consumptions of the vehicle.
         */
        public Builder consumptions(VehicleConsumptions consumptions) {
            this.vehicleConsumptions = consumptions;
            return this;
        }

        /**
         * Set information about the electric motor and the battery of the vehicle.
         */
        public Builder electric(VehicleBatteryState electricInfo) {
            this.electricInfo = electricInfo;
            return this;
        }

        /**
         * Set information about the signals state of the vehicle.
         */
        public Builder signals(VehicleSignals vehicleSignals) {
            this.vehicleSignals = vehicleSignals;
            return this;
        }

        /**
         * Set information about the sensors of the vehicle.
         */
        public Builder sensors(VehicleSensors vehicleSensors) {
            this.vehicleSensors = vehicleSensors;
            return this;
        }

        /**
         * Define, if the vehicle is currently in stopped state.
         */
        public Builder stopped(boolean stopped) {
            this.stopped = stopped;
            return this;
        }

        /**
         * Define the id of the lane area this vehicle is currently driving in.
         */
        public Builder laneArea(String laneArea) {
            this.laneArea = laneArea;
            return this;
        }

        public Builder additional(Object additionalData) {
            if (this.additionalData != null) {
                throw new IllegalStateException("This vehicle data object cannot hold more than one additional data object.");
            }
            this.additionalData = additionalData;
            return this;
        }

        /**
         * Copy all values from an existing {@link VehicleData} object.
         */
        public Builder copyFrom(VehicleData veh) {
            Validate.notNull(veh, "The vehicle info to be copied from must not be null.");

            this.position = veh.getPosition();
            this.projectedPosition = veh.getProjectedPosition();
            this.speed = veh.getSpeed();
            this.heading = veh.getHeading();
            this.vehicleSignals = veh.getVehicleSignals();
            this.vehicleSensors = veh.getVehicleSensors();
            this.vehicleEmissions = veh.getVehicleEmissions();
            this.vehicleConsumptions = veh.getVehicleConsumptions();
            this.electricInfo = veh.getElectricInfo();
            this.roadPosition = veh.getRoadPosition();
            this.stopped = veh.isStopped();
            this.routeId = veh.getRouteId();
            this.longitudinalAcceleration = veh.getLongitudinalAcceleration();
            this.driveDirection = veh.getDriveDirection();
            this.distanceDriven = veh.getDistanceDriven();
            this.slope = veh.getSlope();
            this.laneArea = veh.getLaneAreaId();
            this.additionalData = veh.getAdditionalData();
            return this;
        }

        /**
         * Returns the final {@link VehicleData} based on the properties given before.
         */
        public VehicleData create() {
            return new VehicleData(
                    time, name,
                    position, projectedPosition,
                    speed,
                    heading,
                    vehicleSignals, vehicleEmissions, vehicleConsumptions, electricInfo,
                    roadPosition,
                    stopped,
                    routeId,
                    longitudinalAcceleration,
                    0d, 0d,
                    driveDirection,
                    vehicleSensors,
                    distanceDriven,
                    slope,
                    laneArea,
                    additionalData);
        }
    }
}

