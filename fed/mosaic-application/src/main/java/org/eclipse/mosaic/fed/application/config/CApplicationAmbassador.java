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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.TrafficLightIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.WallIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.WallTree;
import org.eclipse.mosaic.lib.geo.GeoRectangle;
import org.eclipse.mosaic.lib.routing.config.CRouting;
import org.eclipse.mosaic.lib.util.gson.TimeFieldAdapter;
import org.eclipse.mosaic.lib.util.scheduling.MultiThreadedEventScheduler;
import org.eclipse.mosaic.rti.TIME;

import com.google.gson.annotations.JsonAdapter;

import java.io.Serializable;

/**
 * Main configuration of the MOSAIC Application simulator.
 */
public class CApplicationAmbassador implements Serializable {

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
    public static class CRoutingByType extends CRouting implements Serializable {

        /**
         * Defines the {@link org.eclipse.mosaic.lib.routing.Routing} implementation
         * to use for navigation. Possible values are {@code "database" or "no-routing"},
         * or any full-qualified java class name.
         */
        public String type = null;
    }

    /**
     * Configuration for the perception backend used in the ApplicationSimulator
     * to determine surrounding vehicles.
     */
    public CPerception perceptionConfiguration = new CPerception();

    public static class CPerception implements Serializable {

        /**
         * Backend for the spatial index providing vehicle information.
         */
        public VehicleIndex vehicleIndex;

        /**
         * Backend for the spatial index providing traffic light information.
         */
        public TrafficLightIndex trafficLightIndex;

        /**
         * Backend for the spatial index providing information about building walls.
         */
        public WallIndex wallIndex = new WallTree();

        /**
         * Area defining the section of the map in which traffic lights should be held in the index.
         * This is useful if only part of your network contains vehicles.
         */
        public GeoRectangle perceptionArea;

        /**
         * If set to {@code true}, a PerceptionPerformance.csv is generated with detailed information about execution calls
         * of the perception backend.
         */
        public boolean measurePerformance = false;
    }
}
