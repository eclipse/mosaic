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

package org.eclipse.mosaic.interactions.vehicle;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} changes the vehicles route.
 * This route has to be computed and distributed before.
 *
 * @see VehicleRouteRegistration
 */
public final class VehicleRouteChange extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleRouteChange.class);

    /**
     * Integer identifying a simulated vehicle.
     */
    private final String vehicleId;

    /**
     * Integer identifying the chosen path/route/way.
     */
    private final String routeId;

    /**
     * Constructor for a {@link VehicleRouteChange}.
     *
     * @param time      Timestamp of this interaction, unit: [ns]
     * @param vehicleId The vehicleId of the vehicle, which route should be changed.
     * @param routeId   The id of the route to change to.
     */
    public VehicleRouteChange(long time, String vehicleId, String routeId) {
        super(time);
        this.vehicleId = vehicleId;
        this.routeId = routeId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getRouteId() {
        return routeId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 29)
                .append(vehicleId)
                .append(routeId)
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

        VehicleRouteChange rhs = (VehicleRouteChange) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, rhs.vehicleId)
                .append(this.routeId, rhs.routeId)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("routeId", routeId)
                .toString();
    }
}
