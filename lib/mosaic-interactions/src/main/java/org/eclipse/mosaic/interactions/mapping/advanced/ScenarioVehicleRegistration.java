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

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is sent by SUMO ambassador if it launches vehicles by
 * itself (e.g. by using predefined scenarios).
 */
public final class ScenarioVehicleRegistration extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(ScenarioVehicleRegistration.class);

    private final String name;
    private final String vehicleTypeId;

    /**
     * Creates a new interaction that informs that a vehicle was added to the simulation by SUMO.
     *
     * @param time          Timestamp of this interaction, unit: [ns]
     * @param name          name of the vehicle
     * @param vehicleTypeId name of the vehicle type
     */
    public ScenarioVehicleRegistration(final long time, final String name, final String vehicleTypeId) {
        super(time);
        this.name = name;
        this.vehicleTypeId = vehicleTypeId;
    }

    public String getName() {
        return name;
    }

    public String getVehicleTypeId() {
        return vehicleTypeId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 19)
                .append(name)
                .append(vehicleTypeId)
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

        ScenarioVehicleRegistration other = (ScenarioVehicleRegistration) obj;
        return new EqualsBuilder()
                .append(this.name, other.name)
                .append(this.vehicleTypeId, other.vehicleTypeId)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("name", name)
                .append("vehicleTypeId", vehicleTypeId)
                .toString();
    }

}
