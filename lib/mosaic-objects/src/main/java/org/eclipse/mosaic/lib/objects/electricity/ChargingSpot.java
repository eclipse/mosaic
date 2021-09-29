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

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the {@link ChargingSpot}.
     */
    private final String chargingSpotId;

    /**
     * Type of this charging this {@link ChargingSpot} supports.
     */
    private final ChargingType chargingType;

    /**
     * Maximum voltage available at this {@code ChargingSpot}. [V]
     */
    private final double maxVoltage;

    /**
     * Maximum current available at this {@code ChargingSpot}. [A]
     */
    private final double maxCurrent;

    /**
     * Flag, indicating if the {@link ChargingSpot} is available.
     */
    private boolean available = true;

    /**
     * Creates a new {@link ChargingSpot} object.
     *
     * @param chargingSpotId Unique identifier of the {@link ChargingSpot}
     * @param type           Type of this {@link ChargingSpot} in compliance with current standards,
     *                       including <em>IEC 62196-2</em> see {@link ChargingType}
     */
    public ChargingSpot(String chargingSpotId, ChargingType type, double maxVoltage, double maxCurrent) {
        this.chargingSpotId = chargingSpotId;
        this.chargingType = type;
        this.maxVoltage = maxVoltage;
        this.maxCurrent = maxCurrent;
    }

    public String getChargingSpotId() {
        return chargingSpotId;
    }

    public ChargingType getChargingType() {
        return chargingType;
    }

    public double getMaximumVoltage() {
        return maxVoltage;
    }

    public double getMaximumCurrent() {
        return maxCurrent;
    }

    /**
     * Returns the maximum power available at this {@link ChargingSpot}, unit: [W].
     */
    public double getMaximumPower() {
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
                .append("chargingType", chargingType)
                .append("available", available)
                .append("maximumVoltage", getMaximumVoltage())
                .append("maximumCurrent", getMaximumCurrent())
                .append("maximumPower", getMaximumPower())
                .toString();
    }

}
