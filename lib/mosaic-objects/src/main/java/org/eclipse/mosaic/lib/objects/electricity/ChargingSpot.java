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

package org.eclipse.mosaic.lib.objects.electricity;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Definition of a {@link ChargingSpot}.
 */
public final class ChargingSpot implements Serializable {

    /**
     * The mode of this EV charging spot in compliance with current standards,
     * including IEC 62196-2.
     */
    public enum ChargingMode {
        /**
         * Slow charging through household sockets 3,7kW.
         */
        MODE_1,
        /**
         * One to three-phased charging using coded signal 11kW.
         */
        MODE_2,
        /**
         * Specific plug/socket configurations with pilot- and control-contact 22kW.
         */
        MODE_3,
        /**
         * Fast charging controlled by external charger 44kW.
         */
        MODE_4;

        public static int getVoltage(ChargingMode chargingMode) {
            switch (chargingMode) {
                case MODE_1:
                    return 230;
                case MODE_2:
                case MODE_3:
                case MODE_4:
                    return 400;
                default: // fall back
                    return 0;
            }
        }

        public static int getCurrent(ChargingMode chargingMode) {
            switch (chargingMode) {
                case MODE_1:
                case MODE_2:
                    return 16;
                case MODE_3:
                    return 32;
                case MODE_4:
                    return 63;
                default: // fall back
                    return 0;
            }
        }
    }

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the {@link ChargingSpot}.
     */
    private final String chargingSpotId;

    /**
     * Type of this {@link ChargingSpot} in compliance with current standards, including.
     * <em>IEC 62196-2</em>.
     */
    private final ChargingMode chargingMode;

    /**
     * Flag, indicating if the {@link ChargingSpot} is available.
     */
    private boolean available = true;

    /**
     * Creates a new {@link ChargingSpot} object.
     *
     * @param chargingSpotId Unique identifier of the {@link ChargingSpot}
     * @param type Type of this {@link ChargingSpot} in compliance with current standards,
     *             including <em>IEC 62196-2</em> see {@link ChargingMode}
     */
    public ChargingSpot(String chargingSpotId, ChargingMode type) {
        this.chargingSpotId = chargingSpotId;
        this.chargingMode = type;
    }

    public String getChargingSpotId() {
        return chargingSpotId;
    }

    public ChargingMode getChargingMode() {
        return chargingMode;
    }

    /**
     * Returns the maximum voltage based on the type.
     *
     * @return Maximum voltage available at this {@code ChargingSpot}, unit: [V]
     */
    public int getMaximumVoltage() {
        return ChargingMode.getVoltage(chargingMode);
    }

    /**
     * Returns the maximum current based on the type.
     *
     * @return Maximum current available at this {@code ChargingSpot}, unit: [A]
     */
    public int getMaximumCurrent() {
        return ChargingMode.getCurrent(chargingMode);
    }

    /**
     * Returns the maximum power available at this {@link ChargingSpot}, unit: [W].
     */
    public int getMaximumPower() {
        return getMaximumVoltage() * getMaximumCurrent();
    }

    /**
     * Returns {@code True}, if the {@link ChargingSpot} has at least one parking place available.
     */
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("chargingSpotId", chargingSpotId)
                .append("chargingMode", chargingMode)
                .append("available", available)
                .append("maximumVoltage", getMaximumVoltage())
                .append("maximumCurrent", getMaximumCurrent())
                .append("maximumPower", getMaximumPower())
                .toString();
    }

}
