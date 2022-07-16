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
 * This represents a 'change of speed' instruction to the used traffic simulator. The instruction
 * basically only needs the target speed as well as the timeframe to reach that speed.
 * Seeing as some traffic simulators can't seem to detect if this represents a speed up
 * or a slow down compared to the current speed a {@link VehicleSpeedChangeType} needs to be given to
 * determine that data.
 *
 * <pre>
 * As a special bonus {@link VehicleSpeedChangeType#RESET} tells the traffic simulator to control the
 * speed on its own again. In that case speed as well as interval should be disregarded by
 * the respective traffic simulator ambassadors.
 * </pre>
 */
public final class VehicleSpeedChange extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * These are all available types of speed changes currently supported.
     */
    public enum VehicleSpeedChangeType {
        WITH_INTERVAL,
        RESET,
        WITH_FORCED_ACCELERATION,
        WITH_PLEASANT_ACCELERATION
    }

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleSpeedChange.class);

    /**
     * Vehicle identifier.
     */
    private final String vehicleId;

    /**
     * Change speed type.
     */
    private final VehicleSpeedChangeType type;

    /**
     * New speed in m/s.
     */
    private final double newSpeed;

    /**
     * Interval (in ms) in which the new speed shall be reached.
     */
    private final long changeInterval;

    /**
     * The desired acceleration.
     */
    private final double acceleration;

    /**
     * Creates a new {@link VehicleSpeedChange} interaction.
     *
     * @param time         Timestamp of this interaction, unit: [ns]
     * @param vehicleId    vehicle identifier
     * @param type         change speed type
     * @param newSpeed     New desired speed, unit [m/s]
     * @param interval     Interval in which the desired speed shall be reached, unit [ns]
     * @param acceleration desired acceleration
     */
    public VehicleSpeedChange(long time, String vehicleId, VehicleSpeedChangeType type,
                              double newSpeed, long interval, double acceleration) {
        super(time);
        this.vehicleId = vehicleId;
        this.type = type;
        this.newSpeed = newSpeed;
        this.changeInterval = interval;
        this.acceleration = acceleration;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public VehicleSpeedChangeType getType() {
        return this.type;
    }

    public double getSpeed() {
        return this.newSpeed;
    }

    public long getInterval() {
        return changeInterval;
    }

    public double getAcceleration() {
        return acceleration;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 73)
                .append(vehicleId)
                .append(type)
                .append(newSpeed)
                .append(changeInterval)
                .append(acceleration)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        VehicleSpeedChange other = (VehicleSpeedChange) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.type, other.type)
                .append(this.newSpeed, other.newSpeed)
                .append(this.changeInterval, other.changeInterval)
                .append(this.acceleration, other.acceleration)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("type", type)
                .append("newSpeed", newSpeed)
                .append("changeInterval", changeInterval)
                .append("acceleration", acceleration)
                .toString();
    }
}
