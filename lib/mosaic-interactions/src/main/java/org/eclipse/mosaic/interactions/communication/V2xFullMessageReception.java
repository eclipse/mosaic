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

package org.eclipse.mosaic.interactions.communication;

import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.V2xReceiverInformation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

/**
 * This extension of {@link V2xMessageReception} is intended to be used to
 * exchange information about a received V2X message, which is generated
 * out of the application simulator and not added to the according cache.
 * Therefore, it also holds the full information of msg (message payload).
 */
public final class V2xFullMessageReception extends V2xMessageReception {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(V2xFullMessageReception.class);

    /**
     * V2X message itself.
     */
    private final V2xMessage message;

    /**
     * Constructor using fields.
     *
     * @param time                Timestamp of this interaction, unit: [ns]
     * @param receiverName        Identifies the receiving node.
     * @param v2XMessage          The V2X message.
     * @param receiverInformation Additional receiver information. <code>null</code> if no additional information given.
     */
    public V2xFullMessageReception(final long time,
                                   @Nonnull final String receiverName,
                                   @Nonnull final V2xMessage v2XMessage,
                                   @Nonnull final V2xReceiverInformation receiverInformation
    ) {
        super(time, receiverName, v2XMessage.getId(), receiverInformation);
        this.message = v2XMessage;
    }

    public V2xMessage getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 53)
                .appendSuper(super.hashCode())
                .append(message)
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

        V2xFullMessageReception rhs = (V2xFullMessageReception) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.message, rhs.message)
                .isEquals();
    }
}
