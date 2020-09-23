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

package org.eclipse.mosaic.lib.objects.trafficlight;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a program for a traffic light group.
 * The program consists of phases which describe the switches of traffic light states.
 **/
public class TrafficLightProgram implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Id of the traffic light program as it stands in the configuration file (e.g. in scenarioname.net.xml for SUMO).
     */
    private final String programId;

    /**
     * Phases this program consists of.
     */
    private final List<TrafficLightProgramPhase> phases;

    private int currentPhaseIndex;

    public TrafficLightProgram(String programId, List<TrafficLightProgramPhase> phases, int currentPhaseIndex) {
        this.programId = programId;
        this.phases = Collections.unmodifiableList(new ArrayList<>(phases));
        this.currentPhaseIndex = currentPhaseIndex;
    }

    /**
     * Returns a configured total duration of a program as the sum of all phase durations. Unit: [ns]
     *
     * @return a sum of all phase durations in nanoseconds
     */
    public long getProgramDuration() {
        long duration = 0;
        for (TrafficLightProgramPhase phase : phases) {
            duration += phase.getConfiguredDuration();
        }
        return duration;
    }

    /**
     * Returns a sequence of states for a certain traffic light,
     * where each state represents a state of the traffic light during a phase within a traffic light program.
     *
     * @param trafficLightId id of a traffic light within a traffic light group
     * @return a sequence of states for a certain traffic light within a traffic light program.
     */
    public List<TrafficLightState> getSignalSequence(int trafficLightId) {
        List<TrafficLightState> states = new ArrayList<>();
        for (TrafficLightProgramPhase phase : phases) {
            states.add(phase.getStates().get(trafficLightId));
        }
        return states;
    }

    public String getProgramId() {
        return programId;
    }

    public List<TrafficLightProgramPhase> getPhases() {
        return phases;
    }

    public int getCurrentPhaseIndex() {
        return currentPhaseIndex;
    }

    public TrafficLightProgramPhase getCurrentPhase() {
        return phases.get(currentPhaseIndex);
    }

    public void setCurrentPhase(int newPhaseId) {
        //reset the remaining duration value of the yet current phase
        getCurrentPhase().setRemainingDuration(getCurrentPhase().getConfiguredDuration());
        this.currentPhaseIndex = newPhaseId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 57)
                .append(programId)
                .append(currentPhaseIndex)
                .append(phases)
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

        TrafficLightProgram other = (TrafficLightProgram) obj;
        return new EqualsBuilder()
                .append(this.programId, other.programId)
                .append(this.currentPhaseIndex, other.currentPhaseIndex)
                .append(this.phases, other.phases)
                .isEquals();
    }

    @Override
    public String toString() {
        return "TrafficLightProgram{" + "programId=" + programId + ", currentPhaseIndex=" + currentPhaseIndex + ", trafficLightProgramPhases=" + phases + '}';
    }

}
