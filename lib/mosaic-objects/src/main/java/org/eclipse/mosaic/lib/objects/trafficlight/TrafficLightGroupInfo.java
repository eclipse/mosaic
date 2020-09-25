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

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * This class is a container for traffic light update. Immutable.
 * It is sent by the SumoAmbassador to inform simulation units about an updated traffic light state.
 */
public final class TrafficLightGroupInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Traffic light group id.
     */
    private final String trafficLightGroupId;

    /**
     * Id of the current program running on this traffic light group.
     */
    private final String currentProgramId;

    /**
     * Index of current phase running within the current program.
     */
    private final int currentPhaseIndex;

    /**
     * The assumed time of the switch to the next phase [ns].
     */
    private final long assumedTimeOfNextSwitch;

    private final List<TrafficLightState> currentState;

    /**
     * Creates an interaction that updates a traffic light group.
     *
     * @param trafficLightGroupId     Traffic light group id.
     * @param currentProgramId        Id of the current program running on this traffic light group.
     * @param currentPhaseIndex       Index of current phase running within the current program.
     * @param assumedTimeOfNextSwitch The assumed time of the switch to the next phase. Unit: [ns]
     * @param currentState            The actual state of all traffic lights belonging to this group.
     */
    public TrafficLightGroupInfo(final String trafficLightGroupId,
                                 final String currentProgramId,
                                 final int currentPhaseIndex,
                                 final long assumedTimeOfNextSwitch,
                                 final List<TrafficLightState> currentState) {
        this.trafficLightGroupId = trafficLightGroupId;
        this.currentProgramId = currentProgramId;
        this.currentPhaseIndex = currentPhaseIndex;
        this.assumedTimeOfNextSwitch = assumedTimeOfNextSwitch;
        this.currentState = currentState;
    }

    public String getGroupId() {
        return trafficLightGroupId;
    }

    public String getCurrentProgramId() {
        return currentProgramId;
    }

    public int getCurrentPhaseIndex() {
        return currentPhaseIndex;
    }

    /**
     * Returns the assumed time of the next phase switch in nanoseconds.
     */
    public long getAssumedTimeOfNextSwitch() {
        return assumedTimeOfNextSwitch;
    }

    public List<TrafficLightState> getCurrentState() {
        return currentState;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 67)
                .append(trafficLightGroupId)
                .append(currentProgramId)
                .append(currentPhaseIndex)
                .append(assumedTimeOfNextSwitch)
                .append(currentState)
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

        TrafficLightGroupInfo other = (TrafficLightGroupInfo) obj;
        return new EqualsBuilder()
                .append(this.trafficLightGroupId, other.trafficLightGroupId)
                .append(this.currentProgramId, other.currentProgramId)
                .append(this.currentPhaseIndex, other.currentPhaseIndex)
                .append(this.assumedTimeOfNextSwitch, other.assumedTimeOfNextSwitch)
                .append(this.currentState, currentState)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("trafficLightGroupId", trafficLightGroupId)
                .append("currentProgramId", currentProgramId)
                .append("currentPhaseIndex", currentPhaseIndex)
                .append("assumedNextSwitchTime", assumedTimeOfNextSwitch)
                .toString();
    }
}
