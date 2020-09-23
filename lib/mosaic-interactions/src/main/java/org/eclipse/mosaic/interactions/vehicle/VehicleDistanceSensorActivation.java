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

package org.eclipse.mosaic.interactions.vehicle;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.rti.api.Interaction;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;

/**
 * This extension of {@link Interaction} is intended to be used to enable distance sensors of vehicles.
 */
@SuppressWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public final class VehicleDistanceSensorActivation extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleDistanceSensorActivation.class);

    public enum DistanceSensors {
        FRONT, LEFT, RIGHT, REAR
    }

    /**
     * String identifying a simulated vehicle.
     */
    private final String vehicleId;

    /**
     * The maximum distance to look ahead for a leading vehicle.
     */
    private final double maximumLookahead;

    /**
     * list of distance sensors.
     */
    private DistanceSensors[] sensors;

    /**
     * Creates a new {@link VehicleDistanceSensorActivation} interaction.
     *
     * @param time             Timestamp of this interaction, unit: [ns]
     * @param vehicleId        vehicle identifier
     * @param maximumLookahead maximum distance to look ahead for a leading vehicle
     * @param sensors          list of distance sensors
     */
    public VehicleDistanceSensorActivation(long time, String vehicleId, double maximumLookahead, DistanceSensors... sensors) {
        super(time);
        this.vehicleId = vehicleId;
        this.maximumLookahead = maximumLookahead;
        this.sensors = sensors;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public double getMaximumLookahead() {
        return maximumLookahead;
    }

    public DistanceSensors[] getSensors() {
        return sensors;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 23)
                .append(vehicleId)
                .append(maximumLookahead)
                .append(sensors)
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

        VehicleDistanceSensorActivation other = (VehicleDistanceSensorActivation) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.maximumLookahead, other.maximumLookahead)
                .append(this.sensors, other.sensors)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("maximumLookahead", maximumLookahead)
                .append("sensors", Arrays.toString(sensors))
                .toString();
    }
}
