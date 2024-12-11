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

import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;

/**
 * An {@link Interaction} used by electric vehicles to find charging stations ({@link org.eclipse.mosaic.lib.objects.electricity.ChargingStationData}) in a certain area.
 */
public class VehicleChargingStationDiscoveryRequest extends Interaction {
    public VehicleChargingStationDiscoveryRequest(long time, String vehicleId, GeoCircle searchArea) {
        super(time);

        this.vehicleId = vehicleId;
        this.searchArea = searchArea;
    }

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(VehicleChargingStationDiscoveryRequest.class);

    /**
     * String identifying the vehicle sending this interaction.
     */
    private final String vehicleId;

    /**
     * An area given by a point and a radius in which the requested {@link org.eclipse.mosaic.lib.objects.electricity.ChargingStationData} objects are.
     */
    private final GeoCircle searchArea;

    public String getVehicleId() {
        return vehicleId;
    }

    public GeoCircle getSearchArea() {
        return searchArea;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(9, 97)
                .append(vehicleId)
                .append(searchArea)
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

        VehicleChargingStationDiscoveryRequest other = (VehicleChargingStationDiscoveryRequest) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.searchArea, other.searchArea)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("searchArea", searchArea)
                .toString();
    }
}
