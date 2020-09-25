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
import javax.annotation.concurrent.Immutable;

/**
 * This contains all information gathered by the vehicles distance sensor.
 */
@Immutable
public class DistanceSensor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Distance to leading vehicle in [m]. Will be -1 when vehicle does not have
     * this specific distance sensor. Will be Double.POSITIVE_INFINITY when no
     * vehicle was detected.
     */
    public final SensorValue front;

    /**
     * Distance to following vehicle in [m]. Will be -1 when vehicle does not
     * have this specific distance sensor. Will be Double.POSITIVE_INFINITY when
     * no vehicle was detected.
     */
    public final SensorValue back;

    /**
     * Distance to vehicle to the left in [m]. Will be -1 when vehicle does not
     * have this specific distance sensor. Will be Double.POSITIVE_INFINITY when
     * no vehicle was detected.
     */
    public final SensorValue left;

    /**
     * Distance to vehicle to the right in [m]. Will be -1 when vehicle does not
     * have this specific distance sensor. Will be Double.POSITIVE_INFINITY when
     * no vehicle was detected.
     */
    public final SensorValue right;

    /**
     * This creates a new distance sensor object. If the vehicle does not have
     * specific sensors set the appropriate variables to -1. If no vehicle found
     * in sensor range set to Double.POSITIVE_INFINITY
     *
     * @param front       see {@link #front}
     * @param back        see {@link #back}
     * @param left        see {@link #left}
     * @param right       see {@link #right}
     */
    public DistanceSensor(double front, double back, double left, double right) {
        this.front = new SensorValue(front);
        this.back = new SensorValue(back);
        this.left = new SensorValue(left);
        this.right = new SensorValue(right);
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder(3, 59)
                .append(this.front)
                .append(this.back)
                .append(this.left)
                .append(this.right)
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

        DistanceSensor other = (DistanceSensor) obj;
        return new EqualsBuilder()
                .append(this.front, other.front)
                .append(this.back, other.back)
                .append(this.left, other.left)
                .append(this.right, other.right)
                .isEquals();
    }

    @Override
    public String toString() {
        return "DistanceSensor{" + "front=" + this.front + ", back=" + this.back + ", left="
                + this.left + ", right=" + this.right + '}';
    }

}

