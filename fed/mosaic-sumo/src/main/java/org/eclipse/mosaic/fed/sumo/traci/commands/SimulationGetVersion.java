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
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandSimulationControl;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the traci command which allows to get the version of the Traci API.
 */
public class SimulationGetVersion extends AbstractTraciCommand<SimulationGetVersion.CurrentVersion> {

    /**
     * Creates a new {@link SimulationGetVersion} traci command.
     * Which will return the traci version number as an Integer.
     */
    @SuppressWarnings("WeakerAccess")
    public SimulationGetVersion() {
        super((TraciVersion) null);

        write()
                .command(CommandSimulationControl.COMMAND_VERSION);

        read()
                .expectByte(CommandSimulationControl.COMMAND_VERSION)
                .readInteger()
                .readString();
    }

    /**
     * This method executes the command with the given arguments in order to get the version of the Traci API.
     *
     * @param traciCon Connection to Traci.
     * @return Traci API version.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public CurrentVersion execute(TraciConnection traciCon) throws TraciCommandException, InternalFederateException {
        return executeAndReturn(traciCon).orElseThrow(
                () -> new TraciCommandException("Couldn't get Traci API Version", new Status((byte) Status.STATUS_ERR, ""))
        );
    }

    @Override
    protected CurrentVersion constructResult(Status status, Object... objects) {
        CurrentVersion currentVersion = new CurrentVersion();
        currentVersion.apiVersion = (Integer) objects[0];
        currentVersion.sumoVersion = (String) objects[1];
        return currentVersion;
    }

    public static class CurrentVersion {
        public String sumoVersion;
        public int apiVersion;
    }
}
