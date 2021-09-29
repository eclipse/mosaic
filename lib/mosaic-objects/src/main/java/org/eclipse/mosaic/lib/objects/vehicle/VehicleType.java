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

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.eclipse.mosaic.lib.enums.LaneChangeMode;
import org.eclipse.mosaic.lib.enums.SpeedMode;
import org.eclipse.mosaic.lib.enums.VehicleClass;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * This class was created to represent a vehicle type.
 */
@Immutable
public class VehicleType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The default length of a vehicle. Unit: [m]
     */
    public final static double DEFAULT_VEHICLE_LENGTH = 5;

    /**
     * The default width of a vehicle. Unit: [m]
     */
    public final static double DEFAULT_VEHICLE_WIDTH = 1.8;

    /**
     * The default height of a vehicle. Unit: [m]
     */
    public final static double DEFAULT_VEHICLE_HEIGHT = 1.5;

    /**
     * The default minimum distance between two vehicles. Unit: [m]
     */
    public final static double DEFAULT_MINIMAL_GAP = 2.5;

    /**
     * The default maximum speed. Unit: [m/2]
     */
    public final static double DEFAULT_MAX_SPEED_MS = 70;

    /**
     * The default acceleration of a vehicle. Unit: [m/s^2]
     */
    public final static double DEFAULT_ACCELERATION = 2.6;

    /**
     * The default deceleration of a vehicle. Unit: [m/s^2]
     */
    public final static double DEFAULT_DECELERATION = 4.5;

    /**
     * The default value for driver imperfection.
     */
    public final static double DEFAULT_SIGMA = 0.5;

    /**
     * The default reaction time of the vehicles driver. Unit: [m]
     */
    public final static double DEFAULT_TAU = 1.0;

    /**
     * The default speed factor of the vehicles driver.
     */
    public final static double DEFAULT_SPEED_FACTOR = 1.0;

    /**
     * The default deviation of the speed factor the vehicles driver. Unit: [m]
     */
    public final static double DEFAULT_SPEED_DEVIATION = 0.0;

    /**
     * Name of the vehicle type.
     */
    private final String name;

    /**
     * The length of the vehicle. Unit: [m].
     */
    private final double length;

    /**
     * The width of the vehicle. Unit: [m].
     */
    private final double width;

    /**
     * The height of the vehicle. Unit: [m].
     */
    private final double height;

    /**
     * Minimal distance between front bumper of a vehicle
     * and the back bumper of its leader in a traffic jam. Unit: [m].
     */
    private final double minGap;

    /**
     * The maximum speed of the vehicle. Unit: [m/s].
     */
    private final double maxSpeed;

    /**
     * The class of the vehicle.
     */
    private final VehicleClass vehicleClass;

    /**
     * Normal acceleration used to adjust to higher velocities. Unit: [m/s^2].
     */
    private final double accel;

    /**
     * Normal deceleration used to adjust to slower velocities. Unit: [m/s^2].
     */
    private final double decel;

    /**
     * Emergency deceleration. The maximum deceleration a vehicle can take to avoid an accident. Unit: [m/s^2].
     */
    private final double emergencyDecel;

    /**
     * The driver's imperfection (0..1).
     */
    private final double sigma;

    /**
     * The driver's reaction time. Unit: [s].
     */
    private final double tau;

    /**
     * The speed factor of the vehicle. With a value of 1.1, the vehicle would exceed
     * the speed limit of an edge by 10 percent.
     */
    private final double speedFactor;

    /**
     * The color of the vehicle for visualization purposes.
     */
    private final String color;

    /**
     * The lane change mode of the vehicle. Defines which kind of lane changes a
     * vehicle is allowed to initiate. More details see at {@link LaneChangeMode}.
     */
    private final LaneChangeMode laneChangeMode;

    /**
     * The speed mode of the vehicle. Defines the speeding and
     * braking behavior of the vehicle. More details see at {@link SpeedMode}.
     */
    private final SpeedMode speedMode;

    /**
     * Constructor. Pass null values to use default values.
     *  @param name           Name of the vehicle type.
     * @param length         Length of the vehicle. Unit: [m].
     * @param minGap         Distance between front bumper of a vehicle
     *                       and the back bumper of its leader in a traffic jam. Unit: [m].
     * @param maxSpeed       Maximal speed. Unit: [m/s].
     * @param vehicleClass   Class of the vehicle.
     * @param accel          Acceleration. Unit: [m/s^2].
     * @param decel          Deceleration. Unit: [m/s^2].
     * @param emergencyDecel Emergency deceleration. Unit: [m/s^2]
     * @param sigma          Driver imperfection.
     * @param tau            Driver reaction time. Unit: [s].
     * @param laneChangeMode Lane change mode of the Vehicle.
     * @param speedMode      Speed mode of the Vehicle.
     */
    public VehicleType(String name, Double length, Double width,
                       Double height, Double minGap,
                       Double maxSpeed, VehicleClass vehicleClass,
                       Double accel, Double decel, Double emergencyDecel,
                       Double sigma, Double tau, Double speedFactor,
                       String color, LaneChangeMode laneChangeMode,
                       SpeedMode speedMode) {
        this.name = name;
        this.length = defaultIfNull(length, DEFAULT_VEHICLE_LENGTH);
        this.width = defaultIfNull(width, DEFAULT_VEHICLE_WIDTH);
        this.height = defaultIfNull(height, DEFAULT_VEHICLE_HEIGHT);
        this.minGap = defaultIfNull(minGap, DEFAULT_MINIMAL_GAP);
        this.maxSpeed = defaultIfNull(maxSpeed, DEFAULT_MAX_SPEED_MS);
        this.vehicleClass = defaultIfNull(vehicleClass, VehicleClass.Car);
        this.accel = defaultIfNull(accel, DEFAULT_ACCELERATION);
        this.decel = defaultIfNull(decel, DEFAULT_DECELERATION);
        this.emergencyDecel = defaultIfNull(emergencyDecel, this.decel);
        this.sigma = defaultIfNull(sigma, DEFAULT_SIGMA);
        this.tau = defaultIfNull(tau, DEFAULT_TAU);
        this.speedFactor = defaultIfNull(speedFactor, DEFAULT_SPEED_FACTOR);
        this.color = color;
        this.laneChangeMode = defaultIfNull(laneChangeMode, LaneChangeMode.DEFAULT);
        this.speedMode = defaultIfNull(speedMode, SpeedMode.DEFAULT);
    }

    /**
     * Constructor which sets the type name.
     * All other values are default values.
     *
     * @param name Name of the vehicle type
     */
    public VehicleType(String name) {
        this(name, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public String getName() {
        return name;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getMinGap() {
        return minGap;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public double getAccel() {
        return accel;
    }

    public double getDecel() {
        return decel;
    }

    public double getEmergencyDecel() {
        return emergencyDecel;
    }

    public double getSigma() {
        return sigma;
    }

    public double getTau() {
        return tau;
    }

    public double getSpeedFactor() {
        return speedFactor;
    }

    public String getColor() {
        return color;
    }

    public LaneChangeMode getLaneChangeMode() {
        return laneChangeMode;
    }

    public SpeedMode getSpeedMode() {
        return speedMode;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 79)
                .append(name)
                .append(length)
                .append(width)
                .append(height)
                .append(minGap)
                .append(maxSpeed)
                .append(vehicleClass)
                .append(accel)
                .append(decel)
                .append(emergencyDecel)
                .append(sigma)
                .append(tau)
                .append(speedFactor)
                .append(color)
                .append(laneChangeMode)
                .append(speedMode)
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

        VehicleType other = (VehicleType) obj;
        return new EqualsBuilder()
                .append(this.name, other.name)
                .append(this.length, other.length)
                .append(this.width, other.width)
                .append(this.height, other.height)
                .append(this.minGap, other.minGap)
                .append(this.maxSpeed, other.maxSpeed)
                .append(this.vehicleClass, other.vehicleClass)
                .append(this.accel, other.accel)
                .append(this.decel, other.decel)
                .append(this.emergencyDecel, other.emergencyDecel)
                .append(this.sigma, other.sigma)
                .append(this.tau, other.tau)
                .append(this.speedFactor, other.speedFactor)
                .append(this.color, other.color)
                .append(this.laneChangeMode, other.laneChangeMode)
                .append(this.speedMode, other.speedMode)
                .isEquals();
    }

    @Override
    public String toString() {
        return "VehicleType["
                + "name='" + name + '\''
                + ", length=" + length
                + ", width=" + width
                + ", height=" + height
                + ", minGap=" + minGap
                + ", maxSpeed=" + maxSpeed
                + ", vehicleClass=" + vehicleClass
                + ", accel=" + accel
                + ", decel=" + decel
                + ", emergencyDecel=" + emergencyDecel
                + ", sigma=" + sigma
                + ", tau=" + tau
                + ", speedFactor=" + speedFactor
                + ", color='" + color + '\''
                + ", laneChangeMode='" + laneChangeMode + '\''
                + ", speedMode='" + speedMode + '\''
                + ']';
    }
}
