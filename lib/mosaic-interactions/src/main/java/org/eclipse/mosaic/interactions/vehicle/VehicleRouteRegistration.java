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

package org.eclipse.mosaic.interactions.vehicle;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Extension of {@link Interaction} that propagates information about an added route.
 * Should be used if a new route is created during the simulation.
 */
public final class VehicleRouteRegistration extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleRouteRegistration.class);

    private final VehicleRoute route;

    /**
     * Construct a new {@link VehicleRouteRegistration}.
     *
     * @param time  Timestamp of this interaction, unit: [ns]
     * @param route The route to propagate
     */
    public VehicleRouteRegistration(long time, VehicleRoute route) {
        super(time);
        this.route = route;
    }

    /**
     * Returns the route.
     *
     * @return the route.
     */
    public VehicleRoute getRoute() {
        return route;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 79)
                .append(route)
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

        VehicleRouteRegistration rhs = (VehicleRouteRegistration) obj;
        return new EqualsBuilder()
                .append(this.route, rhs.route)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("route", route)
                .toString();
    }

}
