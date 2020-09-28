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

package org.eclipse.mosaic.interactions.application;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.rti.api.Interaction;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.concurrent.Immutable;

/**
 * This extension of {@link Interaction} is used to send a byte array to SUMO TraCI. The request will
 * be handled by TraCI and trigger a {@link SumoTraciResponse}.
 */
@Immutable
public final class SumoTraciRequest extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(SumoTraciRequest.class);

    private final String requestId;
    private final byte[] command;

    /**
     * Constructor for {@link SumoTraciRequest}.
     *
     * @param time      Timestamp of this interaction, unit: [ns]
     * @param requestId Identifier of the request.
     * @param command   Byte array representation of the command to be sent to SUMO.
     */
    public SumoTraciRequest(final long time, final String requestId, final byte[] command) {
        super(time);
        this.requestId = requestId;
        this.command = command.clone();
    }

    public String getRequestId() {
        return requestId;
    }

    public byte[] getCommand() {
        return command.clone();
    }

    public int getCommandLength() {
        return command.length;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 23)
                .append(requestId)
                .append(command)
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

        SumoTraciRequest other = (SumoTraciRequest) obj;
        return new EqualsBuilder()
                .append(this.requestId, other.requestId)
                .append(this.command, other.command)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("requestId", requestId)
                .append("command", BaseEncoding.base16().encode(command))
                .toString();
    }
}

