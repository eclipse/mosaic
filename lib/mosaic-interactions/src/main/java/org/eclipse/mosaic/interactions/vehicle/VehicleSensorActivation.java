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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.mosaic.rti.api.Interaction;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public final class VehicleSensorActivation extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleSensorActivation.class);

    /**
     * Possible sensor types to be spawned.
     */
    public enum SensorTypes {
        LiDAR
    }

    /**
     * Id of the vehicle the sensor is attached to.
     */
    private final String vehicleId;

    /**
     * Sensor type.
     */
    private final SensorTypes sensor;

    /**
     * True if sensor shall be spawned, False if it shall be destroyed.
     */
    boolean activate;

    /**
     * Creates a new {@link VehicleSensorActivation} interaction.
     *
     * @param time Timestamp of this interaction, unit: [ms]
     */
    public VehicleSensorActivation(long time, String vehicleId, SensorTypes sensor, boolean activate) {
        super(time);
        this.vehicleId = vehicleId;
        this.sensor = sensor;
        this.activate = activate;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public SensorTypes getSensor() {
        return sensor;
    }

    public boolean isActivate() {
        return activate;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 23)
                .append(vehicleId)
                .append(sensor)
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

        VehicleSensorActivation other = (VehicleSensorActivation) obj;
        return new EqualsBuilder()
                .append(this.vehicleId, other.vehicleId)
                .append(this.sensor, other.sensor)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("vehicleId", vehicleId)
                .append("sensor", sensor)
                .toString();
    }
}
