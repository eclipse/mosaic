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

import org.eclipse.mosaic.lib.util.objects.Position;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Objects;

/**
 * Represents a traffic speed sign.
 */
public class TrafficSignSpeed extends TrafficSign<SpeedLimit> {

    private static final long serialVersionUID = 1L;

    public final static String TYPE_ID = TrafficSignSpeed.class.getSimpleName();

    /**
     * The default edge speed.
     */
    private double defaultEdgeSpeedInMs = -1;

    /**
     * Construct a {@link TrafficSignSpeed}.
     *
     * @param id          The id if the speed limit sign.
     * @param position    The position of the speed limit sign.
     * @param edge        The corresponding edge id.
     * @param speedLimits The list of speed limits for all lanes of the edge.
     */
    public TrafficSignSpeed(String id, Position position, String edge, List<SpeedLimit> speedLimits) {
        super(id, position, edge);
        this.addSignContents(speedLimits);
    }

    /**
     * Sets the lane the speed sign is valid for.
     */
    @Override
    public TrafficSignSpeed setLane(int lane) {
        super.setLane(lane);
        return this;
    }

    /**
     * Sets the visibility of a traffic sign.
     * Range [0: not visible, 1: very good visible]
     */
    @Override
    public TrafficSignSpeed setVisibility(double visibility) {
        super.setVisibility(visibility);
        return this;
    }

    /**
     * Sets the angle of a traffic sign.
     * 0.0 is north, 90.0 is east
     */
    @Override
    public TrafficSignSpeed setAngle(double angle) {
        super.setAngle(angle);
        return this;
    }

    /**
     * Sets whether the traffic sign is variable.
     */
    @Override
    public TrafficSignSpeed setVariability(boolean isVariable) {
        super.setVariability(isVariable);
        return this;
    }

    /**
     * Returns the type id of the traffic sign.
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    /**
     * Returns speed limits of all lanes.
     */
    public List<SpeedLimit> getSpeedLimits() {
        return getSignContents().stream()
                .map((speedLimit) -> {
                    try {
                        return speedLimit.clone();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Returns the speed limit of the lane with index {@param lane}.
     */
    public SpeedLimit getSpeedLimit(int lane) {
        if (lane < 0 || lane >= getSignContents().size()) {
            return null;
        }
        return getSignContents().get(lane);
    }

    /**
     * Changes the speed signs speed limit, if the sign is variable.
     *
     * @param lane       The lane on which the new speed limit shall be set.
     * @param speedLimit The new speed limit.
     */
    public boolean setSpeedLimit(int lane, double speedLimit) {
        if (lane == -1) {
            return setSpeedLimit(speedLimit);
        }
        if (isVariable() && getSignContents().get(lane) != null) {
            getSignContents().get(lane).setSpeedLimit(speedLimit);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets one speed limit for all lanes.
     *
     * @param speedLimit The new speed limit.
     */
    public boolean setSpeedLimit(double speedLimit) {
        if (isVariable()) {
            getSignContents().forEach(s -> s.setSpeedLimit(speedLimit));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets the default edge speed limit [m/s].
     *
     * @param defaultEdgeSpeedInMs Default edge speed limit in [m/s].
     */
    public void setDefaultEdgeSpeedInMs(double defaultEdgeSpeedInMs) {
        this.defaultEdgeSpeedInMs = defaultEdgeSpeedInMs;
    }

    /**
     * Returns the default edge speed limit.
     * If not set, <code>-1</code> is returned.
     */
    public double getDefaultEdgeSpeedInMs() {
        return defaultEdgeSpeedInMs;
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
                .append("speedLimits", getSignContents())
                .append("defaultEdgeSpeed", defaultEdgeSpeedInMs)
                .toString();
    }
}
