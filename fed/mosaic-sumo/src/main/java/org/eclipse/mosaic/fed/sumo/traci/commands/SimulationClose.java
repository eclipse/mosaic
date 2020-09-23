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
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandSimulationControl;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the traci command which closes the simulation.
 */
public class SimulationClose extends AbstractTraciCommand<Void> {

    /**
     * Creates a new {@link SimulationClose} object.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Control-related_commands.html>Control-Related Commands</a>
     */
    public SimulationClose() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandSimulationControl.COMMAND_CLOSE);
    }

    /**
     * This method executes the command with the given arguments and it leads to closing the simulation.
     *
     * @param traciCon Connection to Traci.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(TraciConnection traciCon) throws TraciCommandException, InternalFederateException {
        super.execute(traciCon);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
