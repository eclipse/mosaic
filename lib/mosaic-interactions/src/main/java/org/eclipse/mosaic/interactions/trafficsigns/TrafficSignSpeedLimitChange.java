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

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This interaction can be sent to TrafficSignAmbassador in order to change a variable speed sign.
 */
public final class TrafficSignSpeedLimitChange extends Interaction {

    public final static String TYPE_ID = createTypeIdentifier(TrafficSignSpeedLimitChange.class);

    /**
     * Identifier of the variable speed sign.
     */
    private final String speedSignId;

    /**
     * The speed limit that is shown on the sign in m/s.
     */
    private final double speedLimit;

    /**
     * The lane index for which the traffic sign is valid for.
     * If the lane index is -1, the traffic sign is valid for all lanes.
     */
    private int lane = -1;

    /**
     * Creates a new interaction that changes the speed limit of a variable speed limit sign.
     *
     * @param time       Timestamp of this interaction, unit: [ns]
     * @param signId     speed sign identifier
     * @param lane       the lane index the sign is valid for (see: {@link #lane})
     * @param speedLimit the new speed limit in m/s
     */
    public TrafficSignSpeedLimitChange(long time, String signId, int lane, double speedLimit) {
        super(time);
        this.lane = lane;
        this.speedSignId = signId;
        this.speedLimit = speedLimit;
    }

    /**
     * Creates a new interaction that changes the speed limit of a variable speed limit sign.
     *
     * @param time       Timestamp of this interaction, unit: [ns]
     * @param signId     speed sign identifier
     * @param speedLimit the new speed limit in m/s
     */
    public TrafficSignSpeedLimitChange(long time, String signId, double speedLimit) {
        super(time);
        this.speedSignId = signId;
        this.speedLimit = speedLimit;
    }

    public String getTrafficSignId() {
        return speedSignId;
    }

    public double getSpeedLimit() {
        return speedLimit;
    }

    public int getLane() {
        return lane;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 71)
                .append(speedSignId)
                .append(lane)
                .append(speedLimit)
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

        TrafficSignSpeedLimitChange other = (TrafficSignSpeedLimitChange) obj;
        return new EqualsBuilder()
                .append(this.speedSignId, other.speedSignId)
                .append(this.lane, other.lane)
                .append(this.speedLimit, other.speedLimit)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("signId", speedSignId)
                .append("laneIndex", lane)
                .append("newSpeedLimit", speedLimit)
                .toString();
    }
}
