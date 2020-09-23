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
 * This extension of {@link Interaction} is intended for the
 * Phabmacs + SUMO Controlling Ambassador (PhaSCA) to notify SUMO and
 * Phabmacs of any vehicles that are simulated externally.
 */
public final class VehicleFederateAssignment extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleFederateAssignment.class);

    /**
     * String identifying a simulated vehicle the interaction applies to.
     */
    private final String vehicleId;

    /**
     * String identifying, which simulator this interaction is intended for.
     */
    private final String assignedFederateId;

    /**
     * The radius around the ego vehicle for which vehicle data is to be sent (ICOS only).
     */
    private final double surroundingVehiclesRadius;

    /**
     * Constructor for {@link VehicleFederateAssignment}.
     *
     * @param time                      The current time at creation of the interaction object
     * @param vehicleId                 String identifying a simulated vehicle the interaction applies to
     * @param assignedFederateId        String identifying which simulator this interaction is intended for
     * @param surroundingVehiclesRadius The radius around the ego vehicle for which vehicle data is to be sent
     */
    public VehicleFederateAssignment(long time, String vehicleId, String assignedFederateId, double surroundingVehiclesRadius) {
        super(time);
        this.vehicleId = vehicleId;
        this.assignedFederateId = assignedFederateId;
        this.surroundingVehiclesRadius = surroundingVehiclesRadius;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getAssignedFederate() {
        return assignedFederateId;
    }

    public double getSurroundingVehiclesRadius() {
        return surroundingVehiclesRadius;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 23)
                .append(vehicleId)
                .append(surroundingVehiclesRadius)
                .append(assignedFederateId)
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

        VehicleFederateAssignment other = (VehicleFederateAssignment) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.assignedFederateId, other.assignedFederateId)
                .append(this.surroundingVehiclesRadius, other.surroundingVehiclesRadius)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("assignedFederateId", assignedFederateId)
                .append("surroundingVehiclesRadius", surroundingVehiclesRadius)
                .toString();
    }

}
