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
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.AwarenessData;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Cooperative Awareness Message (CAM) are status information messages about the traffic flow
 * that is shared between simulation entities (vehicles, roadside units (RSU) or traffic control centre).
 */
@Immutable
public class Cam extends V2xMessage {
    private static final long serialVersionUID = 1L;

    /**
     * The encoded message.
     */
    private final EncodedPayload payLoad;

    @Override
    @Nonnull
    public EncodedPayload getPayLoad() {
        return payLoad;
    }

    private final CamContent camContent;

    /**
     * Creates a new {@link MessageRouting} object.
     *
     * @param routing    Message to be routed.
     * @param camContent Content of a CAM Message.
     */
    public Cam(final MessageRouting routing, final CamContent camContent, long minimalPayloadLength) {
        super(routing);
        Objects.requireNonNull(camContent);

        this.camContent = camContent;

        if (EtsiPayloadConfiguration.getPayloadConfiguration().encodePayloads) {
            payLoad = new EncodedPayload(camContent, minimalPayloadLength);
        } else {
            payLoad = new EncodedPayload(0, minimalPayloadLength);
        }
    }

    public Cam(final MessageRouting routing, final Cam cam, long minimalCamLength) {
        this(routing, new CamContent(cam.camContent), minimalCamLength);
    }

    public long getGenerationTime() {
        return camContent.getGenerationTime();
    }

    public String getUnitID() {
        return camContent.getUnitId();
    }

    public GeoPoint getPosition() {
        return camContent.getPosition();
    }

    @Nullable
    public byte[] getUserTaggedValue() {
        return camContent.getUserTaggedValue();
    }

    public AwarenessData getAwarenessData() {
        return camContent.getAwarenessData();
    }

}
