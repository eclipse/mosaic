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

package org.eclipse.mosaic.lib.objects.vehicle;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * This class provides information about currently active signals visible to the driver.
 * The specific signals are only set correctly if the used traffic simulator supports the values.
 */
@Immutable
public class VehicleSignals implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean blinkerRight;
    private final boolean blinkerLeft;
    private final boolean blinkerEmergency;
    private final boolean brakeLight;
    private final boolean reverseDrive;

    /**
     * Alternative constructor for direct value setting.
     */
    public VehicleSignals(
            boolean blinkerLeft, boolean blinkerRight, boolean blinkerEmergency,
            boolean brakeLight, boolean reverseDrive
    ) {
        this.blinkerLeft = blinkerLeft;
        this.blinkerRight = blinkerRight;
        this.blinkerEmergency = blinkerEmergency;
        this.brakeLight = brakeLight;
        this.reverseDrive = reverseDrive;
    }

    public boolean isBlinkerRight() {
        return blinkerRight;
    }

    public boolean isBlinkerLeft() {
        return blinkerLeft;
    }

    public boolean isBlinkerEmergency() {
        return blinkerEmergency;
    }

    public boolean isBrakeLight() {
        return brakeLight;
    }

    public boolean isReverseDrive() {
        return reverseDrive;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 79)
                .append(blinkerRight)
                .append(blinkerLeft)
                .append(blinkerEmergency)
                .append(brakeLight)
                .append(reverseDrive)
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

        VehicleSignals other = (VehicleSignals) obj;
        return new EqualsBuilder()
                .append(this.blinkerRight, other.blinkerRight)
                .append(this.blinkerLeft, other.blinkerLeft)
                .append(this.blinkerEmergency, other.blinkerEmergency)
                .append(this.brakeLight, other.brakeLight)
                .append(this.reverseDrive, other.reverseDrive)
                .isEquals();
    }

    @Override
    public String toString() {
        return "VehicleSignals{"
                + "blinkerRight=" + blinkerRight
                + ", blinkerLeft=" + blinkerLeft
                + ", blinkerEmergency=" + blinkerEmergency
                + ", brakeLight=" + brakeLight
                + ", reverseDrive=" + reverseDrive
                + '}';
    }

}
