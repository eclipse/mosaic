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

package org.eclipse.mosaic.interactions.environment;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is intended to be used to signal
 * interest in sensor information for a specific node.
 */
public final class EnvironmentSensorActivation extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(EnvironmentSensorActivation.class);

    /**
     * String identifying a simulated vehicle.
     */
    private final String vehicleId;

    /**
     * Constructor for the interaction.
     *
     * @param time      Timestamp of this interaction, unit: [ns]
     * @param vehicleId The payload of the interaction, a vehicleId.
     */
    public EnvironmentSensorActivation(long time, String vehicleId) {
        super(time);
        this.vehicleId = vehicleId;
    }

    /**
     * Getter for vehicle id.
     *
     * @return vehicle identifier
     */
    public String getVehicleId() {
        return this.vehicleId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 41)
                .append(getId())
                .append(getTime())
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

        EnvironmentSensorActivation other = (EnvironmentSensorActivation) obj;
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
