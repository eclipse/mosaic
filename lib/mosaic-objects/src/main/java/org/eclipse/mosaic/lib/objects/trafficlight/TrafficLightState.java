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

package org.eclipse.mosaic.lib.objects.trafficlight;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Represents a state of a traffic light.
 **/
public class TrafficLightState implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static TrafficLightState OFF = new TrafficLightState(false, false, false);
    public final static TrafficLightState GREEN = new TrafficLightState(false, true, false);
    public final static TrafficLightState YELLOW = new TrafficLightState(false, false, true);
    public final static TrafficLightState RED = new TrafficLightState(true, false, false);
    public final static TrafficLightState RED_YELLOW = new TrafficLightState(true, false, true);

    private final boolean red;
    private final boolean green;
    private final boolean yellow;

    /**
     * Constructor initializing the state of a traffic light represented by given booleans.
     */
    public TrafficLightState(boolean red, boolean green, boolean yellow) {
        this.red = red;
        this.green = green;
        this.yellow = yellow;
    }

    public boolean isRed() {
        return red;
    }

    public boolean isGreen() {
        return green;
    }

    public boolean isYellow() {
        return yellow;
    }

    public boolean isRedYellow() {
        return red && yellow;
    }

    public boolean isOff() {
        return !red && !yellow && !green;
    }

    @Override
    public String toString() {
        return isRedYellow() ? "red-yellow"
                : (isRed() ? "red"
                : (isGreen() ? "green"
                : (isYellow() ? "yellow"
                : "off")));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 73)
                .append(red)
                .append(green)
                .append(yellow)
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

        TrafficLightState other = (TrafficLightState) obj;
        return new EqualsBuilder()
                .append(this.red, other.red)
                .append(this.green, other.green)
                .append(this.yellow, other.yellow)
                .isEquals();
    }

}
