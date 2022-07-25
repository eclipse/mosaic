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
 * forward a request to reduce the speed of a simulated vehicle.
 * The name 'SlowDown' is inherited by Sumo and a little bit misleading,
 * the speed can also be increased.
 */
public final class VehicleSlowDown extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleSlowDown.class);

    /**
     * String identifying a simulated vehicle.
     */
    private final String vehicleId;

    /**
     * new speed of a vehicle.
     */
    private final float speed;

    /**
     * Time after which the new speed shall be reached.
     */
    private final long timeInterval;

    /**
     * Constructor for a {@link VehicleSlowDown} interaction.
     *
     * @param time         Timestamp of this interaction, unit: [ns]
     * @param vehicleId    The id of the vehicle, that gets slowed down.
     * @param speed        The desired speed.
     * @param timeInterval The time interval in which the desired speed should be reached, unit: [ns]
     */
    public VehicleSlowDown(long time, String vehicleId, float speed, long timeInterval) {
        super(time);
        this.vehicleId = vehicleId;
        this.speed = speed;
        this.timeInterval = timeInterval;
    }

    public float getSpeed() {
        return speed;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public long getInterval() {
        return timeInterval;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 71)
                .append(vehicleId)
                .append(speed)
                .append(timeInterval)
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

        VehicleSlowDown other = (VehicleSlowDown) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.speed, other.speed)
                .append(this.timeInterval, other.timeInterval)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("speed", speed)
                .append("timeInterval", timeInterval)
                .toString();
    }
}
