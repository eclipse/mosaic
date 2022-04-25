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

package org.eclipse.mosaic.fed.application.config;

import org.eclipse.mosaic.lib.routing.config.CRouting;
import org.eclipse.mosaic.lib.util.gson.TimeFieldAdapter;
import org.eclipse.mosaic.lib.util.gson.UnitFieldAdapter;
import org.eclipse.mosaic.lib.util.scheduling.MultiThreadedEventScheduler;
import org.eclipse.mosaic.rti.TIME;

import com.google.gson.annotations.JsonAdapter;

/**
 * Class for a configuration.
 */
public class CApplicationAmbassador {

    /**
     * To free some memory, use a time limit for cached V2XMessages.
     * Default value is {@code 30} seconds.
     * Use {@code 0} for an infinity cache. Unit: [ns].
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public long messageCacheTime = 30 * TIME.SECOND;

    /**
     * The minimal size which the payload of any ETSI message (CAM, DENM, IVIM) pay should have. Unit: [bytes].
     */
    public int minimalPayloadLength = 200;

    /**
     * If set to {@code true}, messages (e.g. CAMs, DENMs, or SPATMs) will be encoded
     * into a byte array. If set to {@code false}, only there length is stored which
     * may help to improve performance on large-scale scenarios. default: {@code true}
     */
    public boolean encodePayloads = true;


    /**
     * Number of threads used by the {@link MultiThreadedEventScheduler}.
     * using more than 1 thread would result in undetermined behavior.
     * Repeating the simulation could result in different simulation results,
     * if some processed event uses the random number generator.
     */
    public int eventSchedulerThreads = 1;

    /**
     * Class containing the information for the configuration of the
     * Routing/Navigation (CentralNavigationComponent).
     */
    public CRoutingByType navigationConfiguration = null;

    /**
     * Extends the {@link CRouting} configuration with a type parameter
     * allowing to define the actual {@link org.eclipse.mosaic.lib.routing.Routing}
     * implementation to use.
     */
    public static class CRoutingByType extends CRouting {

        /**
         * Defines the {@link org.eclipse.mosaic.lib.routing.Routing} implementation
         * to use for navigation. Possible values are {@code "database" or "no-routing"},
         * or any full-qualified java class name.
         */
        public String type = null;
    }

    public CPerception perceptionConfiguration = new CPerception();

    public static class CPerception {
        public enum PerceptionBackend {
            Grid, QuadTree, Trivial, SUMO
        }

        /**
         * The kind of index to use for perception [Grid, QuadTree, Trivial]. Default: QuadTree
         */
        public PerceptionBackend perceptionBackend = PerceptionBackend.QuadTree;

        /**
         * If set to {@code true}, a PerceptionPerformance.csv is generated with detailed information about execution calls
         * of the perception backend.
         */
        public boolean measurePerformance = false;

        /**
         * If {@link PerceptionBackend#Grid} is used as backend, this indicates the width of a single cell. [m]
         */
        @JsonAdapter(UnitFieldAdapter.DistanceMeters.class)
        public double gridCellWidth = 200;

        /**
         * If {@link PerceptionBackend#Grid} is used as backend, this indicates the height of a single cell. [m]
         */
        @JsonAdapter(UnitFieldAdapter.DistanceMeters.class)
        public double gridCellHeight = 200;

        /**
         * If {@link PerceptionBackend#QuadTree} is used as backend,
         * this indicates the maximum number of vehicles inside a tile before splitting.
         */
        public int treeSplitSize = 20;

        /**
         * If {@link PerceptionBackend#QuadTree} is used as backend,
         * this indicates the maximum depth of the quad-tree.
         */
        public int treeMaxDepth = 12;
    }
}
