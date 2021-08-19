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

package org.eclipse.mosaic.interactions.electricity;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.electricity.ChargingSpot;
import org.eclipse.mosaic.lib.objects.electricity.ChargingType;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is intended to be used to forward a started charging
 * process at a {@link ChargingSpot} to the RTI.
 */
public final class BatteryChargingStart extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(BatteryChargingStart.class);

    /**
     * String identifying the vehicle that started charging.
     */
    private final String vehicleId;

    /**
     * The voltage available for the charging process. [V]
     */
    private final double voltage;

    /**
     * The voltage available for the charging process. [A]
     */
    private final double current;

    /**
     * The charging type to be used for charging.
     */
    private final ChargingType chargingType;

    /**
     * Creates a new {@link BatteryChargingStart} interaction.
     *
     * @param time      Timestamp of this interaction, unit: [ns]
     * @param vehicleId String identifying the vehicle that started charging
     */
    public BatteryChargingStart(long time, String vehicleId, double voltage, double current, ChargingType chargingType) {
        super(time);
        this.vehicleId = vehicleId;
        this.voltage = voltage;
        this.current = current;
        this.chargingType = chargingType;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public double getVoltage() {
        return voltage;
    }

    public double getCurrent() {
        return current;
    }

    public ChargingType getChargingType() {
        return chargingType;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(9, 23)
                .append(vehicleId)
                .append(voltage)
                .append(current)
                .append(chargingType)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        BatteryChargingStart other = (BatteryChargingStart) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.voltage, other.voltage)
                .append(this.current, other.current)
                .append(this.chargingType, other.chargingType)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("voltage", voltage)
                .append("current", current)
                .append("chargingType", chargingType)
                .toString();
    }
}
