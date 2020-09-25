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

package org.eclipse.mosaic.interactions.traffic;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * This extension of {@link Interaction} is intended to be used to
 * forward a request to change the state of a simulated traffic light.
 */
public final class TrafficLightStateChange extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(TrafficLightStateChange.class);


    /**
     * Identifies which kind of parameter type is used in this TrafficLightStateChange.
     */
    public enum ParamType {
        Undefined, RemainingDuration, ProgramId, ChangePhase, ChangeProgramWithPhase, ChangeToCustomState
    }

    /**
     * String identifying a simulated traffic light group.
     */
    private final String trafficLightGroupId;

    /**
     * Indicates the parameter type of this{@link TrafficLightStateChange}.
     */
    private ParamType type;

    /**
     * Stores id of the new traffic light program.
     */
    private String programId;

    /**
     * Stores index of the new traffic light program phase.
     */
    private int phaseIndex;

    /**
     * Stores the phase remaining duration [s].
     */
    private double remainingDuration;

    /**
     * A list of states for all traffic lights of the group to set.
     */
    private List<TrafficLightState> customStateList;


    /**
     * Constructs {@link TrafficLightStateChange}>.
     * The parameter type is initially {@code ParamType.Undefined}.
     *
     * @param time                  Timestamp of this interaction, unit: [ns]
     * @param trafficLightGroupId traffic light group identifier
     */
    public TrafficLightStateChange(long time, String trafficLightGroupId) {
        super(time);
        this.trafficLightGroupId = trafficLightGroupId;
        reset();
    }

    /**
     * Resets the pre-defined parameter type and the corresponding parameters.
     * The parameter type of this {@link TrafficLightStateChange} changes to
     * {@code ParamType.Undefined}.
     */
    public void reset() {
        this.type = ParamType.Undefined;
        this.remainingDuration = -1;
        this.programId = "";
    }

    /**
     * Sets the phase remaining duration.
     * The parameter type of this {@link TrafficLightStateChange} changes to
     * {@code ParamType.RemainingDuration}.
     *
     * @param remainingDuration new phase remaining duration in millisecond
     */
    public void setPhaseRemainingDuration(double remainingDuration) {
        reset();
        this.type = ParamType.RemainingDuration;
        this.remainingDuration = remainingDuration;
    }

    /**
     * Sets id of the new traffic light program.
     * The parameter type of this {@link TrafficLightStateChange} changes to
     * {@code ParamType.ProgramId}.
     *
     * @param programId id of the new traffic light program
     */
    public void setProgramId(String programId) {
        reset();
        this.type = ParamType.ProgramId;
        this.programId = programId;
    }

    /**
     * Sets index of the new traffic light program phase.
     * The parameter type of this {@link TrafficLightStateChange} changes to
     * {@code ParamType.ChangePhase}.
     *
     * @param phaseIndex index of the new traffic light program phase
     */
    public void setPhaseIndex(int phaseIndex) {
        reset();
        this.type = ParamType.ChangePhase;
        this.phaseIndex = phaseIndex;
    }

    /**
     * Sets id of the new traffic light program and index of the new phase for the new program.
     * The parameter type of this {@link TrafficLightStateChange} changes to
     * {@code ParamType.ChangeProgramWithPhase}.
     *
     * @param programId index of the new traffic light program
     */
    public void setProgramWithPhase(String programId, int phaseId) {
        reset();
        this.type = ParamType.ChangeProgramWithPhase;
        this.programId = programId;
        this.phaseIndex = phaseId;
    }

    /**
     * Sets a custom state which will be set for the traffic light.
     *
     * @param stateList the list of states for all traffic lights of the group
     */
    public void setCustomState(List<TrafficLightState> stateList) {
        reset();
        this.type = ParamType.ChangeToCustomState;
        this.customStateList = stateList;
    }

    public String getTrafficLightGroupId() {
        return this.trafficLightGroupId;
    }

    public ParamType getParameterType() {
        return this.type;
    }

    public double getPhaseRemainingDuration() {
        return this.remainingDuration;
    }

    public String getProgramId() {
        return this.programId;
    }

    public int getPhaseIndex() {
        return this.phaseIndex;
    }

    public List<TrafficLightState> getCustomStateList() {
        return customStateList;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 41)
                .append(trafficLightGroupId)
                .append(type)
                .append(remainingDuration)
                .append(programId)
                .append(phaseIndex)
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

        TrafficLightStateChange other = (TrafficLightStateChange) obj;
        return new EqualsBuilder()
                .append(this.trafficLightGroupId, other.trafficLightGroupId)
                .append(this.type, other.type)
                .append(this.phaseIndex, other.phaseIndex)
                .append(this.remainingDuration, other.remainingDuration)
                .append(this.programId, other.programId)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("trafficLightGroupName", trafficLightGroupId)
                .append("type", type)
                .append("programId", programId)
                .append("phaseIndex", phaseIndex)
                .append("remainingDuration", remainingDuration)
                .toString();
    }
}
