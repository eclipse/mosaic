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

package org.eclipse.mosaic.interactions.trafficsigns;

import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * This interaction can be sent to TrafficSignAmbassador in order to change a variable lane assignment sign.
 */
public final class TrafficSignLaneAssignmentChange extends Interaction {

    private static final long serialVersionUID = 1L;

    public final static String TYPE_ID = createTypeIdentifier(TrafficSignLaneAssignmentChange.class);

    /**
     * Lane assignment sign identifier.
     */
    private final String laneAssignmentSignId;

    /**
     * The list of all allowed vehicle classes.
     */
    private final List<VehicleClass> allowedVehicleClasses = new ArrayList<>();

    /**
     * The index of the lane the sign is valid for.
     * If it is set to -1, the sign is valid for all lanes.
     */
    private int lane = -1;

    /**
     * Creates a new interaction that changes the list of allowed vehicle classes of a variable lane assignment limit sign.
     *
     * @param time                  Timestamp of this interaction, unit: [ns]
     * @param signId                traffic sign identifier
     * @param lane                  index of the lane the sign is valid for
     * @param allowedVehicleClasses list of all allowed vehicle classes
     */
    public TrafficSignLaneAssignmentChange(long time, String signId, int lane, List<VehicleClass> allowedVehicleClasses) {
        super(time);
        this.lane = lane;
        this.laneAssignmentSignId = signId;
        this.allowedVehicleClasses.addAll(allowedVehicleClasses);
    }

    /**
     * Creates a new interaction that changes the list of allowed vehicle classes of a variable lane assignment limit sign.
     *
     * @param time                  Timestamp of this interaction, unit: [ns]
     * @param signId                traffic sign identifier
     * @param allowedVehicleClasses list of all allowed vehicle classes
     */
    public TrafficSignLaneAssignmentChange(long time, String signId, List<VehicleClass> allowedVehicleClasses) {
        super(time);
        this.laneAssignmentSignId = signId;
        this.allowedVehicleClasses.addAll(allowedVehicleClasses);
    }

    public List<VehicleClass> getAllowedVehicleClasses() {
        return allowedVehicleClasses;
    }

    public int getLane() {
        return lane;
    }

    public String getTrafficSignId() {
        return laneAssignmentSignId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 71)
                .append(laneAssignmentSignId)
                .append(lane)
                .append(allowedVehicleClasses)
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

        TrafficSignLaneAssignmentChange other = (TrafficSignLaneAssignmentChange) obj;
        return new EqualsBuilder()
                .append(this.laneAssignmentSignId, other.laneAssignmentSignId)
                .append(this.lane, other.lane)
                .append(this.allowedVehicleClasses, other.allowedVehicleClasses)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("laneAssignmentSignId", laneAssignmentSignId)
                .append("lane", lane)
                .append("allowedVehicleClasses", allowedVehicleClasses)
                .toString();
    }
}
