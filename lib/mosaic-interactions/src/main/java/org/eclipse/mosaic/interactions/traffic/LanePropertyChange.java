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

import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * This extension of {@link Interaction} contains lane properties to be changed.
 * Concretely, it sets a list of allowed and disallowed vehicle classes
 * per lane and a new maximum speed limit that shall be changed.
 */
public final class LanePropertyChange extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(LanePropertyChange.class);

    /**
     * The edge identifier.
     */
    private final String edgeId;

    /**
     * The lane index.
     */
    private final int laneIndex;

    /**
     * List of all allowed vehicle classes.
     */
    private final List<VehicleClass> allowedVehicleClasses;

    /**
     * List of all disallowed vehicle classes.
     */
    private final List<VehicleClass> disallowedVehicleClasses;

    /**
     * Maximum speed limit in m/s.
     */
    private final Double maxSpeed;

    /**
     * Creates a new interaction with lane properties that shall be changed.
     *
     * @param time                     Timestamp of this interaction, unit: [ns]
     * @param edgeId                   edge identifier
     * @param laneIndex                lane index
     * @param allowedVehicleClasses    list of allowed vehicle classes
     * @param disallowedVehicleClasses list of disallowed vehicle classes
     * @param maxSpeed                 the new maximum speed in m/s
     */
    public LanePropertyChange(long time, String edgeId, int laneIndex, List<VehicleClass> allowedVehicleClasses,
                              List<VehicleClass> disallowedVehicleClasses, Double maxSpeed) {
        super(time);
        this.edgeId = edgeId;
        this.laneIndex = laneIndex;
        this.allowedVehicleClasses = allowedVehicleClasses;
        this.disallowedVehicleClasses = disallowedVehicleClasses;
        this.maxSpeed = maxSpeed;
    }

    public String getEdgeId() {
        return edgeId;
    }

    public int getLaneIndex() {
        return laneIndex;
    }

    public List<VehicleClass> getDisallowedVehicleClasses() {
        return disallowedVehicleClasses;
    }

    public List<VehicleClass> getAllowedVehicleClasses() {
        return allowedVehicleClasses;
    }

    public Double getMaxSpeed() {
        return maxSpeed;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 37)
                .append(edgeId)
                .append(laneIndex)
                .append(disallowedVehicleClasses)
                .append(allowedVehicleClasses)
                .append(maxSpeed)
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

        LanePropertyChange other = (LanePropertyChange) obj;
        return new EqualsBuilder()
                .append(this.edgeId, other.edgeId)
                .append(this.laneIndex, other.laneIndex)
                .append(this.allowedVehicleClasses, other.allowedVehicleClasses)
                .append(this.disallowedVehicleClasses, other.disallowedVehicleClasses)
                .append(this.maxSpeed, other.maxSpeed)
                .isEquals();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("edgeId", edgeId)
                .append("laneIndex", laneIndex)
                .append("allowedVehicleClasses", allowedVehicleClasses)
                .append("disallowedVehicleClasses", disallowedVehicleClasses)
                .append("maxSpeed", maxSpeed)
                .toString();
    }
}
