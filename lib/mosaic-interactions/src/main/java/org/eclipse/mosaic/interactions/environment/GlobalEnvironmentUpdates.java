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

import org.eclipse.mosaic.lib.objects.environment.EnvironmentEventLocation;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * This extension of {@link Interaction} contains a list of current environment events and their locations.
 * Those events can than be used to react upon a changing environment.
 */
public final class GlobalEnvironmentUpdates extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(GlobalEnvironmentUpdates.class);

    /**
     * String identifying the current environment event locations.
     */
    private final List<EnvironmentEventLocation> currentEvents;

    /**
     * Constructor for {@link GlobalEnvironmentUpdates}.
     *
     * @param time          Timestamp of this interaction, unit: [ns]
     * @param currentEvents the payload of the interaction, a list of {@link EnvironmentEventLocation}s
     */
    public GlobalEnvironmentUpdates(long time, List<EnvironmentEventLocation> currentEvents) {
        super(time);
        this.currentEvents = currentEvents;
    }

    public List<EnvironmentEventLocation> getCurrentEvents() {
        return this.currentEvents;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 41)
                .append(getId())
                .append(getTime())
                .append(currentEvents)
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

        GlobalEnvironmentUpdates other = (GlobalEnvironmentUpdates) obj;
        return new EqualsBuilder()

                .append(this.currentEvents, other.currentEvents)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("currentEvents", currentEvents)
                .toString();
    }

}
