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
 * Provided the vehicle has a radar this wraps up all information that could be read
 * from it.
 */
@Immutable
public class RadarSensor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Absolute speed of the front vehicle in [m/s].
     * -1 If not available.
     */
    public final double speedFrontVehicle;

    /**
     * Construct a {@link RadarSensor} object.
     *
     * @param speedFrontVehicle the speed of the front vehicle.
     */
    public RadarSensor(double speedFrontVehicle) {
        this.speedFrontVehicle = speedFrontVehicle;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 11)
                .append(speedFrontVehicle)
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

        RadarSensor other = (RadarSensor) obj;
        return new EqualsBuilder()
                .append(this.speedFrontVehicle, other.speedFrontVehicle)
                .isEquals();

    }

    @Override
    public String toString() {
        return "RadarSensor{" + "speedFrontVehicle=" + speedFrontVehicle + '}';
    }

}
