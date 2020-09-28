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

package org.eclipse.mosaic.interactions.mapping;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.mapping.VehicleMapping;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * This extension of {@link Interaction} is sent by the mapping ambassador to inform every component
 * about a newly registered vehicle.
 */
public class VehicleRegistration extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleRegistration.class);

    /**
     * Route information of the vehicle.
     */
    private final VehicleDeparture departureInfo;

    /**
     * The new vehicle.
     */
    private final VehicleMapping vehicleMapping;

    /**
     * Creates a new interaction that informs about a new added vehicle to the simulation.
     *
     * @param time          Timestamp of this interaction, unit: [ns]
     * @param name          vehicle identifier
     * @param group         vehicle group identifier
     * @param applications  installed applications of the vehicle
     * @param departureInfo route information of the vehicle
     * @param vehicleType   vehicle type
     */
    public VehicleRegistration(final long time, final String name, final String group, final List<String> applications,
                               final VehicleDeparture departureInfo, final VehicleType vehicleType) {
        super(time);
        this.vehicleMapping = new VehicleMapping(name, group, applications, vehicleType);
        this.departureInfo = departureInfo;
    }

    public VehicleDeparture getDeparture() {
        return departureInfo;
    }

    public VehicleMapping getMapping() {
        return vehicleMapping;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 19)
                .append(departureInfo)
                .append(vehicleMapping)
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

        VehicleRegistration rhs = (VehicleRegistration) obj;
        return new EqualsBuilder()
                .append(this.departureInfo, rhs.departureInfo)
                .append(this.vehicleMapping, rhs.vehicleMapping)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("routeInfo", departureInfo)
                .append("vehicleMapping", vehicleMapping)
                .toString();
    }
}
