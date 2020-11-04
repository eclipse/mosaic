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

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.util.SerializationUtils;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.annotation.Nonnull;

@SuppressWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2", "CORRECTNESS"})
public class TestMessage extends V2xMessage {

    /**
     * The encoded message.
     */
    private final EncodedPayload payload;

    /**
     * The minimal size in bytes, which the message should have.
     */
    private static final int MINIMAL_LENGTH = 200;

    private final long timeStamp;

    private final String senderName;

    private final GeoPoint senderPosition;

    private final byte[] additionalPayload;

    /**
     * @param routing
     * @param timeStamp
     * @param senderName
     * @param senderPosition
     * @param additionalPayload Uninterpreted payload of a message
     */
    public TestMessage(
            final MessageRouting routing,
            final long timeStamp,
            final String senderName,
            final GeoPoint senderPosition,
            final byte[] additionalPayload
    ) {
        super(routing);
        this.timeStamp = timeStamp;
        this.senderName = senderName;
        this.senderPosition = senderPosition;
        this.additionalPayload = additionalPayload;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             final DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeLong(timeStamp);
            dos.writeUTF(senderName);
            SerializationUtils.encodeGeoPoint(dos, senderPosition);
            dos.writeInt(additionalPayload.length);
            dos.write(additionalPayload);

            payload = new EncodedPayload(baos.toByteArray(), baos.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TestMessage(final MessageRouting routing, final TestMessage testMessage) {
        this(routing, testMessage.getTimeStamp(), testMessage.getSenderName(), testMessage.getSenderPosition(), testMessage.getAdditionalPayload());
    }

    @Override
    @Nonnull
    public EncodedPayload getPayLoad() {
        return payload;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getSenderName() {
        return senderName;
    }

    public GeoPoint getSenderPosition() {
        return senderPosition;
    }

    public byte[] getAdditionalPayload() {
        return additionalPayload;
    }

    @Override
    public String toString() {
        return "TestMessage{" + "timeStamp=" + timeStamp + ", senderName=" + senderName + ", senderPosition=" + senderPosition + ", additionalPayload=" + additionalPayload + ", encodedPayload=" + payload + '}';
    }

}
