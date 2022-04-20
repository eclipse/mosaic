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

package org.eclipse.mosaic.lib.objects.v2x;

import org.eclipse.mosaic.lib.objects.ToDataOutput;
import org.eclipse.mosaic.lib.util.ClassUtils;

import javax.annotation.Nonnull;

/**
 * This {@link V2xMessage} implementation can be used for simple message exchange between entities.
 */
public final class GenericV2xMessage extends V2xMessage {

    private static final long serialVersionUID = 1L;

    private final String messageType;

    /**
     * The encoded payload.
     */
    private final EncodedPayload payload;

    /**
     * Creates a {@link GenericV2xMessage} with a specific payload length.
     */
    public GenericV2xMessage(MessageRouting routing, long messageSize) {
        super(routing);
        this.messageType = ClassUtils.createShortClassName(getClass());
        this.payload = new EncodedPayload(messageSize, messageSize);
    }

    /**
     * Creates a {@link GenericV2xMessage} with a specific message type name (e.g. to identify different messages for
     * evaluation purposes), and a specific payload length.
     */
    public GenericV2xMessage(MessageRouting routing, String messageName, long messageSize) {
        super(routing);
        this.messageType = messageName;
        this.payload = new EncodedPayload(messageSize, messageSize);
    }

    /**
     * Creates a {@link GenericV2xMessage} carrying specific payload. The payload is encoded to a byte-array to determine
     * payload length.
     */
    public GenericV2xMessage(MessageRouting routing, ToDataOutput messagePayload, long minimalMessageSize) {
        super(routing);
        this.messageType = messagePayload.getClass().getSimpleName();
        this.payload = new EncodedPayload(messagePayload, minimalMessageSize);
    }


    @Override
    @Nonnull
    public EncodedPayload getPayLoad() {
        return payload;
    }

    @Nonnull
    public String getMessageType() {
        return messageType;
    }

    @Override
    public String toString() {
        return "GenericV2xMessage{"
                + "classSimpleName=" + messageType
                + ", encodedPayload=" + payload + '}';
    }

}
