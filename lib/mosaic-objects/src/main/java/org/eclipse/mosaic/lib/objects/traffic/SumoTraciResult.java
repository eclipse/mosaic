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

package org.eclipse.mosaic.lib.objects.traffic;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Provides information for the result of a previously called SUMO command.
 */
@Immutable
public class SumoTraciResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String requestCommandId;
    private final byte commandStatus;
    private final String commandMessage;
    private final byte[] commandResult;

    /**
     * Constructs the {@link SumoTraciResult} based on the TraCI command being called.
     *
     * @param requestCommandId the ID of the originating command request
     * @param commandStatus    the byte status of the command as returned by TraCI
     * @param commandMessage   the text message of the command as returned by TraCI
     * @param result           the actual result byte array as returned by TraCI
     */
    public SumoTraciResult(
            @Nonnull final String requestCommandId,
            final byte commandStatus,
            @Nonnull final String commandMessage,
            @Nonnull final byte[] result
    ) {
        this.requestCommandId = Objects.requireNonNull(requestCommandId);
        this.commandStatus = commandStatus;
        this.commandMessage = Objects.requireNonNull(commandMessage);
        Objects.requireNonNull(result);
        this.commandResult = result.clone();
    }

    /**
     * Returns the ID of the request which corresponds to this result.
     *
     * @return the ID of the originating TraCI request
     */
    @Nonnull
    public String getRequestCommandId() {
        return requestCommandId;
    }

    public byte getTraciCommandStatus() {
        return commandStatus;
    }

    @Nonnull
    public String getTraciCommandMessage() {
        return commandMessage;
    }

    @Nonnull
    public byte[] getTraciCommandResult() {
        return commandResult.clone();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 43)
                .append(requestCommandId)
                .append(commandStatus)
                .append(commandMessage)
                .append(commandResult)
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

        SumoTraciResult rhs = (SumoTraciResult) obj;
        return new EqualsBuilder()
                .append(this.requestCommandId, rhs.requestCommandId)
                .append(this.commandStatus, rhs.commandStatus)
                .append(this.commandMessage, rhs.commandMessage)
                .append(this.commandResult, rhs.commandResult)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("requestId", requestCommandId)
                .append("commandStatus", commandStatus)
                .append("commandMessage", commandMessage)
                .append("commandResult", BaseEncoding.base16().encode(commandResult))
                .toString();
    }

}

