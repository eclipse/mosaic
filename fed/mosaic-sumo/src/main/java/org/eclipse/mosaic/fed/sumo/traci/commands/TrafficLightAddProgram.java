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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.complex.SumoTrafficLightLogic;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandChangeTrafficLightState;
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.fed.sumo.traci.writer.ListTraciWriter;
import org.eclipse.mosaic.fed.sumo.traci.writer.TrafficLightPhaseWriter;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

/**
 * Adds a complete definition of a traffic light program. Does only work with SUMO >= 1.1.0.
 */
public class TrafficLightAddProgram extends AbstractTraciCommand<Void> {

    /**
     * Creates a {@link TrafficLightAddProgram} traci command.
     * Access needs to be public, because command is called using Reflection.
     */
    @SuppressWarnings("WeakerAccess")
    public TrafficLightAddProgram() {
        super(TraciVersion.API_20);

        write()
                .command(CommandChangeTrafficLightState.COMMAND)
                .variable(CommandChangeTrafficLightState.VAR_COMPLETE_PROGRAM)
                .writeStringParam()
                .writeByte(TraciDatatypes.COMPOUND)
                .writeInt(5)
                .writeStringParamWithType() // program id
                .writeByte(TraciDatatypes.INTEGER)
                .writeInt(0)
                .writeIntParamWithType() // phase index
                .writeByte(TraciDatatypes.COMPOUND)
                .writeComplex(new ListTraciWriter<>(new TrafficLightPhaseWriter())) // phases
                .writeByte(TraciDatatypes.COMPOUND)
                .writeInt(0);
    }

    public void execute(TraciConnection con, String tlId, String programId, int phaseIndex, List<SumoTrafficLightLogic.Phase> phases) throws TraciCommandException, InternalFederateException {
        super.execute(con, tlId, programId, phaseIndex, phases);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
