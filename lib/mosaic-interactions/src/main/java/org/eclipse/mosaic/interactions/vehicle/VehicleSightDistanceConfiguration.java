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

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction}  informs about a vehicles' sight distance.
 */
public final class VehicleSightDistanceConfiguration extends Interaction {

    private static final long serialVersionUID = 1L;

    public final static String TYPE_ID = createTypeIdentifier(VehicleSightDistanceConfiguration.class);

    /**
     * The vehicles name that has sent the interaction.
     */
    private String vehicleId;

    /**
     * The sender vehicles current sight distance.
     */
    private double sightDistance;

    /**
     * The opening angle of the sight area of the vehicle.
     */
    private double openingAngle;

    /**
     * Creates an interaction that includes the sender name (vehicle) and its sight distance (in m).
     *
     * @param time          Timestamp of this interaction, unit: [ns]
     * @param vehicleId     vehicle identifier
     * @param sightDistance sight distance of the vehicle, unit: [m]
     */
    public VehicleSightDistanceConfiguration(long time, String vehicleId, double sightDistance) {
        this(time, vehicleId, sightDistance, 360);
    }

    /**
     * Creates an interaction that includes the sender name (vehicle) and its sight distance (in m).
     *
     * @param time          Timestamp of this interaction, unit: [ns]
     * @param vehicleId     vehicle identifier
     * @param sightDistance sight distance of the vehicle, unit: [m]
     * @param openingAngle  opening angle of the sight area of the vehicle in degrees, unit: [deg]
     */
    public VehicleSightDistanceConfiguration(long time, String vehicleId, double sightDistance, double openingAngle) {
        super(time);
        this.vehicleId = vehicleId;
        this.sightDistance = sightDistance;
        this.openingAngle = openingAngle;
    }

    public double getSightDistance() {
        return sightDistance;
    }

    public double getOpeningAngle() {
        return openingAngle;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 29)
                .append(vehicleId)
                .append(sightDistance)
                .append(openingAngle)
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

        VehicleSightDistanceConfiguration other = (VehicleSightDistanceConfiguration) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.sightDistance, other.sightDistance)
                .append(this.openingAngle, other.openingAngle)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("sightDistance", sightDistance)
                .append("openingAngle", openingAngle)
                .toString();
    }
}
