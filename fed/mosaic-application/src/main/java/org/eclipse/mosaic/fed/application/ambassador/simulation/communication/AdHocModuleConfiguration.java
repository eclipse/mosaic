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

import org.eclipse.mosaic.lib.enums.AdHocChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration class for the AdHoc module.
 * <br>
 * Single Radio Single Channel:
 * <br>
 * <pre>
 * AdHocModuleConfiguration configuration = new AdHocModuleConfiguration()
 *     .addRadio().power(50).channel(AdHocChannel.CCH).create();
 * </pre>
 * Single Radio Single Channel (with distance instead of power for SNS):
 *  * <br>
 *  * <pre>
 *  * AdHocModuleConfiguration configuration = new AdHocModuleConfiguration()
 *  *     .addRadio().distance(250).channel(AdHocChannel.CCH).create();
 *  * </pre>
 * <br>
 * <br>
 * Single Radio Dual Channel:
 * <br>
 * <pre>
 * AdHocModuleConfiguration configuration = new AdHocModuleConfiguration()
 *      .addRadio().power(50).channel(AdHocChannel.CCH, AdHocChannel.SCH1).create();
 * </pre>
 * <br>
 * <br>
 * Dual Radio Single Channel:
 * <br>
 * <pre>
 * AdHocModuleConfiguration configuration = new AdHocModuleConfiguration()
 *     .addRadio().power(50).channel(AdHocChannel.SCH1).create()
 *     .addRadio().power(50).channel(AdHocChannel.CCH).create();
 * </pre>
 */
public class AdHocModuleConfiguration extends AbstractCommunicationModuleConfiguration {

    /**
     * List of enabled and configured radios (usual cases operate with 1 (single) or 2 (dual) radios).
     */
    private final List<AdHocModuleRadioConfiguration> radios = new ArrayList<>();

    /**
     * Adds a radio to the {@link AdHocModuleConfiguration} using the
     * builder {@link AdHocModuleRadioConfiguration}.
     *
     * @return The {@link AdHocModuleRadioConfiguration} that is build upon.
     */
    public AdHocModuleRadioConfiguration addRadio() {
        if (getNrOfRadios() > 1) {
            throw new RuntimeException("More than 2 radios are not allowed within an AdHoc configuration.");
        }
        return new AdHocModuleRadioConfiguration(this);
    }

    public List<AdHocModuleRadioConfiguration> getRadios() {
        return radios;
    }

    public int getNrOfRadios() {
        return radios.size();
    }

    @Override
    public AdHocModuleConfiguration camMinimalPayloadLength(long minimalPayloadLength) {
        this.camMinimalPayloadLength = minimalPayloadLength;
        return this;
    }

    /**
     * {@link AdHocModuleConfiguration} builder.
     */
    public static class AdHocModuleRadioConfiguration {

        private final AdHocModuleConfiguration parent;

        private double power = -1;  // Default value -1 indicates power configuration through federate
        private Double distance = null;

        private AdHocChannel channel0;
        private AdHocChannel channel1;
        private int nrOfChannels = 0;

        private AdHocModuleRadioConfiguration(AdHocModuleConfiguration parent) {
            this.parent = parent;
        }

        /**
         * Set the transmission power of the configured radio.
         * If a power is given, the distance cannot be set anymore.
         * Not setting the power results in the federate configuration being used.
         *
         * @param power the transmission power in mW
         * @return the current AdHocRadioConfiguration
         */
        public AdHocModuleRadioConfiguration power(double power) {
            if (power < -1) {
                throw new RuntimeException("Negative power is not allowed within an AdHoc configuration.");
            }
            this.power = power;
            return this;
        }

        /**
         * Set the distance that messages transmitted over this radio will travel.
         * If a distance is given, the power cannot be set anymore.
         *
         * @param distance the distance in m
         * @return the current AdHocRadioConfiguration
         */
        public AdHocModuleRadioConfiguration distance(double distance) {
            if (distance < 0) {
                throw new RuntimeException("Negative distance is not allowed within an AdHoc configuration.");
            }
            this.distance = distance;
            return this;
        }

        /**
         * Sets the configured radio to have one channel in single channel mode.
         *
         * @param channel the AdHoc channel that the radio shall stay on
         * @return the current AdHocRadioConfiguration
         */
        public AdHocModuleRadioConfiguration channel(AdHocChannel channel) {
            this.channel0 = channel;
            this.nrOfChannels = 1;
            return this;
        }

        /**
         * Sets the configured radio to have two channels in alternating channel mode.
         *
         * @param channel0 the first channel (normally CCH) that the radio will tune.
         * @param channel1 the second channel that the radio will tune alternatingly with the first channel.
         * @return the current AdHocRadioConfiguration
         */
        public AdHocModuleRadioConfiguration channel(AdHocChannel channel0, AdHocChannel channel1) {
            this.channel0 = channel0;
            this.channel1 = channel1;
            this.nrOfChannels = 2;
            return this;
        }

        public AdHocModuleConfiguration getParent() {
            return parent;
        }

        public double getPower() {
            return power;
        }

        public Double getDistance() {
            return distance;
        }

        public AdHocChannel getChannel0() {
            return channel0;
        }

        public AdHocChannel getChannel1() {
            return channel1;
        }

        public int getNrOfChannels() {
            return nrOfChannels;
        }

        /**
         * Adds the current radio configuration to the AdHocModule configuration and returns the configuration for further
         * settings or to hand it to the AdHocModule.
         *
         * @return the current AdHocConfiguration
         */
        public AdHocModuleConfiguration create() {
            if (channel0 == null) {
                throw new RuntimeException("No channels were given for the AcHocModuleRadioConfiguration.");
            }
            this.parent.radios.add(this);
            return this.parent;
        }
    }
}