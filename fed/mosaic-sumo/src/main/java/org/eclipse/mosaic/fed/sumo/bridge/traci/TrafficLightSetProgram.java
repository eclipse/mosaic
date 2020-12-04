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
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandChangeTrafficLightState;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the traci command which allows to set the traffic light program.
 */
public class TrafficLightSetProgram
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightSetProgram {

    /**
     * Creates a new {@link TrafficLightSetProgram} object.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Traffic_Lights_State.html">Traffic Lights State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public TrafficLightSetProgram() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangeTrafficLightState.COMMAND)
                .variable(CommandChangeTrafficLightState.VAR_PROGRAM_ID)
                .writeStringParam()
                .writeStringParamWithType();
    }

    /**
     * This method executes the command with the given arguments in order to set the traffic light program.
     *
     * @param con       Connection to Traci.
     * @param tlId      Id of the traffic light.
     * @param programId Id of the traffic light program.
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge con, String tlId, String programId) throws CommandException, InternalFederateException {
        super.execute(con, tlId, programId);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
