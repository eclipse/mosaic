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

package org.eclipse.mosaic.lib.objects.v2x.etsi.ivim;

import org.eclipse.mosaic.lib.objects.ToDataOutput;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Traffic control advice for vehicles to adjust their speeds or acc parameters, or to switch lanes.
 */
public class Advice implements ToDataOutput, Serializable {

    public enum ReactionState {
        NOT_REACTED, REACTED, NO_ADVICE_AVAILABLE
    }

    public enum LaneChange {
        ToLeft, ToRight
    }

    /**
     * Speed advice in m/s.
     */
    private double speed = -1.0;
    private final MutablePair<ReactionState, Double> speedState = MutablePair.of(ReactionState.NO_ADVICE_AVAILABLE, -1.0);

    /**
     * Gap advice in s.
     */
    private double gap = -1.0;
    private final MutablePair<ReactionState, Double> gapState = MutablePair.of(ReactionState.NO_ADVICE_AVAILABLE, -1.0);

    /**
     * Acceleration advice in m/s^2.
     */
    private double accelerationFactor = -1.0;
    private double acceleration = -1.0;
    private final MutablePair<ReactionState, Double> accelerationState = MutablePair.of(ReactionState.NO_ADVICE_AVAILABLE, -1.0);

    /**
     * Tells whether the lane is dedicated for AVs only.
     */
    private boolean dedicatedForAvsOnly = false;

    /**
     * Map of vehicles that shall react on lane change advices.
     */
    private Map<String, LaneChange> laneChanges = new HashMap<>();

    public Advice() {

    }

    public Advice(@Nonnull DataInput dataInput) throws IOException {
        this.speed = dataInput.readDouble();
        this.gap = dataInput.readDouble();
        this.accelerationFactor = dataInput.readDouble();
        this.dedicatedForAvsOnly = dataInput.readBoolean();

        for (int mapSize = dataInput.readInt(); mapSize > 0; mapSize--) {
            String vehicleId = dataInput.readUTF();
            final int laneChange = dataInput.readInt();
            this.laneChanges.put(vehicleId, laneChange == 1 ? LaneChange.ToLeft : laneChange == 2 ? LaneChange.ToRight : null);
        }
    }

    @Override
    public void toDataOutput(@Nonnull DataOutput dataOutput) throws IOException {
        dataOutput.writeDouble(speed);
        dataOutput.writeDouble(gap);
        dataOutput.writeDouble(accelerationFactor);
        dataOutput.writeBoolean(dedicatedForAvsOnly);

        dataOutput.writeInt(laneChanges.size());
        for (Map.Entry<String, LaneChange> laneChangeAdvice : laneChanges.entrySet()) {
            dataOutput.writeUTF(laneChangeAdvice.getKey());
            LaneChange laneChange = laneChangeAdvice.getValue();
            dataOutput.writeInt(
                    laneChange == LaneChange.ToLeft ? 1
                            : laneChange == LaneChange.ToRight ? 2
                            : 0
            );
        }
    }

    public void update(Advice other) {
        this.setSpeedAdvice(other.speed);
        this.setGap(other.gap);
        this.setAccelerationFactor(other.accelerationFactor);
        this.setDedicatedAvLane(other.dedicatedForAvsOnly);
        this.laneChanges = other.getLaneChanges();
    }

    void updateAccelerationAdvice(double currentMaxAcceleration) {
        acceleration = accelerationFactor == -1 ? -1 : currentMaxAcceleration * accelerationFactor;
    }

    public Advice setSpeedAdvice(double speedAdvice) {
        this.speed = speedAdvice;
        if (eq(speedAdvice, -1.0)) {
            speedState.setLeft(ReactionState.NO_ADVICE_AVAILABLE);
            speedState.setRight(-1.0);
        } else if (!eq(speedState.getValue(), speedAdvice)) {
            speedState.setLeft(ReactionState.NOT_REACTED);
            speedState.setRight(speedAdvice);
        }
        return this;
    }

