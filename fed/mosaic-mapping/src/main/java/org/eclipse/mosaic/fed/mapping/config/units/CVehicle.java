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

package org.eclipse.mosaic.fed.mapping.config.units;

import org.eclipse.mosaic.fed.mapping.config.CPrototype;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture.DepartSpeedMode;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture.LaneSelectionMode;
import org.eclipse.mosaic.lib.util.gson.AbstractEnumDefaultValueTypeAdapter;
import org.eclipse.mosaic.lib.util.gson.UnitFieldAdapter;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Class defining a vehicle to be spawned in the simulation. Will be parsed against
 * JSON-configuration. This class is responsible for describing the vehicles
 * populating the simulation. It is possible to create a single vehicle
 * (maxNumberVehicles should be one in that case) or a stream of one or multiple
 * vehicles. The type(-s) are defined in the field types. When more than
 * one type is defined the weights in the {@link CPrototype}
 * can be used to balance them against each other.
 */
public class CVehicle implements Comparable<CVehicle> {

    /**
     * Mapper to define an Origin-Destination Matrix. The mapper contains a list of points (with
     * varying radius) and a matrix (arrays) of flow values. It creates a series of
     * conventional vehicles spawners from the specified data.
     */
    public static class COriginDestinationMatrixMapper {

        /**
         * List of points that can be referenced from the OD-matrix.
         */
        public List<COriginDestinationPoint> points;

        /**
         * List of the vehicles that should be spawned.
         */
        public List<CPrototype> types;

        /**
         * If deterministic is true the spawning-process will be exactly the same
         * with every execution. If left false the order is different and the
         * selected weights will be reached slower than in the deterministic mode.
         */
        public Boolean deterministic = true;

        /**
         * Values for the OD-matrix. Unit should be vehicles/hour.
         */
        public List<List<Double>> odValues;
    }

    /**
     * Defines a point that can be referenced from an OD-matrix.
     */
    public static class COriginDestinationPoint {
        /**
         * The name of the point.
         */
        public String name;

        /**
         * The center of the circle and the distance from the point from which vehicles will be spawned.
         * If left empty (<code>0</code>) then only the closest node will be used.
         */
        public GeoCircle position;
    }

    public enum SpawningMode {
        CONSTANT, POISSON,
        GROW, SHRINK, GROW_AND_SHRINK,
        GROW_EXPONENTIAL, SHRINK_EXPONENTIAL, GROW_AND_SHRINK_EXPONENTIAL
    }

    /**
     * Time at which the first vehicle will be created.
     */
    public double startingTime = 0.0;

    /**
     * Simulation time in seconds at which no more vehicles will be created.
     */
    @SerializedName(value = "maxTime", alternate = {"endingTime"})
    public Double maxTime;

    /**
     * Density of vehicles per hour. Vehicles will be spawned uniformly.
     */
    @SerializedName(value = "targetFlow", alternate = {"targetDensity"})
    public double targetFlow = 600.0;

    /**
     * Adjusts the departure time of individual vehicles.
     *
     * @see SpawningMode#CONSTANT spawns vehicles by the given targetFlow
     * @see SpawningMode#GROW increases the vehicle flow demand exponentially to targetFlow until maxTime is reached
     * @see SpawningMode#SHRINK decreases the vehicle flow demand exponentially from targetFlow until maxTime is reached
     */
    @JsonAdapter(SpawningModeTypeAdapter.class)
    public SpawningMode spawningMode = SpawningMode.CONSTANT;

    /**
     * Defining a maximum number of vehicles to be created from this source.
     */
    public Integer maxNumberVehicles;

    /**
     * List of lanes to be used. The vehicles will be evenly distributed among
     * the given lanes. When no value is given lane zero will be used for all
     * vehicles.
     */
    public List<Integer> lanes;

    /**
     * The lane selection mode which chooses the lane for the next departing vehicle.
     *
     * @see LaneSelectionMode#ROUNDROBIN
     * @see LaneSelectionMode#FREE
     * @see LaneSelectionMode#BEST
     * @see LaneSelectionMode#HIGHWAY
     */
    @JsonAdapter(LaneSelectionModeTypeAdapter.class)
    public LaneSelectionMode laneSelectionMode = LaneSelectionMode.DEFAULT;

    /**
     * The speed at which the vehicle is supposed to depart. Depending on the simulator
     * this value may only be used if {@link CVehicle#departSpeedMode} is set to PRECISE.
     */
    @JsonAdapter(UnitFieldAdapter.SpeedMS.class)
    public double departSpeed;

