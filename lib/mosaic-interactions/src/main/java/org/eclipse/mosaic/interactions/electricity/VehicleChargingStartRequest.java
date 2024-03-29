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

import org.eclipse.mosaic.lib.objects.electricity.ChargingStationData;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is intended to be used to forward a request from a vehicle
 * to start charging its battery at a {@link ChargingStationData} to the RTI.
 */
public final class VehicleChargingStartRequest extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(VehicleChargingStartRequest.class);

    /**
     * String identifying the vehicle sending this interaction.
     */
    private final String vehicleId;

    /**
     * The identifier of the charging station.
     */
    private final String chargingStationId;

    /**
     * Optional identifier of the exact charging spot.
     */
    private final String chargingSpotId;

    /**
     * Creates a new {@link VehicleChargingStartRequest} interaction.
     *
     * @param time      Timestamp of this interaction, unit: [ns]
     * @param vehicleId String identifying the vehicle sending this interaction
     * @param chargingStationId id of the charging station the vehicle wants to dock at
     */
    public VehicleChargingStartRequest(long time, String vehicleId, String chargingStationId) {
        this(time, vehicleId, chargingStationId, null);
    }

    /**
     * Creates a new {@link VehicleChargingStartRequest} interaction.
     *
     * @param time      Timestamp of this interaction, unit: [ns]
     * @param vehicleId String identifying the vehicle sending this interaction
     * @param chargingStationId id of the charging station the vehicle wants to dock at
     * @param chargingSpotId id of the charging spot the vehicle wants to dock at, can only be used in combination with chargingStationId
     */
    public VehicleChargingStartRequest(long time, String vehicleId, String chargingStationId, String chargingSpotId) {
        super(time);
        this.vehicleId = vehicleId;
        this.chargingStationId = chargingStationId;
        this.chargingSpotId = chargingSpotId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getChargingStationId() {
        return chargingStationId;
    }

    public String getChargingSpotId() {
        return chargingSpotId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(9, 97)
                .append(vehicleId)
                .append(chargingStationId)
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

        VehicleChargingStartRequest other = (VehicleChargingStartRequest) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.chargingStationId, other.chargingStationId)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("chargingStationId", chargingStationId)
                .toString();
    }

}