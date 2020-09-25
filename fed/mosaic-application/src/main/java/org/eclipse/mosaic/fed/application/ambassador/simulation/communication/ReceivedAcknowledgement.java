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

package org.eclipse.mosaic.fed.application.ambassador.simulation.communication;

import org.eclipse.mosaic.lib.enums.NegativeAckReason;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReceivedAcknowledgement {


    /**
     * The sent V2XMessage for which an acknowledgement was received.
     */
    private final V2xMessage sentMessage;

    private final List<NegativeAckReason> negativeAckReasons = new ArrayList<>();

    public ReceivedAcknowledgement(@Nonnull final V2xMessage sentMessage, @Nullable List<NegativeAckReason> negativeAckReasons) {
        this.sentMessage = sentMessage;
        if (negativeAckReasons != null) {
            this.negativeAckReasons.addAll(negativeAckReasons);
        }
    }

    /**
     * Returns the sent V2XMessage for which an acknowledgement was received.
     * @return the sent V2XMessage for which an acknowledgement was received.
     */
    @Nonnull
    public V2xMessage getSentMessage() {
        return sentMessage;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 59)
                .append(sentMessage)
                .append(negativeAckReasons)
                .toHashCode();
    }

    public boolean isAcknowledged() {
        return negativeAckReasons.isEmpty();
    }

    public List<NegativeAckReason> getNegativeAckReasons() {
        return negativeAckReasons;
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

        ReceivedAcknowledgement rhs = (ReceivedAcknowledgement) obj;
        return new EqualsBuilder()
                .append(this.sentMessage, rhs.sentMessage)
                .append(this.negativeAckReasons, rhs.negativeAckReasons)
                .isEquals();
    }

    @Override
    public String toString() {
        return "ReceivedAcknowledgement{" + "sentMessage=" + sentMessage + ", negativeAckReasons=" + negativeAckReasons + '}';
    }

}