    /**
     * The depart speed mode. Depending on the value the depart speed behaves
     * as follows:
     * <p></p>
     * PRECISE = Use the value given in {@link CVehicle#departSpeed}
     * RANDOM  = The {@link CVehicle#departSpeed} will be overridden by a random value
     * MAXIMUM = The {@link CVehicle#departSpeed} will be overridden by the max value
     */
    @JsonAdapter(DepartSpeedModeTypeAdapter.class)
    public DepartSpeedMode departSpeedMode = DepartSpeedMode.MAXIMUM;

    /**
     * List of possible vehicle types to be spawned. In this list you can simply refer to an
     * existing {@link CPrototype} by its {@link CPrototype#name} attribute to include everything
     * defined there. You can also overwrite every attribute of the prototype.
     * If you don't have an existing prototype the definitions found here will be used as the
     * prototype definition itself. <br />
     * <br />
     * Example configuration file:
     * <pre>
     * {
     *    "prototypeDeserializers":[
     *        {
     *           "name":"PKW",
     *           "accel":2.5,
     *           "decel":4.5,
     *           "length":5.0,
     *           "minGap":2.5,
     *           "maxSpeed":70.0
     *       }
     *    ],
     *    "vehicles":[
     *        {
     *            "route":"0",
     *            "types":[
     *                { "name":"PKW" },
     *                {
     *                    "name":"PKW",
     *                    "accel":2.0,
     *                    "length":7.0
     *                }
     *            ]
     *        }
     *    ]
     * }
     * </pre>
     */
    public List<CPrototype> types;

    /**
     * Defines the distribution of vehicle types.
     */
    public String typeDistribution;

    /**
     * Determines if selection of a vehicles type when spawning follows a deterministic or stochastic model.
     * When set to true the spawning-process will choose exactly the same types with every execution.
     * When set to false the order of types may be different and selected weights will be reached more slowly.
     */
    public boolean deterministic = true;

    /**
     * Position within the route where the vehicle(-s) should be spawned.
     */
    public int pos = 0;

    /**
     * Route that the vehicle(-s) should use. If an origin and a destination are
     * specified this route will be treated as a preference (i.e. it will be
     * selected if it connects the two points in question).
     */
    public String route;

    /**
     * Point from which the vehicles will be spawned.
     */
    public GeoCircle origin;

    /**
     * Point to which the vehicles will travel.
     */
    public GeoCircle destination;

    /**
     * Define a group for grouping in ITEF Visualizer.
     */
    public String group;

    @Override
    public int compareTo(CVehicle o) {
        if (Double.compare(o.startingTime, 0) < 0 && Double.compare(this.startingTime, 0) < 0) {
            return 0;
        } else if (Double.compare(this.startingTime, 0) < 0) {
            return 1;
        } else if (Double.compare(o.startingTime, 0) < 0) {
            return -1;
        } else {
            return Double.compare(this.startingTime, o.startingTime);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CVehicle that = (CVehicle) o;

        return new EqualsBuilder()
                .append(startingTime, that.startingTime)
                .append(targetFlow, that.targetFlow)
                .append(departSpeed, that.departSpeed)
                .append(deterministic, that.deterministic)
                .append(pos, that.pos)
                .append(maxTime, that.maxTime)
                .append(spawningMode, that.spawningMode)
                .append(maxNumberVehicles, that.maxNumberVehicles)
                .append(lanes, that.lanes)
                .append(laneSelectionMode, that.laneSelectionMode)
                .append(departSpeedMode, that.departSpeedMode)
                .append(types, that.types)
                .append(typeDistribution, that.typeDistribution)
                .append(route, that.route)
                .append(origin, that.origin)
                .append(destination, that.destination)
                .append(group, that.group)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(startingTime)
                .append(maxTime)
                .append(targetFlow)
                .append(spawningMode)
                .append(maxNumberVehicles)
                .append(lanes)
                .append(laneSelectionMode)
                .append(departSpeed)
                .append(departSpeedMode)
                .append(types)
                .append(typeDistribution)
                .append(deterministic)
                .append(pos)
                .append(route)
                .append(origin)
                .append(destination)
                .append(group)
                .toHashCode();
    }

    private static class DepartSpeedModeTypeAdapter extends AbstractEnumDefaultValueTypeAdapter<DepartSpeedMode> {
        public DepartSpeedModeTypeAdapter() {
            super(DepartSpeedMode.MAXIMUM);
        }
    }

    private static class SpawningModeTypeAdapter extends AbstractEnumDefaultValueTypeAdapter<SpawningMode> {
        public SpawningModeTypeAdapter() {
            super(SpawningMode.CONSTANT);
        }
    }

    private static class LaneSelectionModeTypeAdapter extends AbstractEnumDefaultValueTypeAdapter<LaneSelectionMode> {
        public LaneSelectionModeTypeAdapter() {
            super(LaneSelectionMode.DEFAULT);
        }
    }
}
