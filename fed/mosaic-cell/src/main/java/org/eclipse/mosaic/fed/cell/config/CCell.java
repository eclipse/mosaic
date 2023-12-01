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

package org.eclipse.mosaic.fed.cell.config;

import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.lib.util.gson.DataFieldAdapter;
import org.eclipse.mosaic.rti.DATA;

import com.google.gson.annotations.JsonAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage class for convenient access to the cell configuration (cell_config.json).
 * Provides general configuration for the ambassador, such as paths to the regions and network configuration files.
 */
public final class CCell {

    /**
     * Configuration of header sizes added to all messages before
     * simulating packet transmission.
     */
    public final CHeaderLengths headerLengths = new CHeaderLengths();

    /**
     * Interval (in seconds) in which the bandwidth is aggregated.
     *
     * @see #bandwidthMeasurements
     */
    public int bandwidthMeasurementInterval = 1;

    /**
     * If enabled, the export files with bandwidth measurements will be compressed using gzip compression (default: false).
     *
     * @see #bandwidthMeasurements
     */
    public boolean bandwidthMeasurementCompression = false;

    /**
     * Measure the bandwidth between regions.
     */
    public List<CBandwidthMeasurement> bandwidthMeasurements = new ArrayList<>();

    /**
     * relative path to the network configuration file (default: network.json)
     */
    public String networkConfigurationFile = "network.json";

    /**
     * relative path to the region configuration file (default: regions.json)
     */
    public String regionConfigurationFile = "regions.json";

    @Override
    public String toString() {
        return String.format("networkConfigurationFile: %s, regionConfigurationFile: %s",
                networkConfigurationFile, regionConfigurationFile);
    }

    public static class CBandwidthMeasurement {

        /**
         * Measure the bandwidth of messages which originate in this region (use wildcard * for all regions).
         */
        public String fromRegion;

        /**
         * Measure the bandwidth of messages which target in this region (use wildcard * for all regions).
         */
        public String toRegion;

        /**
         * Defines the transmission mode which is observed.
         */
        public TransmissionMode transmissionMode;

        /**
         * The application class.
         */
        public String applicationClass = "*";
    }

    public static class CHeaderLengths {

        /**
         * The size of all headers of the ethernet link layer (used only for server nodes).
         * E.g. Ethernet + MAC + PHY Header = ~ 38 bytes
         */
        @JsonAdapter(DataFieldAdapter.SizeQuiet.class)
        public long ethernetHeader = 38 * DATA.BYTE;

        /**
         * The size of all headers of the cellular link layer.<br>
         * For example, for LTE we estimate ~20 bytes (PDCP (6 bytes) + RLC (4 bytes) + MAC (10 bytes) + PHY (6 bytes))
         */
        @JsonAdapter(DataFieldAdapter.SizeQuiet.class)
        public long cellularHeader = 26 * DATA.BYTE;

        /**
         * The size of IP header added to all messages.
         */
        @JsonAdapter(DataFieldAdapter.SizeQuiet.class)
        public long ipHeader = 20 * DATA.BYTE;

        /**
         * The size of TCP header added to all messages which use
         * {@link org.eclipse.mosaic.lib.enums.ProtocolType#TCP}
         * for transmission.
         */
        @JsonAdapter(DataFieldAdapter.SizeQuiet.class)
        public long tcpHeader = 20 * DATA.BYTE;

        /**
         * The size of UDP headers added to all messages which use
         * {@link org.eclipse.mosaic.lib.enums.ProtocolType#UDP}
         * for transmission.
         */
        @JsonAdapter(DataFieldAdapter.SizeQuiet.class)
        public long udpHeader = 8 * DATA.BYTE;
    }
}
