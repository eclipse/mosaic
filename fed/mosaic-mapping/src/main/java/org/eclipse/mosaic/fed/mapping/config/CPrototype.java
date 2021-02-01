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

package org.eclipse.mosaic.fed.mapping.config;

import org.eclipse.mosaic.lib.enums.LaneChangeMode;
import org.eclipse.mosaic.lib.enums.SpeedMode;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.util.gson.UnitFieldAdapter;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

/**
 * Defining basic properties for an object in the simulation. All possible
 * properties of objects are available. Only the ones needed will be used (for
 * example an RSU does not have a length, so this property would be ignored).
 * <p/>
 * If these values are not defined some default values might be applied depending on prototype type.
 */
public class CPrototype {

    /**
     * The name of this prototype. This identifier is used to match it against
     * other objects.
     */
    public String name;

    /**
     * The group name.
     */
    public String group;

    /**
     * The weight is used to distribute objects between multiple types. All
     * weights do NOT have to add up to 1 or 100. (Example: A vehicle spawner
     * defining a traffic stream contains two prototypeDeserializers with the weights being
     * 4 and 6. The resulting traffic stream will consist to 40% of the one type
     * and 60% of the other)
     */
    public Double weight;

    /**
     * Specify the applications to be used for this object. If none are
     * specified, none are used.
     */
    public List<String> applications;

    /**
     * Length of the vehicle in meter.
     */
    @JsonAdapter(UnitFieldAdapter.DistanceMeters.class)
    public Double length;

    /**
     * Distance in meter between front bumper of a vehicle
     * and the back bumper of its leader in a traffic jam.
     */
    @JsonAdapter(UnitFieldAdapter.DistanceMeters.class)
    public Double minGap;

    /**
     * Maximal speed in m/s.
     */
    @JsonAdapter(UnitFieldAdapter.SpeedMS.class)
    public Double maxSpeed;

    /**
     * Class of the vehicle.
     */
    public VehicleClass vehicleClass;

    /**
     * Acceleration in m/s^2.
     */
    public Double accel;

    /**
     * Deceleration in m/s^2.
     */
    public Double decel;

    /**
     * Emergency deceleration. The maximum deceleration a vehicle can take to avoid an accident. Unit: [m/s^2].
     */
    public Double emergencyDecel;

    /**
     * Driver imperfection.
     */
    public Double sigma;

    /**
     * Driver reaction time in seconds.
     */
    public Double tau;

    /**
     * The speed factor of the vehicle. E.g., with a value of 1.1 the
     * vehicle would exceed the speed limit of an edge by 10 percent.
     */
    public Double speedFactor;

    /**
     * The color of the vehicle for visualization purposes.
     */
    public String color;

    /**
     * The laneChangeMode of the vehicle. Accepted values are String representations of {@link LaneChangeMode}.
     */
    public LaneChangeMode laneChangeMode;

    /**
     * The speedMode of the vehicle. Accepted values are String representations of {@link SpeedMode}
     * Value is used for behavior of {@see VehicleSlowDown} and {@see VehicleSpeedChange} interaction.
     */
    public SpeedMode speedMode;

    /**
     * Parameters to deviate for each individual vehicle based in this prototype.
     */
    public CParameterDeviations deviations;


    /**
     * Creates a copy of this prototype.
     *
     * @return a new instance of this object with copied values
     */
    public CPrototype copy() {
        final CPrototype copy = new CPrototype();
        copy.name = name;
        copy.group = group;
        copy.weight = weight;
        copy.applications = applications;
        copy.length = length;
        copy.minGap = minGap;
        copy.maxSpeed = maxSpeed;
        copy.vehicleClass = vehicleClass;
        copy.accel = accel;
        copy.decel = decel;
        copy.emergencyDecel = emergencyDecel;
        copy.sigma = sigma;
        copy.tau = tau;
        copy.speedFactor = speedFactor;
        copy.color = color;
        copy.laneChangeMode = laneChangeMode;
        copy.speedMode = speedMode;
        copy.deviations = deviations;
        return copy;
    }

    @Override
    public String toString() {
        return "CPrototype[name: " + name
                + ", group: " + (group != null ? group : "null")
                + ", weight: " + (weight != null ? weight : "null")
                + ", length: " + (length != null ? length : "null")
                + ", minGap: " + (minGap != null ? minGap : "null")
                + ", maxSpeed: " + (maxSpeed != null ? maxSpeed : "null")
                + ", accel: " + (accel != null ? accel : "null")
                + ", decel: " + (decel != null ? decel : "null")
                + ", emergencyDecel: " + (emergencyDecel != null ? emergencyDecel : "null")
                + ", sigma: " + (sigma != null ? sigma : "null")
                + ", tau: " + (tau != null ? tau : "null")
                + ", speedFactor: " + (speedFactor != null ? speedFactor : "null")
                + ", color: " + (color != null ? color : "null")
                + ", laneChangeMode: " + laneChangeMode
                + ", speedMode: " + speedMode
                + ", vehicleClass: " + vehicleClass
                + ", applications: " + (applications != null ? applications : "null")
                + "]";
    }

}
