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
 * This extension of {@link Interaction} is intended to be used to
 * forward a request to change the lane of a simulated vehicle.
 */
public final class VehicleLaneChange extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * string identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleLaneChange.class);

    /**
     * This mode tells what is the target lane from the current lane of the vehicle.
     */
    public enum VehicleLaneChangeMode {
        TO_RIGHT,
        TO_LEFT,
        TO_RIGHTMOST,
        BY_INDEX,
        STAY
    }

    /**
     * string identifying a simulated vehicle.
     */
    private final String vehicleId;

    /**
     * the mode of change lane.
     */
    private final VehicleLaneChangeMode vehicleLaneChangeMode;

    /**
     * index of the target lane to change to.
     */
    private final int targetLaneIndex;

    /**
     * Index of the current lane.
     */
    private final int currentLaneIndex;

    /**
     * duration for how long the vehicle shall try to change to the target lane in ms.
     */
    private final long duration;

    /**
     * Creates a new {@link VehicleLaneChange} interaction using a lane index as target.
     *
     * @param time            Timestamp of this interaction, unit: [ns]
     * @param vehicleId       vehicle identifier
     * @param targetLaneIndex target lane index
     * @param duration        duration for how long the vehicle shall try to change to the target lane in ms
     */
    public VehicleLaneChange(long time, String vehicleId, int targetLaneIndex, long duration) {
        super(time);
        this.vehicleId = vehicleId;
        this.vehicleLaneChangeMode = VehicleLaneChangeMode.BY_INDEX;
        this.targetLaneIndex = targetLaneIndex;
        this.duration = duration;
        this.currentLaneIndex = -1;
    }

    /**
     * Creates a new {@link VehicleLaneChange} interaction using a direction as target.
     *
     * @param time                  Timestamp of this interaction, unit: [ns]
     * @param vehicleId             vehicle identifier
     * @param vehicleLaneChangeMode target lane relative to the current vehicle lane
     * @param currentLaneIndex      current lane index
     * @param duration              duration for how long the vehicle shall try to change to the target lane in ms
     */
    public VehicleLaneChange(long time, String vehicleId, VehicleLaneChangeMode vehicleLaneChangeMode,
                             int currentLaneIndex, long duration) {
        super(time);
        this.vehicleId = vehicleId;
        this.currentLaneIndex = currentLaneIndex;
        this.duration = duration;
        this.targetLaneIndex = -1;

        this.vehicleLaneChangeMode =
                vehicleLaneChangeMode == null || vehicleLaneChangeMode == VehicleLaneChangeMode.BY_INDEX
                        ? VehicleLaneChangeMode.STAY
                        : vehicleLaneChangeMode;
    }

    public VehicleLaneChangeMode getVehicleLaneChangeMode() {
        return vehicleLaneChangeMode;
    }

    /**
     * Returns the target lane index.
     * Returns <code>-1</code>, if <code>vehicleLaneChangeMode != index</code>.
     *
     * @return the targetLaneIndex.
     */
    public int getTargetLaneIndex() {
        return targetLaneIndex;
    }

    public int getCurrentLaneId() {
        return currentLaneIndex;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    /**
     * Getter for the duration.
     * The lane will be chosen for the given amount of time in millisecond.
     *
     * @return the duration.
     */
    public long getDuration() {
        return duration;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 59)
                .append(vehicleId)
                .append(vehicleLaneChangeMode)
                .append(targetLaneIndex)
                .append(currentLaneIndex)
                .append(duration)
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

        VehicleLaneChange other = (VehicleLaneChange) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.vehicleLaneChangeMode, other.vehicleLaneChangeMode)
                .append(this.targetLaneIndex, other.targetLaneIndex)
                .append(this.currentLaneIndex, other.currentLaneIndex)
                .append(this.duration, other.duration)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("vehicleLaneChangeMode", vehicleLaneChangeMode)
                .append("targetLaneIndex", targetLaneIndex)
                .append("currentLaneIndex", currentLaneIndex)
                .append("duration", duration)
                .toString();
    }

}
