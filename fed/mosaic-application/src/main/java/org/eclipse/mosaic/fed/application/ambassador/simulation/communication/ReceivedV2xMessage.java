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

package org.eclipse.mosaic.fed.application.ambassador.simulation.communication;

import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.V2xReceiverInformation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
public class ReceivedV2xMessage {

    /**
     * The received V2XMessage.
     */
    private final V2xMessage message;

    /**
     * The additional receiver information.
     */
    private final V2xReceiverInformation receiverInformation;

    public ReceivedV2xMessage(@Nonnull final V2xMessage message, @Nonnull final V2xReceiverInformation receiverInformation) {
        this.message = message;
        this.receiverInformation = receiverInformation;
    }


    /**
     * Returns the message.
     *
     * @return the message.
     */
    @Nonnull
    public V2xMessage getMessage() {
        return message;
    }

    /**
     * Returns the additional receiver information.
     *
     * @return the additional receiver information.
     */
    @Nonnull
    public V2xReceiverInformation getReceiverInformation() {
        return receiverInformation;
    }

    public long getTime() {
        return getReceiverInformation().getReceiveTime();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 59)
                .append(message)
                .append(receiverInformation)
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

        ReceivedV2xMessage rhs = (ReceivedV2xMessage) obj;
        return new EqualsBuilder()
                .append(this.message, rhs.message)
                .append(this.receiverInformation, rhs.receiverInformation)
                .isEquals();
    }

    @Override
    public String toString() {
        return "ReceivedV2XMessage{" + "message=" + message + ", receiverInformation=" + receiverInformation + '}';
    }

}
