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

package org.eclipse.mosaic.interactions.traffic;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is sent when a unit wants to subscribe for data of a SUMO induction loop.
 */
public final class InductionLoopDetectorSubscription extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(InductionLoopDetectorSubscription.class);

    /**
     * The identifier of the induction loop.
     */
    private String inductionLoopId;

    /**
     * Creates a new interaction that subscribes for data of a SUMO induction loop.
     *
     * @param time            Timestamp of this interaction, unit: [ns]
     * @param inductionLoopId induction loop identifier
     */
    public InductionLoopDetectorSubscription(long time, String inductionLoopId) {
        super(time);
        this.inductionLoopId = inductionLoopId;
    }

    public String getInductionLoopId() {
        return inductionLoopId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 37)
                .append(inductionLoopId)
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

        InductionLoopDetectorSubscription other = (InductionLoopDetectorSubscription) obj;
        return new EqualsBuilder()
                .append(this.inductionLoopId, other.inductionLoopId)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("inductionLoopId", inductionLoopId)
                .toString();
    }
}
