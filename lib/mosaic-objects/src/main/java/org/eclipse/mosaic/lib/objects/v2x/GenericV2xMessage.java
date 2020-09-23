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

package org.eclipse.mosaic.lib.objects.v2x;

import org.eclipse.mosaic.lib.objects.ToDataOutput;

import javax.annotation.Nonnull;

/**
 * There is a need to define messages only in applications as the
 * message class might not be known within MOSAIC.
 */
public final class GenericV2xMessage extends V2xMessage {

    private static final long serialVersionUID = 1L;

    private final String classSimpleName;

    /**
     * The encoded payload.
     */
    private final EncodedPayload payload;

    public GenericV2xMessage(MessageRouting routing, ToDataOutput contentToEncode, long messageSize) {
        super(routing);
        this.classSimpleName = contentToEncode.getClass().getSimpleName();
        this.payload = new EncodedPayload(contentToEncode, messageSize);
    }


    @Override
    @Nonnull
    public EncodedPayload getPayLoad() {
        return payload;
    }

    @Override
    @Nonnull
    public String getSimpleClassName() {
        return classSimpleName;
    }

    @Override
    public String toString() {
        return "V2XMessageGeneralized{"
                + "classSimpleName=" + classSimpleName
                + ", encodedPayload=" + payload + '}';
    }

}
