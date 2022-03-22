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

package org.eclipse.mosaic.fed.mapping.config.units;

import org.eclipse.mosaic.fed.mapping.config.CPrototype;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Mapper to define an Origin-Destination Matrix. The mapper contains a list of points (with
 * varying radius) and a matrix (arrays) of flow values. It creates a series of
 * conventional vehicles spawners from the specified data.
 */
public class COriginDestinationMatrixMapper {


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
     * The lane selection mode which chooses the lane for the next departing vehicle.
     *
     * @see VehicleDeparture.LaneSelectionMode#ROUNDROBIN
     * @see VehicleDeparture.LaneSelectionMode#FREE
     * @see VehicleDeparture.LaneSelectionMode#BEST
     * @see VehicleDeparture.LaneSelectionMode#HIGHWAY
     */
    @JsonAdapter(CVehicle.LaneSelectionModeTypeAdapter.class)
    public VehicleDeparture.LaneSelectionMode laneSelectionMode = VehicleDeparture.LaneSelectionMode.DEFAULT;

    /**
     * The depart speed mode. Depending on the value the depart speed behaves
     * as follows:
     * <p></p>
     * PRECISE = Use the value given in {@link CVehicle#departSpeed}
     * RANDOM  = The {@link CVehicle#departSpeed} will be overridden by a random value
     * MAXIMUM = The {@link CVehicle#departSpeed} will be overridden by the max value
     */
    @JsonAdapter(CVehicle.DepartSpeedModeTypeAdapter.class)
    public VehicleDeparture.DepartureSpeedMode departSpeedMode = VehicleDeparture.DepartureSpeedMode.MAXIMUM;


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
}
