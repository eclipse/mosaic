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

import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * An interaction that contains updated traffic light groups information,
 * which was received based on subscriptions through TraciSimulationFacade#simulateUntil(time).
 **/
public class TrafficLightUpdates extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(TrafficLightUpdates.class);

    private final Map<String, TrafficLightGroupInfo> updatedTrafficLights;

    /**
     * Constructor for this interaction.
     *
     * @param time                 Timestamp of this interaction, unit: [ns]
     * @param updatedTrafficLights Instances of TrafficLightGroupInfo class
     *                             that represent the current states of subscribed TrafficLightGroups
     */
    public TrafficLightUpdates(final long time, Map<String, TrafficLightGroupInfo> updatedTrafficLights) {
        super(time);
        this.updatedTrafficLights = updatedTrafficLights;
    }

    public Map<String, TrafficLightGroupInfo> getUpdated() {
        return updatedTrafficLights;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 83)
                .append(updatedTrafficLights)
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

        TrafficLightUpdates other = (TrafficLightUpdates) obj;
        return new EqualsBuilder()
                .append(this.updatedTrafficLights, other.updatedTrafficLights)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("updatedTrafficLights", StringUtils.join(",", updatedTrafficLights))
                .toString();
    }
}
