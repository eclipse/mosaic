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
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveTrafficLightValue;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.Locale;

/**
 * This class retrieves the current traffic light program.
 */
public class TrafficLightGetCurrentProgram extends AbstractTraciCommand<String> {

    /**
     * Creates a new {@link TrafficLightGetCurrentProgram} object.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Traffic_Lights_Value_Retrieval.html">Traffic Lights Value Retrieval</a>
     */
    public TrafficLightGetCurrentProgram() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveTrafficLightValue.COMMAND)
                .variable(CommandRetrieveTrafficLightValue.VAR_CURRENT_PROGRAM)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .readStringWithType();
    }

    /**
     * This method executes the command with the given arguments in order to get the current traffic light application.
     *
     * @param con  Connection to Traci.
     * @param tlId Id of the traffic light.
     * @return The traffic light application.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public String execute(TraciConnection con, String tlId) throws TraciCommandException, InternalFederateException {
        return executeAndReturn(con, tlId).orElseThrow(
                () -> new TraciCommandException(String.format(
                        Locale.ENGLISH, "Couldn't get current Program for TrafficLight: %s", tlId),
                        new Status((byte) Status.STATUS_ERR, "")
                )
        );
    }

    @Override
    protected String constructResult(Status status, Object... objects) {
        return (String) objects[0];
    }
}
