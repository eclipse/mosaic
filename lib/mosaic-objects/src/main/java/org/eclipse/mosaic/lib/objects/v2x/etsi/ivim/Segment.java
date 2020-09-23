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

package org.eclipse.mosaic.lib.objects.v2x.etsi.ivim;

import org.eclipse.mosaic.lib.objects.ToDataOutput;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
 * Contains segment information and vehicle control advices for each segment.
 */
public class Segment implements ToDataOutput, Serializable {

    private final String name;
    private SegmentPosition start;
    private SegmentPosition end;
    private final List<String> edges = new ArrayList<>();

    private final Map<Integer, Advice> advices = new HashMap<>();

    public Segment(String name) {
        this.name = name;
    }

    public Segment(DataInput dataInput) throws IOException {
        this.name = dataInput.readUTF();
        this.start = new SegmentPosition(dataInput);
        this.end = new SegmentPosition(dataInput);
        int edgesListLength = dataInput.readInt();
        for (int i = 0; i < edgesListLength; i++) {
            edges.add(dataInput.readUTF());
        }
        int lanesListLength = dataInput.readInt();
        for (int i = 0; i < lanesListLength; i++) {
            int laneIndex = dataInput.readInt();
            Advice lane = new Advice(dataInput);
            advices.put(laneIndex, lane);
        }
    }

    @Override
    public void toDataOutput(@Nonnull DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(name);
        start.toDataOutput(dataOutput);
        end.toDataOutput(dataOutput);
        dataOutput.writeInt(edges.size());
        for (String edge : edges) {
            dataOutput.writeUTF(edge);
        }
        dataOutput.writeInt(advices.size());
        for (Map.Entry<Integer, Advice> entry : advices.entrySet()) {
            dataOutput.writeInt(entry.getKey());
            entry.getValue().toDataOutput(dataOutput);
        }
    }

    public void updateAccelerationAdvices(double currentMaxAcceleration) {
        for (Advice advice : advices.values()) {
            advice.updateAccelerationAdvice(currentMaxAcceleration);
        }
    }

    public Segment setStartPosition(SegmentPosition startPosition) {
        this.start = startPosition;
        return this;
    }

    public Segment setEndPosition(SegmentPosition endPosition) {
        this.end = endPosition;
        return this;
    }

    public Segment setEdges(List<String> edges) {
        if (edges != null) {
            this.edges.addAll(edges);
        }
        return this;
    }

    public Segment putAdvice(int laneIndex, Advice advice) {
        advices.put(laneIndex, advice);
        return this;
    }

    public boolean update(Segment otherSegment) {
        if (this.equals(otherSegment)) {
            for (Map.Entry<Integer, Advice> otherEntry : otherSegment.getAdvicesPerLane().entrySet()) {
                if (advices.containsKey(otherEntry.getKey())) {
                    advices.get(otherEntry.getKey()).update(otherEntry.getValue());
                } else {
                    advices.put(otherEntry.getKey(), otherEntry.getValue());
                }
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public SegmentPosition getStartPosition() {
        return start;
    }

    public SegmentPosition getEndPosition() {
        return end;
    }

    public List<String> getEdges() {
        return edges;
    }

    public Advice getAdvice(int laneIndex) {
        return advices.get(laneIndex);
    }

    public List<Advice> getAdvices() {
        return new ArrayList<>(advices.values());
    }

    public Map<Integer, Advice> getAdvicesPerLane() {
        return advices;
    }

    public boolean isDifferentAcrossLanes() {
        Advice base = null;
        for (Advice advice : advices.values()) {
            if (base != null) {
                return differs(base.getSpeedAdvice(), advice.getSpeedAdvice())
                        || differs(base.getGap(), advice.getGap());
            } else {
                base = advice;
            }
        }
        return false;
    }

    private static boolean differs(double a, double b) {
        return Math.abs(a - b) > 0.01;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(31, 77)
                .append(this.name)
                .append(this.start)
                .append(this.end)
                .append(this.edges)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;

        Segment rhs = (Segment) obj;
        return new EqualsBuilder()
                .append(this.name, rhs.getName())
                .append(this.start, rhs.getStartPosition())
                .append(this.end, rhs.getEndPosition())
                .append(this.edges, rhs.getEdges())
                .isEquals();
    }

    public Segment copy() {
        Segment copy = new Segment(this.name);
        copy.edges.addAll(edges);
        advices.forEach((k,v) -> {
            copy.advices.put(k, v.copy());
        });
        copy.start = start;
        copy.end = end;
        return copy;
    }
}
