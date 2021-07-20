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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandSimulationControl;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the SUMO command which closes the simulation.
 */
public class SimulationClose
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.SimulationClose {

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
     * @param bridge Connection to SUMO.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge) throws CommandException, InternalFederateException {
        super.execute(bridge);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
