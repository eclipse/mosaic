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

package org.eclipse.mosaic.fed.sumo.bridge.traci.reader;

import org.eclipse.mosaic.fed.sumo.bridge.api.complex.AbstractSubscriptionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

public abstract class AbstractSubscriptionTraciReader<T extends AbstractSubscriptionResult> extends AbstractTraciResultReader<T> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AbstractSubscriptionTraciReader() {
        super(null);
    }

    private final TypeBasedTraciReader typeBasedTraciReader = new TypeBasedTraciReader();

    protected TypeBasedTraciReader getTypeBasedTraciReader() {
        return typeBasedTraciReader;
    }

    abstract T createSubscriptionResult(String id);

    @Override
    protected T readFromStream(DataInputStream in) throws IOException {
        T result = createSubscriptionResult(readString(in));

        int varCount = readUnsignedByte(in);

        for (int i = 0; i < varCount; i++) {
            int varId = readUnsignedByte(in);
            int varStatus = readUnsignedByte(in);

            Object varValue = typeBasedTraciReader.read(in, totalBytesLeft - numBytesRead);
            this.numBytesRead += typeBasedTraciReader.getNumberOfBytesRead();

            if (varStatus == 0x00) {
                handleSubscriptionVariable(result, varId, varValue);
            } else {
                log.warn("Could not read subscription variable {}: {}", String.format("%02X ", varId), varValue);
            }
        }

        return result;
    }

    protected abstract void handleSubscriptionVariable(T result, int varId, Object varValue);
}
