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

package org.eclipse.mosaic.interactions.trafficsigns;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.trafficsign.TrafficSign;
import org.eclipse.mosaic.lib.util.gson.PolymorphismTypeAdapterFactory;
import org.eclipse.mosaic.rti.api.Interaction;

import com.google.gson.annotations.JsonAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} informs about a new traffic sign added to the simulation.
 */
public final class TrafficSignRegistration extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(TrafficSignRegistration.class);

    /**
     * The added traffic sign.
     */
    @JsonAdapter(PolymorphismTypeAdapterFactory.class)
    private final TrafficSign trafficSign;

    /**
     * Creates a new interaction that informs about a new added traffic sign.
     *
     * @param time        Timestamp of this interaction, unit: [ns]
     * @param trafficSign the added traffic sign
     */
    public TrafficSignRegistration(final long time, final TrafficSign trafficSign) {
        super(time);
        this.trafficSign = trafficSign;
    }

    public TrafficSign getTrafficSign() {
        return trafficSign;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(trafficSign)
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

        TrafficSignRegistration other = (TrafficSignRegistration) obj;
        return new EqualsBuilder()
                .append(trafficSign, other.trafficSign)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("trafficSign", trafficSign)
                .toString();
    }
}
