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
 */

package org.eclipse.mosaic.interactions.electricity;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.electricity.ChargingSpot;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is intended to be used to forward a started charging
 * process at a {@link ChargingSpot} to the RTI.
 */
public final class ChargingStartResponse extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(ChargingStartResponse.class);

    /**
     * String identifying the vehicle that started charging.
     */
    private final String vehicleId;

    /**
     * {@link ChargingSpot} at which the vehicle started charging.
     */
    private final ChargingSpot chargingSpot;

    /**
     * Creates a new {@link ChargingStartResponse} interaction.
     *
     * @param time         Timestamp of this interaction, unit: [ns]
     * @param vehicleId    String identifying the vehicle that started charging
     * @param chargingSpot {@link ChargingSpot} at which the vehicle started charging
     */
    public ChargingStartResponse(long time, String vehicleId, ChargingSpot chargingSpot) {
        super(time);
        this.vehicleId = vehicleId;
        this.chargingSpot = chargingSpot;
    }

    /**
     * Returns the identifier of the vehicle that started charging.
     *
     * @return vehicle identifier
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Returns the {@link ChargingSpot} at which the vehicle started charging.
     *
     * @return the charging spot
     */
    public ChargingSpot getChargingSpot() {
        return chargingSpot;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(9, 23)
                .append(vehicleId)
                .append(chargingSpot)
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

        ChargingStartResponse other = (ChargingStartResponse) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.chargingSpot, other.chargingSpot)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("chargingSpot", chargingSpot)
                .toString();
    }
}
