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

import org.eclipse.mosaic.lib.objects.vehicle.VehicleParameter;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This extension of {@link Interaction} stores a list of {@link VehicleParameter}s,
 * that shall be changed of a specific vehicle.
 */
public final class VehicleParametersChange extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleParametersChange.class);

    /**
     * Vehicle identifier.
     */
    private final String vehicleId;

    /**
     * List of parameters o be changed.
     */
    private final Collection<VehicleParameter> vehicleParameters;

    /**
     * Creates a new {@link VehicleParametersChange} interaction.
     *
     * @param time               Timestamp of this interaction, unit: [ns]
     * @param vehicleId          vehicle identifier
     * @param parametersToChange lit of vehicle parameters to be changed
     */
    public VehicleParametersChange(final long time, final String vehicleId, Collection<VehicleParameter> parametersToChange) {
        super(time);
        this.vehicleId = vehicleId;
        this.vehicleParameters = new ArrayList<>(parametersToChange);
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public Collection<VehicleParameter> getVehicleParameters() {
        return vehicleParameters;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 53)
                .append(vehicleId)
                .append(vehicleParameters)
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

        VehicleParametersChange other = (VehicleParametersChange) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.vehicleParameters, other.vehicleParameters)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("vehicleParameters", vehicleParameters)
                .toString();
    }
}
