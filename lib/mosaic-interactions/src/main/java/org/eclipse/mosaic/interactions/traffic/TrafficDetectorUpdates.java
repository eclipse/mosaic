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

import org.eclipse.mosaic.lib.objects.UnitData;
import org.eclipse.mosaic.lib.objects.traffic.InductionLoopInfo;
import org.eclipse.mosaic.lib.objects.traffic.LaneAreaDetectorInfo;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This extension of {@link Interaction} combines updates of lane area and induction loop detectors.
 * Usually the updates are supplied by the traffic simulator and will be filtered by applications
 * subscribed to them.
 */
public final class TrafficDetectorUpdates extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(TrafficDetectorUpdates.class);

    /**
     * List of lane area detector information objects identifying lane area with updated positions.
     */
    private final List<LaneAreaDetectorInfo> updatedLaneAreaDetectors;

    /**
     * List of induction loops with updated data.
     */
    private final List<InductionLoopInfo> updatedInductionLoops;

    /**
     * Constructor for {@link TrafficDetectorUpdates}.
     *
     * @param time                     Timestamp of this interaction, unit: [ns]
     * @param updatedLaneAreaDetectors A list of updated {@link LaneAreaDetectorInfo}'s.
     * @param updatedInductionLoops    A list of updated {@link InductionLoopInfo}'s.
     */
    public TrafficDetectorUpdates(long time, List<LaneAreaDetectorInfo> updatedLaneAreaDetectors,
                                  List<InductionLoopInfo> updatedInductionLoops) {
        super(time);
        this.updatedLaneAreaDetectors = updatedLaneAreaDetectors;
        this.updatedInductionLoops = updatedInductionLoops;
    }

    public List<LaneAreaDetectorInfo> getUpdatedLaneAreaDetectors() {
        return this.updatedLaneAreaDetectors;
    }

    public List<InductionLoopInfo> getUpdatedInductionLoops() {
        return updatedInductionLoops;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 37)
                .append(updatedLaneAreaDetectors)
                .append(updatedInductionLoops)
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

        TrafficDetectorUpdates other = (TrafficDetectorUpdates) obj;
        return new EqualsBuilder()
                .append(this.updatedInductionLoops, other.updatedInductionLoops)
                .append(this.updatedLaneAreaDetectors, other.updatedLaneAreaDetectors)
                .isEquals();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("updatedInductionLoops",
                        updatedInductionLoops.stream().map(UnitData::getName).collect(Collectors.joining(","))
                )
                .append("updatedLaneAreaDetectors",
                        updatedLaneAreaDetectors.stream().map(UnitData::getName).collect(Collectors.joining(","))
                )
                .toString();
    }
}
