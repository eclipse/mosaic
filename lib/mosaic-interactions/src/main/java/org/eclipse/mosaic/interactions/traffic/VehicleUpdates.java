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

import org.eclipse.mosaic.lib.objects.UnitData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This extension of {@link Interaction} is used to update the position of some or all vehicles
 * of the simulation. It consists of three lists, containing newly added vehicles, vehicles
 * which were updated since the last simulation step, and vehicles which have been removed
 * from the traffic simulation.
 */
public final class VehicleUpdates extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleUpdates.class);

    /**
     * Time at which the next sensor update will be sent.
     */
    private long nextUpdate;

    /**
     * List of {@link VehicleData} identifying added vehicles.
     */
    private final List<VehicleData> added;

    /**
     * List of {@link VehicleData} identifying vehicles with updated positions.
     */
    private final List<VehicleData> updated;

    /**
     * List of vehicle IDs (strings) identifying removed vehicles.
     */
    private final List<String> removedNames;

    /**
     * Constructor using fields.
     *
     * @param time         Timestamp of this interaction, unit: [ns]
     * @param added        Vehicles that were added in this simulation step.
     * @param updated      Vehicles that were already in the simulation.
     * @param removedNames Vehicles that were removed in this simulation step.
     */
    public VehicleUpdates(long time, List<VehicleData> added, List<VehicleData> updated, List<String> removedNames) {
        super(time);
        this.added = added;
        this.updated = updated;
        this.removedNames = removedNames;
    }

    public List<VehicleData> getAdded() {
        return this.added;
    }

    public List<VehicleData> getUpdated() {
        return this.updated;
    }

    public List<String> getRemovedNames() {
        return this.removedNames;
    }

    public long getNextUpdate() {
        return this.nextUpdate;
    }

    public void setNextUpdate(long nextUpdate) {
        this.nextUpdate = nextUpdate;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 17)
                .append(nextUpdate)
                .append(added)
                .append(updated)
                .append(removedNames)
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

        VehicleUpdates other = (VehicleUpdates) obj;
        return new EqualsBuilder()
                .append(this.nextUpdate, other.nextUpdate)
                .append(this.added, other.added)
                .append(this.updated, other.updated)
                .append(this.removedNames, other.removedNames)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("added", added.stream().map(UnitData::getName).collect(Collectors.joining(",")))
                .append("updated", updated.stream().map(UnitData::getName).collect(Collectors.joining(",")))
                .append("removed", removedNames)
                .toString();
    }

}
