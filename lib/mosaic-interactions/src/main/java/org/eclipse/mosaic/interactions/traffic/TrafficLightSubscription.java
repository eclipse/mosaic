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
 * This extension of {@link Interaction} is sent when a unit wants to subscribe for data of a SUMO traffic light
 * (represented by {@link org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup}).
 **/
public class TrafficLightSubscription extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(org.eclipse.mosaic.interactions.traffic.TrafficLightSubscription.class);

    /**
     * The identifier of the traffic light group.
     */
    private String trafficLightGroupId;

    /**
     * Creates a new interaction that subscribes for data of a SUMO traffic light group.
     *
     * @param time       Timestamp of this interaction, unit: [ns]
     * @param trafficLightGroupId The identifier of the traffic light group.
     */
    public TrafficLightSubscription(long time, String trafficLightGroupId) {
        super(time);
        this.trafficLightGroupId = trafficLightGroupId;
    }

    public String getTrafficLightGroupId() {
        return trafficLightGroupId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 37)
                .append(trafficLightGroupId)
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

        TrafficLightSubscription other = (TrafficLightSubscription) obj;
        return new EqualsBuilder()
                .append(this.trafficLightGroupId, other.trafficLightGroupId)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("trafficLightGroupId", trafficLightGroupId)
                .toString();
    }

}
