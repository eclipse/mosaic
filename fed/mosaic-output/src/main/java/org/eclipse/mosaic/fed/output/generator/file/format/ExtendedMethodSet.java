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

package org. eclipse.mosaic.fed.output.generator.file.format;

import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.objects.v2x.GenericV2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.Interaction;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class ExtendedMethodSet {

    private final static SortedMap<Integer, V2xMessage> V2X_MESSAGES = new TreeMap<>();

    public static void deleteV2xMessage(int messageId) {
        V2X_MESSAGES.remove(messageId);
    }

    public static void putV2xMessage(V2xMessage message) {
        V2X_MESSAGES.put(message.getId(), message);
    }

    static public Object getType(V2xMessageReception interaction) {
        V2xMessage message = Objects.requireNonNull(V2X_MESSAGES.get(interaction.getMessageId()));
        if (message instanceof GenericV2xMessage) {
            return ((GenericV2xMessage) message).getMessageType();
        }
        return message.getSimpleClassName();
    }

    static public Object getType(V2xMessageTransmission interaction) {
        V2xMessage message = Objects.requireNonNull(V2X_MESSAGES.get(interaction.getMessageId()));
        if (message instanceof GenericV2xMessage) {
            return ((GenericV2xMessage) message).getMessageType();
        }
        return message.getSimpleClassName();
    }

    static public Object getTimeInSec(Interaction interaction) {
        return interaction == null ? "" : interaction.getTime() / TIME.SECOND;
    }

    static public Object getTimeInMs(Interaction interaction) {
        return interaction == null ? "" : interaction.getTime() / TIME.MILLI_SECOND;
    }

}
