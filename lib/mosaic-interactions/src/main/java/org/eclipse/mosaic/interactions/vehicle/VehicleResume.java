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
 * This extension of {@link Interaction} is intended to be used to forward a request to resume a
 * stopped vehicle to the RTI.
 */
public final class VehicleResume extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(VehicleResume.class);

    /**
     * String identifying the vehicle sending this interaction.
     */
    private final String vehicleId;

    /**
     * Creates a new {@link VehicleResume} interaction.
     *
     * @param time      Timestamp of this interaction, unit: [ns]
     * @param vehicleId String identifying the vehicle sending this interaction
     */
    public VehicleResume(long time, String vehicleId) {
        super(time);
        this.vehicleId = vehicleId;
    }

    /**
     * Getter for the vehicle identifier.
     *
     * @return String identifying the vehicle sending this interaction
     */
    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 67)
                .append(vehicleId)
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

        VehicleResume other = (VehicleResume) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .toString();
    }
}
