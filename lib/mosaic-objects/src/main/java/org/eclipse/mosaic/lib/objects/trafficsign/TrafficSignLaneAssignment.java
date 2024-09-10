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

package org.eclipse.mosaic.lib.objects.trafficsign;

import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.util.objects.Position;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Objects;

/**
 * Represents a traffic sign that assigns {@link VehicleClass}es to lanes.
 */
public class TrafficSignLaneAssignment extends TrafficSign<LaneAssignment> {

    private static final long serialVersionUID = 1L;

    public final static String TYPE_ID = TrafficSignLaneAssignment.class.getSimpleName();

    /**
     * Constructs a {@link TrafficSignLaneAssignment}.
     *
     * @param id The id of the lane assignment sign.
     * @param position The position of the sign.
     * @param connectionId The corresponding edge.
     * @param laneAssignments The lane assignments for all lanes.
     */
    public TrafficSignLaneAssignment(String id, Position position, String connectionId, List<LaneAssignment> laneAssignments) {
        super(id, position, connectionId);
        super.addSignContents(laneAssignments);
    }

    /**
     * Sets the lane the sign is valid for.
     */
    @Override
    public TrafficSignLaneAssignment setLane(int lane) {
        super.setLane(lane);
        return this;
    }

    /**
     * Sets the visibility of a traffic sign.
     * Range: [0: not visible, 1: very good visible]
     */
    @Override
    public TrafficSignLaneAssignment setVisibility(double visibility) {
        super.setVisibility(visibility);
        return this;
    }

    /**
     * Sets the angle of a traffic sign.
     * 0.0 is north, 90.0 is east
     */
    @Override
    public TrafficSignLaneAssignment setAngle(double angle) {
        super.setAngle(angle);
        return this;
    }

    /**
     * Sets whether the traffic sign is variable.
     */
    @Override
    public TrafficSignLaneAssignment setVariability(boolean isVariable) {
        super.setVariability(isVariable);
        return this;
    }

    /**
     * Set a new lane assignment for a specific lane.
     *
     * @param lane The lane for which the new lane assignment shall be set.
     * @param allowedVehicleClasses List of allowed vehicle classes.
     */
    public LaneAssignment setLaneAssignment(int lane, List<VehicleClass> allowedVehicleClasses) {
        return new LaneAssignment(lane, allowedVehicleClasses);
    }

    /**
     * Get lane assignments of all lanes.
     */
    public List<LaneAssignment> getLaneAssignments() {
        return getSignContents().stream()
                .map((laneAssignment) -> {
                    try {
                        return laneAssignment.clone();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Gets lane assignments.
     */
    public LaneAssignment getLaneAssignment(int lane) {
        if (lane < 0 || lane >= getSignContents().size()) {
            return null;
        }
        return getSignContents().get(lane);
    }

    /**
     * Sets the list of allowed {@link VehicleClass}es.
     */
    public boolean setAllowedVehicleClasses(int lane, List<VehicleClass> vehicleClasses) {
        if (lane == -1) {
            return setAllowedVehicleClasses(vehicleClasses);
        }
        if (isVariable()) {
            LaneAssignment laneAssignment = getLaneAssignment(lane);
            if (laneAssignment != null) {
                laneAssignment.setAllowedVehicleClasses(vehicleClasses);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets one speed limit for all lanes.
     */
    public boolean setAllowedVehicleClasses(List<VehicleClass> vehicleClasses) {
        if (isVariable()) {
            getSignContents().forEach(s -> s.setAllowedVehicleClasses(vehicleClasses));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a {@link VehicleClass} to the allowed list.
     */
    public boolean addAllowedVehicleClass(int lane, VehicleClass vehicleClass) {
        if (isVariable()) {
            LaneAssignment laneAssignment = getLaneAssignment(lane);
            if (laneAssignment != null && !laneAssignment.getAllowedVehicleClasses().contains(vehicleClass)) {
                laneAssignment.addAllowedVehicleClass(vehicleClass);
            }
        }
        return false;
    }

    /**
     * Removes a {@link VehicleClass} of the allowed list.
     */
    public boolean removeAllowedVehicleClass(int lane, VehicleClass vehicleClass) {
        if (isVariable()) {
            LaneAssignment laneAssignment = getLaneAssignment(lane);
            if (laneAssignment != null) {
                return laneAssignment.removeAllowedVehicleClass(vehicleClass);
            }
        }
        return false;
    }

    /**
     * Blocks the lane(s) for every {@link VehicleClass}.
     */
    public void block(int lane) {
        if (isVariable()) {
            LaneAssignment laneAssignment = getLaneAssignment(lane);
            if (laneAssignment != null) {
                laneAssignment.block();
            }
        }
    }

    /**
     * Opens the lane(s) for every {@link VehicleClass}.
     */
    public void open(int lane) {
        if (isVariable()) {
            LaneAssignment laneAssignment = getLaneAssignment(lane);
            if (laneAssignment != null) {
                laneAssignment.open();
            }
        }
    }

    /**
     * Returns the type of the traffic sign.
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .append("position", getPosition())
                .append("geoPosition", getGeoPosition())
                .append("angle", getAngle())
                .append("connection", getConnectionId())
                .append("lane", getLane())
                .append("isVariable", isVariable())
                .append("visibility", getVisibility())
                .append("laneAssignments", getSignContents())
                .toString();
    }
}
