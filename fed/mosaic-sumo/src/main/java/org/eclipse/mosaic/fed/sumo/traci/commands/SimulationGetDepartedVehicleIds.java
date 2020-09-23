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
 */

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveSimulationValue;
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.fed.sumo.traci.reader.ListTraciReader;
import org.eclipse.mosaic.fed.sumo.traci.reader.VehicleIdTraciReader;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

/**
 * This class represents the traci command which allows to get the Id's of the vehicles departed the simulation.
 */
public class SimulationGetDepartedVehicleIds extends AbstractTraciCommand<List<String>> {

    /**
     * Creates a new {@link SimulationGetDepartedVehicleIds} traci command,
     * which will return a list of all departed vehicles once executed.
     * Access needs to be public, because command is called using Reflection.
     */
    @SuppressWarnings("WeakerAccess")
    public SimulationGetDepartedVehicleIds() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveSimulationValue.COMMAND)
                .variable(CommandRetrieveSimulationValue.VAR_DEPARTED_VEHICLES)
                .writeString("0");

        read()
                .skipBytes(2)
                .skipString()
                .expectByte(TraciDatatypes.STRING_LIST)
                .readComplex(new ListTraciReader<>(new VehicleIdTraciReader()));
    }

    /**
     * This method executes the command with the given arguments in order to get the vehicles Id's
     * in the simulation, which departed the simulation.
     *
     * @param con Connection to Traci.
     * @return List of vehicle Id's.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public List<String> execute(TraciConnection con) throws TraciCommandException, InternalFederateException {
        return executeAndReturn(con).orElseThrow(
                () -> new TraciCommandException("Couldn't get departed Vehicles.", new Status((byte) Status.STATUS_ERR, ""))
        );
    }

    @Override
    protected List<String> constructResult(Status status, Object... objects) {
        return (List<String>) objects[0];
    }
}
