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

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is sent out by the Charging Station Ambassador to inform the
 * Application Simulator (the vehicles) when a charging station is already in use.
 * e.g. a vehicle wants to start charging on an engaged charging station then the charging request gets rejected
 */
public final class ChargingDenialResponse extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(ChargingDenialResponse.class);

    /**
     * String identifying the vehicle sending this interaction.
     */
    private final String vehicleId;

    /**
     * The id of the charging station which denied the charging, e.g. if it has no free spots.
     */
    private final String chargingStationId;

    /**
     * Is {@code true}, if there is a free spot at the station, which is already reserved for another vehicle.
     */
    private final boolean reservedUnoccupiedSpot;

    /**
     * Creates a new {@link ChargingDenialResponse} message that is sent to a vehicle that requested to charge at this station.
     *
     * @param time                   Timestamp of this interaction, unit: [ns]
     * @param vehicleId              Vehicle identifier
     * @param chargingStationId      Charging station identifier
     * @param reservedUnoccupiedSpot If {@code true} there exists an unoccupied spot, which is reserved for another vehicle.
     */
    public ChargingDenialResponse(long time, String vehicleId, String chargingStationId, boolean reservedUnoccupiedSpot) {
        super(time);
        this.vehicleId = vehicleId;
        this.chargingStationId = chargingStationId;
        this.reservedUnoccupiedSpot = reservedUnoccupiedSpot;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getChargingStationId() {
        return chargingStationId;
    }

    public boolean isReservedUnoccupiedSpot() {
        return this.reservedUnoccupiedSpot;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 59)
                .append(getChargingStationId())
                .append(getVehicleId())
                .append(isReservedUnoccupiedSpot())
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

        ChargingDenialResponse other = (ChargingDenialResponse) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.chargingStationId, other.chargingStationId)
                .append(this.reservedUnoccupiedSpot, other.reservedUnoccupiedSpot)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("chargingStationId", chargingStationId)
                .append("reservedUnoccupiedSpot", reservedUnoccupiedSpot)
                .toString();
    }
}
