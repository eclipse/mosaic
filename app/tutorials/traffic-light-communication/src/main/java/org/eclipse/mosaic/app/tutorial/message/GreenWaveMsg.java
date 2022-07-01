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

package org.eclipse.mosaic.app.tutorial.message;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.util.objects.Position;

import javax.annotation.Nonnull;

public final class GreenWaveMsg extends V2xMessage {
    private final String         message;
    private final EncodedPayload payload;
    private final static long    MIN_LEN = 8L;

    public GreenWaveMsg(MessageRouting routing, String message) {
        super(routing);
        this.message = message;
        payload = new EncodedPayload(message.length(), MIN_LEN);
    }

    public String getMessage() {
        return message;
    }

    @Nonnull
    @Override
    public EncodedPayload getPayLoad() {
        return payload;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GreenWaveMsg{");
        sb.append("message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
