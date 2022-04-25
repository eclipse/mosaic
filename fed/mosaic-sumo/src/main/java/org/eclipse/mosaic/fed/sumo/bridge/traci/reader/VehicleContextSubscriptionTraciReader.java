/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.VehicleContextSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.VehicleSubscriptionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

public class VehicleContextSubscriptionTraciReader extends AbstractTraciResultReader<VehicleContextSubscriptionResult> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TypeBasedTraciReader typeBasedTraciReader = new TypeBasedTraciReader();

    public VehicleContextSubscriptionTraciReader() {
        super(null);
    }

    @Override
    protected VehicleContextSubscriptionResult readFromStream(DataInputStream in) throws IOException {
        VehicleContextSubscriptionResult result = new VehicleContextSubscriptionResult();
        result.id = Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(readString(in));
        VehicleSubscriptionTraciReader childTraciReader = new VehicleSubscriptionTraciReader();

        readUnsignedByte(in); // ignore context domain byte
        int varCount = readUnsignedByte(in);
        int objectCount = readInt(in);

        for (int o = 0; o < objectCount; o++) {
            VehicleSubscriptionResult childResult = new VehicleSubscriptionResult();
            childResult.id = Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(readString(in));
            for (int v = 0; v < varCount; v++) {
                int varId = readUnsignedByte(in);
                int varStatus = readUnsignedByte(in);

                Object varValue = typeBasedTraciReader.read(in, totalBytesLeft - numBytesRead);
                this.numBytesRead += typeBasedTraciReader.getNumberOfBytesRead();

                if (varStatus == 0x00) {
                    childTraciReader.handleSubscriptionVariable(childResult, varId, varValue);
                } else {
                    log.warn("Could not read subscription variable {}: {}", String.format("%02X ", varId), varValue);
                }
            }
            result.contextSubscriptions.add(childResult);
        }

        return result;
    }

}
