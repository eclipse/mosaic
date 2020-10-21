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

package org.eclipse.mosaic.fed.cell.config.model;

import org.eclipse.mosaic.fed.cell.config.gson.CapacityTypeAdapter;
import org.eclipse.mosaic.lib.model.delay.Delay;
import org.eclipse.mosaic.lib.model.gson.DelayTypeAdapterFactory;
import org.eclipse.mosaic.lib.model.transmission.CTransmission;

import com.google.gson.annotations.JsonAdapter;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * {@link CNetworkProperties} holds all coverage properties of one region of the radio access network (ran-part).
 * Such a configuration consists of one uplink-module and one downlink-module.
 * In this context, uplink and downlink always refer to the direction TOWARDS
 * respectively FROM the GEO entity.
 */
public class CNetworkProperties {
    public static final String GLOBAL_NETWORK_ID = "globalNetwork";

    /**
     * Network-Id for identification.
     */
    public String id = GLOBAL_NETWORK_ID;

    /**
     * Up- and downlink module.
     */
    public CUplink uplink;
    public CDownlink downlink;

    /**
     * The uplink direction only allows point-to-point communication (unicast).
     * It is composed of the three nested models for
     * <ul>
     *     <li/> Delay
     *     <li/> Transmission configuration
     *     <li/> Capacity
     * </ul>
     */
    public static class CUplink {
        /**
         * The delay used by {@link CUplink}.
         */
        @JsonAdapter(DelayTypeAdapterFactory.class)
        public Delay delay;
        /**
         * The packet retransmission (here summarized in the delay).
         */
        public CTransmission transmission;
        /**
         * Current capacity.
         */
        @JsonAdapter(CapacityTypeAdapter.class)
        public long capacity;
        /**
         * The maximal Capacity (when no transmission is ongoing).
         */
        public transient long maxCapacity;
    }

    /**
     * The downlink supports two individual paths.
     * <ul>
     *     <li/> Point-to-point communication (unicast)
     *     <li/> Point-to-multipoint communication (multicast)
     * </ul>
     */
    public static class CDownlink {
        /**
         * Point-to-point communication (unicast).
         */
        public CUnicast unicast;
        /**
         * Point-to-multipoint communication (multicast).
         */
        public CMulticast multicast;
        /**
         * Shared capacity between unicast and multicast.
         */
        @JsonAdapter(CapacityTypeAdapter.class)
        public long capacity;
        /**
         * The maximal Capacity (when no transmission is ongoing).
         */
        public transient long maxCapacity;

        public static class CUnicast {
            /**
             * Delay to be used by unicast.
             */
            @JsonAdapter(DelayTypeAdapterFactory.class)
            public Delay delay;
            /**
             * Parameters for Transmission Model to be used by unicast.
             */
            public CTransmission transmission;
        }

        public static class CMulticast {
            /**
             * Delay to be used by multicast.
             */
            @JsonAdapter(DelayTypeAdapterFactory.class)
            public Delay delay;
            /**
             * Parameters for Transmission Model to be used by multicast.
             */
            public CTransmission transmission;
            /**
             * For more detailed modeling of the multicast, the usableCapacity configures
             * the ratio of the overall downlink capacity allowed to be used.
             */
            public float usableCapacity;
        }
    }

    @Override
    public String toString() {
        return String.format("\"%s\" uplink: %s, capacity: %d, "
                        + "downlink {unicast: %s, multicast: %s, usableCapacity: %s, capacity: %d}",
                id, uplink.delay.toString(), uplink.capacity,
                downlink.unicast.delay.toString(), downlink.multicast.delay.toString(),
                downlink.multicast.usableCapacity, downlink.capacity);
    }
}
