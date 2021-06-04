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

package org.eclipse.mosaic.fed.sumo.traci.writer;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciParameterWriter;
import org.eclipse.mosaic.fed.sumo.traci.complex.SumoTrafficLightLogic;
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;

import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficLightPhaseWriter extends AbstractTraciParameterWriter<SumoTrafficLightLogic.Phase> {

    private final StringTraciWriter stringWriter = new StringTraciWriter();

    public TrafficLightPhaseWriter() {
        super(((1 + 4) + (1 + 8) + 1 + (1 + 8) + (1 + 8) + (1 + 4)) + (1 + 4));
    }

    @Override
    public int getVariableLength(SumoTrafficLightLogic.Phase argument) {
        return getLength() + stringWriter.getVariableLength(argument.getPhaseDef());
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        writeVariableArgument(out, value);
    }

    @Override
    public void writeVariableArgument(DataOutputStream out, SumoTrafficLightLogic.Phase argument) throws IOException {
        out.writeByte(TraciDatatypes.COMPOUND);
        out.writeInt(6);

        out.writeByte(TraciDatatypes.DOUBLE);
        out.writeDouble(argument.getDuration() / 1000d);

        out.writeByte(TraciDatatypes.STRING);
        stringWriter.writeVariableArgument(out, argument.getPhaseDef());

        out.writeByte(TraciDatatypes.DOUBLE);
        out.writeDouble(-1d);
        out.writeByte(TraciDatatypes.DOUBLE);
        out.writeDouble(-1d);
        out.writeByte(TraciDatatypes.COMPOUND);
        out.writeInt(0);

        out.writeByte(TraciDatatypes.STRING);
        stringWriter.writeVariableArgument(out, "");
    }
}
