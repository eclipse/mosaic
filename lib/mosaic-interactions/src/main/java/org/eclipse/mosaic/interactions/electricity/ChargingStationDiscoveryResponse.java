/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

import java.util.Collection;

/**
 * An {@link Interaction} that contains a list of {@link ChargingStationData} objects as a response to a {@link VehicleChargingStationDiscoveryRequest}.
 */
public class ChargingStationDiscoveryResponse extends Interaction {
    public ChargingStationDiscoveryResponse(long time, String vehicleId, Collection<ChargingStationData> chargingStations) {
        super(time);

        this.chargingStations = chargingStations;
        this.vehicleId = vehicleId;
    }

    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(ChargingStationDiscoveryResponse.class);

    /**
     * The queried list {@link ChargingStationData} originating from the {@link VehicleChargingStationDiscoveryRequest}.
     */
    private final Collection<ChargingStationData> chargingStations;

    /**
     * The ID of the requesting vehicle.
     */
    private final String vehicleId;

    public String getVehicleId() {
        return vehicleId;
    }

    public Collection<ChargingStationData> getChargingStations() {
        return chargingStations;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(9, 97)
                .append(vehicleId)
                .append(chargingStations)
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

        ChargingStationDiscoveryResponse other = (ChargingStationDiscoveryResponse) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.chargingStations, other.chargingStations)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("chargingStations", chargingStations.stream().map(ChargingStationData::toString))
                .toString();
    }

}
