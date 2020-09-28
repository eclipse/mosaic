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

package org.eclipse.mosaic.fed.sumo.traci.reader;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciResultReader;
import org.eclipse.mosaic.fed.sumo.traci.complex.InductionLoopVehicleData;
import org.eclipse.mosaic.rti.TIME;

import java.io.DataInputStream;
import java.io.IOException;

public class InductionLoopVehicleDataTraciReader extends AbstractTraciResultReader<InductionLoopVehicleData> {

    private final VehicleIdTraciReader vehicleIdTraciReader = new VehicleIdTraciReader();

    /**
     * Creates a {@link InductionLoopVehicleDataTraciReader}.
     * Used to read the vehicle data from a induction loop.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Induction_Loop_Value_Retrieval.html#response_0xb0_induction_loop_variable">
     * Induction Loop Variable</a>
     */
    @SuppressWarnings("WeakerAccess")
    public InductionLoopVehicleDataTraciReader() {
        super(null);
    }

    @Override
    protected InductionLoopVehicleData readFromStream(DataInputStream in) throws IOException {
        InductionLoopVehicleData inductionLoopVehicleData = new InductionLoopVehicleData();
        inductionLoopVehicleData.vehicleId = readVehicleIdWithType(in);
        readDoubleWithType(in); // vehicle length, currently not required
        inductionLoopVehicleData.entryTime = (long) (readDoubleWithType(in) * TIME.SECOND);
        inductionLoopVehicleData.leaveTime = (long) (readDoubleWithType(in) * TIME.SECOND);
        readStringWithType(in); // vehicle type, currently not required
        return inductionLoopVehicleData;
    }

    private String readVehicleIdWithType(DataInputStream in) throws IOException {
        readByte(in);
        final String vehicleId = vehicleIdTraciReader.read(in, totalBytesLeft - numBytesRead);
        numBytesRead += vehicleIdTraciReader.getNumberOfBytesRead();
        return vehicleId;
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
