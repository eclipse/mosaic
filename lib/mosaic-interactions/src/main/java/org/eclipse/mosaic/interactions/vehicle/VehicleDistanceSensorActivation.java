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
     * Sensor type.
     */
    private final SensorTypes sensorType;

    /**
     * String identifying a simulated vehicle.
     */
    private final String vehicleId;
    /**
     * list of distance sensors.
     */
    private final DistanceSensors[] sensors;

    /**
     * The maximum distance to look ahead for a leading vehicle.
     */
    private final double maximumLookahead;
    /**
     * True if sensor shall be spawned, False if it shall be destroyed.
     */
    private final boolean activate;

    /**
     * Creates a new {@link VehicleDistanceSensorActivation} interaction.
     *
     * @param time             Timestamp of this interaction, unit: [ns]
     * @param vehicleId        vehicle identifier
     * @param sensorType
     * @param maximumLookahead maximum distance to look ahead for a leading vehicle
     * @param activate
     * @param sensors          list of distance sensors
     */
    public VehicleDistanceSensorActivation(long time, String vehicleId, SensorTypes sensorType, double maximumLookahead, boolean activate, DistanceSensors... sensors) {
        super(time);
        this.vehicleId = vehicleId;
        this.sensorType = sensorType;
        this.maximumLookahead = maximumLookahead;
        this.activate = activate;
        this.sensors = sensors;
    }

    public SensorTypes getSensorType() {
        return sensorType;
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

    public boolean isActivate() {
        return activate;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 23)
                .append(vehicleId)
                .append(maximumLookahead)
                .append(sensors)
                .append(sensorType)
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
                .append(this.sensorType, other.sensorType)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("maximumLookahead", maximumLookahead)
                .append("sensors", Arrays.toString(sensors))
                .append("sensorType", sensorType)
                .toString();
    }

    /**
     * Possible sensor types to be spawned.
     */
    public enum SensorTypes {
        UNKNOWN, LiDAR
    }
}