    public Advice setGap(double gap) {
        this.gap = gap;
        if (eq(gap, -1.0)) {
            gapState.setLeft(ReactionState.NO_ADVICE_AVAILABLE);
            gapState.setRight(-1.0);
        } else if (!eq(gapState.getValue(), gap)) {
            gapState.setLeft(ReactionState.NOT_REACTED);
            gapState.setRight(gap);
        }
        return this;
    }

    public Advice setAccelerationFactor(double accelerationFactor) {
        this.accelerationFactor = accelerationFactor;
        if (eq(accelerationFactor, -1)) {
            accelerationState.setLeft(ReactionState.NO_ADVICE_AVAILABLE);
            accelerationState.setRight(-1.0);
        } else if (!eq(accelerationState.getValue(), gap)) {
            accelerationState.setLeft(ReactionState.NOT_REACTED);
            accelerationState.setRight(accelerationFactor);
        }
        return this;
    }

    public Advice setLaneChange(String vehicleId, LaneChange laneChange) {
        laneChanges.put(vehicleId, laneChange);
        return this;
    }

    public Advice setDedicatedAvLane(boolean dedicatedForAvsOnly) {
        this.dedicatedForAvsOnly = dedicatedForAvsOnly;
        return this;
    }

    public double getSpeedAdvice() {
        return speed;
    }

    public double getGap() {
        return gap;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public boolean isDedicatedForAvsOnly() {
        return this.dedicatedForAvsOnly;
    }

    public Map<String, LaneChange> getLaneChanges() {
        return laneChanges;
    }

    public List<String> getLaneChangeReceivers() {
        return new ArrayList<>(laneChanges.keySet());
    }

    public void reactedOnAccelerationAdvice(double acceleration) {
        accelerationState.setLeft(ReactionState.REACTED);
        accelerationState.setRight(acceleration);
    }

    public void reactedOnGapAdvice(double gap) {
        gapState.setLeft(ReactionState.REACTED);
        gapState.setRight(gap);
    }

    public void reactedOnSpeedAdvice(double speed) {
        speedState.setLeft(ReactionState.REACTED);
        speedState.setRight(speed);
    }

    public ReactionState getSpeedAdviceReactionState() {
        return speedState.getKey();
    }

    public ReactionState getGapAdviceReactionState() {
        return gapState.getKey();
    }

    public ReactionState getAccelerationAdviceReactionState() {
        return accelerationState.getKey();
    }

    public void resetReactionStates() {
        resetReactionStateGapAdvice();
        resetReactionStateSpeedAdvice();
        resetReactionStateAccelerationAdvice();
    }

    public void resetReactionStateSpeedAdvice() {
        speedState.setLeft(eq(speed, -1.0) ? ReactionState.NO_ADVICE_AVAILABLE : ReactionState.NOT_REACTED);
        speedState.setRight(speed);
    }

    public void resetReactionStateGapAdvice() {
        gapState.setLeft(eq(gap, -1.0) ? ReactionState.NO_ADVICE_AVAILABLE : ReactionState.NOT_REACTED);
        gapState.setRight(gap);
    }

    public void resetReactionStateAccelerationAdvice() {
        accelerationState.setLeft(eq(acceleration, -1.0) ? ReactionState.NO_ADVICE_AVAILABLE : ReactionState.NOT_REACTED);
        accelerationState.setRight(acceleration);
    }

    private boolean eq(double a, double b) {
        return Math.abs(a - b) < 0.0001d;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 27)
                .append(this.speed)
                .append(this.gap)
                .append(this.accelerationFactor)
                .append(this.dedicatedForAvsOnly)
                .append(this.laneChanges)
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

        Advice rhs = (Advice) obj;
        return new EqualsBuilder()
                .append(this.speed, rhs.speed)
                .append(this.gap, rhs.gap)
                .append(this.accelerationFactor, rhs.accelerationFactor)
                .append(this.dedicatedForAvsOnly, rhs.dedicatedForAvsOnly)
                .append(this.laneChanges, rhs.laneChanges)
                .isEquals();
    }

    public Advice copy() {
        Advice copy = new Advice();
        copy.update(this);
        return copy;
    }
}
