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

import org.eclipse.mosaic.lib.objects.vehicle.VehicleBatteryState;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;

/**
 * This extension of {@link Interaction} is used to inform the applications of simulation units about
 * changed {@link VehicleBatteryState}.
 */
public final class VehicleElectricityUpdates extends Interaction {

    private static final long serialVersionUID = 1L;

    public final static String TYPE_ID = createTypeIdentifier(VehicleElectricityUpdates.class);

    /**
     * The updated list of electricity information for each simulated electric vehicles.
     */
    private final Collection<VehicleBatteryState> updated;

    /**
     * Creates a new {@link VehicleElectricityUpdates} interaction.
     *
     * @param time    Timestamp of this interaction, unit: [ns]
     * @param updated The payload of the interaction, a list of {@link VehicleBatteryState}.
     */
    public VehicleElectricityUpdates(long time, Collection<VehicleBatteryState> updated) {
        super(time);
        this.updated = updated;
    }

    public Collection<VehicleBatteryState> getUpdated() {
        return this.updated;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 41)
                .append(updated)
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

        VehicleElectricityUpdates other = (VehicleElectricityUpdates) obj;
        return new EqualsBuilder()
                .append(this.updated, other.updated)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("updated", updated)
                .toString();
    }
}