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

package org.eclipse.mosaic.interactions.mapping.advanced;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.lib.objects.mapping.OriginDestinationPair;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * This extension of {@link Interaction} is sent by mapping to the
 * navigation-system to later calculate the actual {@link VehicleRegistration}-interaction.
 * The added vehicles described by these interactions travel from one defined point to another.
 */
public final class RoutelessVehicleRegistration extends VehicleRegistration {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(RoutelessVehicleRegistration.class);

    /**
     * The trip consisting of the origin and the destination for the vehicle.
     */
    private final OriginDestinationPair trip;

    /**
     * Constructor for {@link RoutelessVehicleRegistration}.
     *
     * @param time         Timestamp of this interaction, unit: [ns]
     * @param name         vehicle identifier
     * @param group        vehicle group identifier
     * @param applications installed applications of the vehicle
     * @param departure    departure information for the vehicle
     * @param vehicleType  vehicle type
     * @param trip         origin-destination information for calculation of route.
     */
    public RoutelessVehicleRegistration(final long time, final String name, final String group, final List<String> applications,
                                        final VehicleDeparture departure, final VehicleType vehicleType, final OriginDestinationPair trip) {
        super(time, name, group, applications, departure, vehicleType);
        this.trip = trip;
    }

    public OriginDestinationPair getTrip() {
        return trip;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 53)
                .appendSuper(super.hashCode())
                .append(trip)
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

        RoutelessVehicleRegistration other = (RoutelessVehicleRegistration) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(this.trip, other.trip)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("trip", trip)
                .toString();
    }

}
