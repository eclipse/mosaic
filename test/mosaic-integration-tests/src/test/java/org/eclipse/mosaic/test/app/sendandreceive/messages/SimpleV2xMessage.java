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

package org.eclipse.mosaic.test.app.sendandreceive.messages;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import javax.annotation.Nonnull;

public class SimpleV2xMessage extends V2xMessage {

    /**
     * The encoded message.
     */
    private final EncodedPayload encodedPayload;

    /**
     * Simple message taking a byte array as input for the payload.
     *
     * @param routing the {@link MessageRouting} for the message
     */
    public SimpleV2xMessage(MessageRouting routing) {
        super(routing);
        this.encodedPayload = new EncodedPayload(8);
    }

    @Nonnull
    @Override
    public EncodedPayload getPayLoad() {
        return encodedPayload;
    }
}
