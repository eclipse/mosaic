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

import org.eclipse.mosaic.lib.enums.VehicleStopMode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is intended to be used to forward a request to stop a
 * vehicle to the RTI.
 */
public final class VehicleStop extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(VehicleStop.class);

    /**
     * String identifying the vehicle sending this interaction.
     */
    private final String vehicleId;

    /**
     * Edge where the vehicle is supposed to stop on.
     */
    private final IRoadPosition stopPosition;

    /**
     * Duration of the stop, unit: [ms].
     */
    private final long duration;

    /**
     * The mode of stopping, e.g. parking at the road side or stopping on the street.
     */
    private final VehicleStopMode vehicleStopMode;

    /**
     * Creates a new {@link VehicleStop} interaction.
     *
     * @param time            Timestamp of this interaction, unit: [ns]
     * @param vehicleId       String identifying the vehicle sending this interaction
     * @param stopPosition    Position, where the vehicle should be stopped on
     * @param duration        Duration of the stop, unit: [ms]
     * @param vehicleStopMode How to stop the vehicle.
     */
    public VehicleStop(long time, String vehicleId, IRoadPosition stopPosition, long duration, VehicleStopMode vehicleStopMode) {
        super(time);
        this.vehicleId = vehicleId;
        this.stopPosition = stopPosition;
        this.vehicleStopMode = vehicleStopMode;
        this.duration = duration;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public IRoadPosition getStopPosition() {
        return stopPosition;
    }

    public long getDuration() {
        return duration;
    }

    public VehicleStopMode getVehicleStopMode() {
        return vehicleStopMode;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 83)
                .append(vehicleId)
                .append(stopPosition)
                .append(duration)
                .append(vehicleStopMode)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        VehicleStop other = (VehicleStop) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.stopPosition, other.stopPosition)
                .append(this.duration, other.duration)
                .append(this.vehicleStopMode, other.vehicleStopMode)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("stopPosition", stopPosition)
                .append("duration", duration)
                .append("vehicleStopMode", vehicleStopMode)
                .build();
    }
}
