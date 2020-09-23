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

package org.eclipse.mosaic.interactions.traffic;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * This extension of {@link Interaction} is sent by navigation ambassador, after vehicle route generation.
 * It contains routes and their IDs.
 */
public final class VehicleRoutesInitialization extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleRoutesInitialization.class);

    /**
     * HashMap containing vehicle routes and its IDs.
     */
    private final Map<String, VehicleRoute> routes;

    /**
     * Creates a new VehicleRoutesInitialization.
     *
     * @param time   Timestamp of this interaction, unit: [ns]
     * @param routes vehicle routes as a list of nodes
     */
    public VehicleRoutesInitialization(long time, Map<String, VehicleRoute> routes) {
        super(time);
        this.routes = routes;
    }

    public Map<String, VehicleRoute> getRoutes() {
        return this.routes;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 97)
                .append(routes)
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

        VehicleRoutesInitialization other = (VehicleRoutesInitialization) obj;
        return new EqualsBuilder()
                .append(this.routes, other.routes)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("routes", routes)
                .toString();
    }
}
