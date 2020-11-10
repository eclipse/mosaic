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

package org.eclipse.mosaic.fed.application.ambassador.simulation.communication;

import org.eclipse.mosaic.fed.application.app.api.communication.CommunicationModuleConfiguration;
import org.eclipse.mosaic.lib.enums.DestinationType;
import org.eclipse.mosaic.rti.DATA;

/**
 * <pre>
 * CellModuleConfiguration cellConfiguration = new CellModuleConfiguration()
 *     .maxDlBitrate(7200000000)
 *     .maxUlBitrate(1400000000)
 *     .camConfiguration(new CAMConfiguration(DestinationType.CellGeoUnicast, 300));
 * </pre>
 */
public class CellModuleConfiguration implements CommunicationModuleConfiguration {

    private final static long DEFAULT_MAX_DL_BITRATE = 900 * DATA.MEGABYTE;
    private final static long DEFAULT_MAX_UL_BITRATE = 175 * DATA.MEGABYTE;
    /**
     * DL/UL bitrates to reflect a data plan from a certain provider.
     * (intended to be used by Cell)
     */
    private Long maxDlBitrate;
    private Long maxUlBitrate;
    /**
     * Configuration for CAM messaging over cellular communication.
     * (intended for application ambassador)
     */
    private CellCamConfiguration camConfiguration = null;

    public CellModuleConfiguration maxDlBitrate(long bitrate) {
        this.maxDlBitrate = bitrate;
        return this;
    }

    public CellModuleConfiguration maxUlBitrate(long bitrate) {
        this.maxUlBitrate = bitrate;
        return this;
    }

    /**
     * Convenience method creating CAM config using default bitrates.
     *
     * @param camReceiver id of receiving entity
     * @return the built {@link CellModuleConfiguration}
     */
    public CellModuleConfiguration camConfigurationTopocast(String camReceiver) {
        useDefaultBitrates();
        this.camConfiguration = new CellCamConfiguration(camReceiver);
        return this;
    }

    /**
     * Convenience method creating CAM config using default bitrates.
     *
     * @param addressingMode addressing mode to be used
     * @param geoRadius      reception radius for CAM
     * @return the built {@link CellModuleConfiguration}
     */
    public CellModuleConfiguration camConfiguration(DestinationType addressingMode, double geoRadius) {
        useDefaultBitrates();
        this.camConfiguration = new CellCamConfiguration(addressingMode, geoRadius);
        return this;
    }

    /**
     * Convenience method creating CAM config using default bitrates.
     *
     * @param geoRadius reception radius for CAM
     * @return the built {@link CellModuleConfiguration}
     */
    public CellModuleConfiguration camConfiguration(double geoRadius) {
        useDefaultBitrates();
        this.camConfiguration = new CellCamConfiguration(DestinationType.CELL_GEOCAST, geoRadius);
        return this;
    }

    public Long getMaxDlBitrate() {
        return maxDlBitrate;
    }

    public Long getMaxUlBitrate() {
        return maxUlBitrate;
    }

    public CellCamConfiguration getCamConfiguration() {
        return camConfiguration;
    }

    private void useDefaultBitrates() {
        maxDlBitrate = DEFAULT_MAX_DL_BITRATE;
        maxUlBitrate = DEFAULT_MAX_UL_BITRATE;
    }

    /**
     * Configuration of disseminationMode (multiple unicasts or one multicast)
     * and information distance to neighbors in the vicinity.
     */
    public static class CellCamConfiguration {
        final DestinationType addressingMode;
        double geoRadius;
        String topocastReceiver;

        public CellCamConfiguration(DestinationType addressingMode, double geoRadius) {
            this.addressingMode = addressingMode;
            this.geoRadius = geoRadius;
        }

        public CellCamConfiguration(String topocastReceiver) {
            this.addressingMode = DestinationType.CELL_TOPOCAST;
            this.topocastReceiver = topocastReceiver;
        }

        public DestinationType getAddressingMode() {
            return addressingMode;
        }

        public double getGeoRadius() {
            return geoRadius;
        }

        public String getTopocastReceiver() {
            return topocastReceiver;
        }
    }
}
