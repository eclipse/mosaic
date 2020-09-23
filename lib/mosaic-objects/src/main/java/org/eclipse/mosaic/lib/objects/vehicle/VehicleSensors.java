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

package org.eclipse.mosaic.lib.objects.vehicle;

import org.eclipse.mosaic.lib.objects.vehicle.sensor.DistanceSensor;
import org.eclipse.mosaic.lib.objects.vehicle.sensor.RadarSensor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * Contains all information from sensors this vehicle has.
 */
@Immutable
public class VehicleSensors implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * All distance sensor information the vehicle has.
     */
    public final DistanceSensor distance;

    /**
     * All radar sensor information the vehicle has.
     */
    public final RadarSensor radar;

    /**
     * Creates a VehicleSensors object that contains the given sensors.
     */
    public VehicleSensors(DistanceSensor distance, RadarSensor radar) {
        this.distance = distance;
        this.radar = radar;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 97)
                .append(this.distance)
                .append(this.radar)
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

        VehicleSensors other = (VehicleSensors) obj;
        return new EqualsBuilder()
                .append(this.radar, other.radar)
                .append(this.distance, other.distance)
                .isEquals();
    }

    @Override
    public String toString() {
        return "VehicleSensors{" + "distance=" + distance + ", radar=" + radar + '}';
    }

}
