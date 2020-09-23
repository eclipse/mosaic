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

package org.eclipse.mosaic.interactions.communication;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.communication.HandoverInfo;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * This extension of {@link Interaction} is used by the cell2 ambassador to communicate handovers.
 */
public final class CellularHandoverUpdates extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(CellularHandoverUpdates.class);

    /**
     * List of {@link HandoverInfo} identifying vehicles with updated regions.
     */
    private final List<HandoverInfo> updated;

    /**
     * Constructor for {@link CellularHandoverUpdates}.
     *
     * @param time    Timestamp of this interaction, unit: [ns]
     * @param updated Vehicles that where already in the simulation.
     */
    public CellularHandoverUpdates(long time, List<HandoverInfo> updated) {
        super(time);
        this.updated = updated;
    }

    public List<HandoverInfo> getUpdated() {
        return this.updated;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 17)
                .append(updated)
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

        CellularHandoverUpdates other = (CellularHandoverUpdates) obj;
        return new EqualsBuilder()
                .append(this.updated, other.updated)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("updated", getUpdated())
                .toString();
    }
}
