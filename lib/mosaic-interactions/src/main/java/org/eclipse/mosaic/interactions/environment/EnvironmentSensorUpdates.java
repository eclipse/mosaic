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

package org.eclipse.mosaic.interactions.environment;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.environment.EnvironmentEvent;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;
import java.util.Collections;

/**
 * This extension of {@link Interaction} is intended to be used to exchange sensor data.
 */
public final class EnvironmentSensorUpdates extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(EnvironmentSensorUpdates.class);

    /**
     * String identifying a unit.
     */
    private final String unitId;

    /**
     * Collection of all environment events until the next update.
     */
    private final Collection<EnvironmentEvent> events;

    /**
     * The constructor for the interaction.
     *
     * @param time   Timestamp of this interaction, unit: [ns]
     * @param unitId The id of the unit, which sensors have been updated.
     * @param events The events holding information about the updated.
     */
    public EnvironmentSensorUpdates(long time, String unitId, Collection<EnvironmentEvent> events) {
        super(time);
        this.unitId = unitId;
        this.events = Collections.unmodifiableCollection(events);
    }

    public Collection<EnvironmentEvent> getEvents() {
        return this.events;
    }

    public String getUnitId() {
        return unitId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 13)
                .append(getId())
                .append(getTime())
                .append(unitId)
                .append(events)
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

        EnvironmentSensorUpdates other = (EnvironmentSensorUpdates) obj;
        return new EqualsBuilder()

                .append(this.unitId, other.unitId)
                .append(this.events, other.events)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("unitId", unitId)
                .append("events", events)
                .toString();
    }

}

