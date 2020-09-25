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

package org.eclipse.mosaic.lib.objects.vehicle.sensor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Describes the value and status of a {@link DistanceSensor}.
 */
public class SensorValue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Represents the three possible statuses of a {@link DistanceSensor}.
     */
    public enum SensorStatus {
        NOT_EQUIPPED, VEHICLE_DETECTED, NO_VEHICLE_DETECTED
    }

    public final double distValue;
    public final SensorStatus status;

    /**
     * The constructor maps the old sensor status representation to the new status representation.
     * Formerly, a sensor distance value of -1 means that the vehicle was not equipped with a sensor.
     * A sensor distance value of -2 or Double.POSITIVE_INFINITY means that there was no vehicle detected by the sensor.
     *
     * @param distValue The value of the measured distance.
     */
    SensorValue(double distValue) {
        if (distValue == -1) {
            this.status = SensorStatus.NOT_EQUIPPED;
            this.distValue = 0.0;
        } else if (distValue == -2 || distValue == Double.POSITIVE_INFINITY) {
            this.status = SensorStatus.NO_VEHICLE_DETECTED;
            this.distValue = 0.0;
        } else {
            this.status = SensorStatus.VEHICLE_DETECTED;
            this.distValue = distValue;
        }
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

        SensorValue other = (SensorValue) obj;
        return new EqualsBuilder()
                .append(this.distValue, other.distValue)
                .append(this.status, other.status)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 67)
                .append(this.distValue)
                .append(this.status)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "DistanceSensorValue{status=" + this.status + " distance value=" + this.distValue + "}";
    }
}
