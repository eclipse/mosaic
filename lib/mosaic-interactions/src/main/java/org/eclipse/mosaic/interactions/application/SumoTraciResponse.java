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

package org.eclipse.mosaic.interactions.application;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.traffic.SumoTraciResult;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.concurrent.Immutable;

/**
 * This extension of {@link Interaction} holds the TraCI response for a {@link SumoTraciRequest}. It is sent by the SumoAmbassador
 * and will usually be handled by the Application that sent the request.
 */
@Immutable
public final class SumoTraciResponse extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(SumoTraciResponse.class);

    /**
     * The response of SUMO.
     */
    private final SumoTraciResult sumoTraciResult;

    /**
     * Constructor for {@link SumoTraciResponse}.
     *
     * @param time            Timestamp of this interaction, unit: [ns]
     * @param sumoTraciResult The result of the request stored as {@link SumoTraciResult}.
     */
    public SumoTraciResponse(final long time, final SumoTraciResult sumoTraciResult) {
        super(time);
        this.sumoTraciResult = sumoTraciResult;
    }

    public SumoTraciResult getSumoTraciResult() {
        return sumoTraciResult;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 89)
                .append(sumoTraciResult)
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

        SumoTraciResponse other = (SumoTraciResponse) obj;
        return new EqualsBuilder()
                .append(this.getSumoTraciResult(), other.getSumoTraciResult())
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("result", sumoTraciResult)
                .toString();
    }
}

