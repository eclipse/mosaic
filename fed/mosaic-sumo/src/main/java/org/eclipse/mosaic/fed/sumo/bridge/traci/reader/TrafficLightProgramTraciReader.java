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

import org.eclipse.mosaic.fed.sumo.bridge.api.complex.SumoTrafficLightLogic;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrafficLightProgramTraciReader extends AbstractTraciResultReader<SumoTrafficLightLogic> {

    private final ListTraciReader<String> paramReader = new ListTraciReader<>(new StringTraciReader(), true);

    public TrafficLightProgramTraciReader() {
        super(null);
    }

    @Override
    protected SumoTrafficLightLogic readFromStream(DataInputStream in) throws IOException {
        readIntWithType(in); // compound of 5

        final String logicId = readStringWithType(in);
        readIntWithType(in); // type, currently not yet implemented by SUMO (21.03.2020)
        final int phaseIndex = readIntWithType(in);
        final int numberOfPhases = readIntWithType(in);

        List<SumoTrafficLightLogic.Phase> phases = new ArrayList<>();

        for (int i = 0; i < numberOfPhases; i++) {
            int numberOfElements = readIntWithType(in);
            double durationS = readDoubleWithType(in);
            String phaseDefinition = readStringWithType(in);
            readDoubleWithType(in); // minDuration, in case of actuated traffic lights
            readDoubleWithType(in); // maxDuration, in case of actuated traffic lights
            int nextCount = readIntWithType(in);
            for (int n = 0; n < nextCount; n++) {
                readInt(in);
            }
            if (numberOfElements > 5) {
                readStringWithType(in); // name of the phase
            }
            phases.add(new SumoTrafficLightLogic.Phase((int) (durationS * 1000), phaseDefinition));
        }

        int numberOfParams = readIntWithType(in);
        for (int i = 0; i < numberOfParams; i++) { // each parameter is read using list reader
            paramReader.readFromStream(in); // key/value pair as list with two items
        }
        numBytesRead += paramReader.getNumberOfBytesRead();
        return new SumoTrafficLightLogic(logicId, phases, phaseIndex);
    }

    private int readIntWithType(DataInputStream in) throws IOException {
        readByte(in);
        return readInt(in);
    }

    private double readDoubleWithType(DataInputStream in) throws IOException {
        readByte(in);
        return readDouble(in);
    }

    private String readStringWithType(DataInputStream in) throws IOException {
        readByte(in);
        return readString(in);
    }
}
