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

package org.eclipse.mosaic.starter.config;

import org.eclipse.mosaic.lib.util.gson.TimeFieldAdapter;
import org.eclipse.mosaic.rti.config.CIpResolver;
import org.eclipse.mosaic.rti.config.CProjection;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class CScenario {

    /**
     * Basic configuration of the scenario and the simulation.
     */
    public Simulation simulation = new Simulation();

    /**
     * A list of ambassadors or federates to be active in the simulation.
     * Each key refers to a federate descriptor in the etc/runtime.xml. The associated
     * value ({@code true} or {@code false}) states whether the federate should be included
     * in the actual simulation or not.
     */
    public Map<String, Boolean> federates = new HashMap<>();

    public static class Simulation {

        /**
         * The id or name of the scenario.
         */
        public String id;

        /**
         * The simulation duration in seconds.
         */
        @JsonAdapter(TimeFieldAdapter.LegacySeconds.class)
        public long duration;

        /**
         * The random seed to use for all random number generators. If not set,
         * all random number generators are initialized with a different seed, thus every
         * simulation run may return different results.
         */
        @Nullable
        public Long randomSeed;

        /**
         * The projection configuration which defines the projection from WGS84 coordinates to local
         * cartesian coordinates of the simulation playground.
         *
         * <p>
         * The projection is based on UTM coordinates. The field {@link CProjection#centerCoordinates}
         * refers roughly to the center of the simulation area and is used only to determine the UTM
         * zone used for the transformation. The field {@link CProjection#cartesianOffset} is
         * an offset added to a transformed UTM coordinate. This value is usually extracted
         * from the traffic or vehicle simulator road network definition.
         * </p>
         */
        @SerializedName("projection")
        public CProjection projectionConfig;


        /**
         * The network configuration which defines the subnet masks to use for each type of entities.
         */
        @SerializedName("network")
        public CIpResolver networkConfig = new CIpResolver();
    }

}
