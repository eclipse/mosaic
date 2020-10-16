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

package org.eclipse.mosaic.fed.cell.viz;

import org.eclipse.mosaic.fed.cell.config.model.CMobileNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;

import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Subclass of {@link StreamListener} which generates instances of {@link PerRegionBandwidthMeasurement}s only if needed.
 * Furthermore, it uses a map to lookup required {@link PerRegionBandwidthMeasurement} instances.
 */
public class OnDemandPerRegionBandwidthMeasurements implements StreamListener {

    private final File parentDir;
    private final List<CMobileNetworkProperties> regions;

    /**
     * Map which contains all registered {@link PerRegionBandwidthMeasurement}, identifying the measurement instance.
     * Key is for example: *, region1, server0
     */
    private final Map<Triple<String, String, String>, PerRegionBandwidthMeasurement> measurementsRegistry = new HashMap<>();

    /**
     * Creates a new {@link OnDemandPerRegionBandwidthMeasurements} object.
     *
     * @param parentDir The parent directory for bandwidth measurements.
     * @param regions   List of the regions.
     */
    public OnDemandPerRegionBandwidthMeasurements(File parentDir, List<CMobileNetworkProperties> regions) {
        this.parentDir = parentDir;
        this.regions = regions;
    }

    @Override
    public void messageSent(StreamParticipant sender, StreamParticipant receiver, StreamProperties properties) {
        measurementsRegistry
                .computeIfAbsent(
                        Triple.of(PerRegionBandwidthMeasurement.WILDCARD_ALL, receiver.getRegion(), properties.getApplicationClass()),
                        this::createUplinkMeasurement
                )
                .messageSent(sender, receiver, properties);

        measurementsRegistry
                .computeIfAbsent(
                        Triple.of(sender.getRegion(), PerRegionBandwidthMeasurement.WILDCARD_ALL, properties.getApplicationClass()),
                        this::createDownlinkMeasurement
                )
                .messageSent(sender, receiver, properties);
    }

    /**
     * Creates a uplink measurement.
     *
     * @param key Triple object contains the input and output region for the message and the application class..
     * @return Bandwidth measurement per region.
     */
    private PerRegionBandwidthMeasurement createUplinkMeasurement(Triple<String, String, String> key) {
        return new PerRegionBandwidthMeasurement(
                parentDir,
                key.getLeft(),
                key.getMiddle(),
                TransmissionMode.UplinkUnicast,
                key.getRight(),
                regions
        );
    }

    /**
     * Creates a downlink measurement.
     *
     * @param key Triple object contains the input and output region for the message and the application class.
     */
    private PerRegionBandwidthMeasurement createDownlinkMeasurement(Triple<String, String, String> key) {
        return new PerRegionBandwidthMeasurement(
                parentDir,
                key.getLeft(),
                key.getMiddle(),
                TransmissionMode.DownlinkUnicast,
                key.getRight(),
                regions
        );
    }

    @Override
    public void finish() {
        measurementsRegistry.forEach((k, v) -> v.finish());
    }
}
