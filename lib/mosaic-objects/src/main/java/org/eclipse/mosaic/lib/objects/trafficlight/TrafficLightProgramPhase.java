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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a traffic light phase within a traffic light program,
 * characterized by its duration and certain states of traffic lights.
 */
public class TrafficLightProgramPhase implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int index;

    /**
     * Duration given in the configuration file. Unit: [ns]
     * Be aware that actual phases duration can be changed during the simulation (e.g. lengthened)
     * and thus can differ from the configured one.
     */
    private final long configuredDuration;

    /**
     * Remaining duration of this phase. Unit: [ns]
     */
    private long remainingDuration;

    private final List<TrafficLightState> states;

    /**
     * Constructor of a TrafficLightProgramPhase.
     *
     * @param index              index of the phase inside of a traffic light program
     * @param configuredDuration Configured duration of the phase. Unit: [ns].
     * @param states             states that the according traffic lights will have during this phase
     */
    public TrafficLightProgramPhase(int index, long configuredDuration, List<TrafficLightState> states) {
        this.index = index;
        this.configuredDuration = configuredDuration;
        this.remainingDuration = configuredDuration;
        this.states = Collections.unmodifiableList(new ArrayList<>(states));
    }

    public int getIndex() {
        return index;
    }

    /**
     * Returns the duration of the phase as it is in the configuration file. Unit: [ns].
     */
    public long getConfiguredDuration() {
        return configuredDuration;
    }

    public List<TrafficLightState> getStates() {
        return states;
    }

    /**
     * Returns the duration of the phase as it is in the configuration file. Unit: [ns].
     */
    public long getRemainingDuration() {
        return remainingDuration;
    }

    /**
     * Sets the remaining duration of this phase.
     *
     * @param remainingDuration in nanoseconds
     */
    public void setRemainingDuration(long remainingDuration) {
        this.remainingDuration = remainingDuration;
    }

    /**
     * Returns true, if states of this traffic light phase equal states of another TrafficLightPhase.
     * (ignores id and duration)
     *
     * @param otherPhase to be compared with.
     * @return true if phases are equal.
     */
    public boolean equalsOtherPhase(TrafficLightProgramPhase otherPhase) {
        if (otherPhase == null) {
            return false;
        }
        if (otherPhase == this) {
            return true;
        }
        if (otherPhase.getStates().size() != states.size()) {
            return false;
        }

        return new EqualsBuilder()
                .append(this.states, otherPhase.states)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 17)
                .append(index)
                .append(states)
                .append(configuredDuration)
                .append(remainingDuration)
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

        TrafficLightProgramPhase other = (TrafficLightProgramPhase) obj;
        return new EqualsBuilder()
                .append(this.index, other.index)
                .append(this.states, other.states)
                .append(this.configuredDuration, other.configuredDuration)
                .append(this.remainingDuration, other.remainingDuration)
                .isEquals();
    }

    @Override
    public String toString() {
        return "TrafficLightPhase{"
                + "id=" + index
                + ", configuredDuration="
                + configuredDuration
                + ", remainingDuration="
                + remainingDuration
                + ", states=" + states
                + '}';
    }

}
