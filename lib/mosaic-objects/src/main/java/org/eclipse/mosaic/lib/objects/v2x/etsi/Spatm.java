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

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import javax.annotation.Nonnull;

/**
 * Signal Phase and Timing Message
 */
public class Spatm extends V2xMessage {

    private static final long serialVersionUID = 1L;

    /**
     * The encoded message.
     */
    private EncodedPayload payload = null;

    @Override
    @Nonnull
    public EncodedPayload getPayLoad() {
        return payload;
    }

    private SpatmContent spatmContent;

    public Spatm(final MessageRouting routing, final SpatmContent spatmContent, long minimalPayloadLength) {
        super(routing);

        this.spatmContent = spatmContent;

        if (EtsiPayloadConfiguration.getPayloadConfiguration().encodePayloads) {
            payload = new EncodedPayload(spatmContent, minimalPayloadLength);
        } else {
            payload = new EncodedPayload(0, minimalPayloadLength);
        }
    }

    public Spatm(final MessageRouting routing, final Spatm spatm, long minimalPayloadLength) {
        this(routing, new SpatmContent(spatm.spatmContent), minimalPayloadLength);
    }

    public long getTime() {
        return this.spatmContent.getTime();
    }

    public GeoPoint getSenderPosition() {
        return this.spatmContent.getSenderPosition();
    }

    public boolean isPhaseRed() {
        return this.spatmContent.isPhaseRed();
    }

    public boolean isPhaseYellow() {
        return this.spatmContent.isPhaseYellow();
    }

    public boolean isPhaseGreen() {
        return this.spatmContent.isPhaseGreen();
    }

    public long getPhaseDurationRed() {
        return this.spatmContent.getPhaseDurationRed();
    }

    public long getPhaseDurationYellow() {
        return this.spatmContent.getPhaseDurationYellow();
    }

    public long getPhaseDurationGreen() {
        return this.spatmContent.getPhaseDurationGreen();
    }

    public long getRemainingPhaseTime() {
        return this.spatmContent.getRemainingPhaseTime();
    }
}
