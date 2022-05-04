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

package org.eclipse.mosaic.lib.objects.v2x.etsi;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.etsi.ivim.Segment;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Infrastructure to Vehicle Information  Message.
 */
@Immutable
public class Ivim extends V2xMessage {

    private static final long serialVersionUID = 1L;

    /**
     * The encoded message.
     */
    private final EncodedPayload payload;

    private final IvimContent ivimContent;

    @Override
    @Nonnull
    public EncodedPayload getPayLoad() {
        return payload;
    }

    public Ivim(final MessageRouting routing, IvimContent ivimContent, long minimalPayloadLength) {
        super(routing);
        this.ivimContent = ivimContent;

        if (EtsiPayloadConfiguration.getPayloadConfiguration().encodePayloads) {
            payload = new EncodedPayload(ivimContent, minimalPayloadLength);
        } else {
            payload = new EncodedPayload(0, minimalPayloadLength);
        }
    }

    public Ivim(final MessageRouting routing, final Ivim ivim, long minimalPayloadLength) {
        this(routing, ivim.ivimContent, minimalPayloadLength);
    }

    public List<Segment> getSegments() {
        return ivimContent.getSegments();
    }

    /**
     * Returns the raw object transferred with this message.
     */
    public IvimContent getIvimContent() {
        return ivimContent;
    }
}
