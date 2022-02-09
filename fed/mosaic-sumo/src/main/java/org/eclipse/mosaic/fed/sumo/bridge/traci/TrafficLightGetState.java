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
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveTrafficLightValue;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.Locale;

/**
 * This class retrieves the current state of the traffic light application.
 */
public class TrafficLightGetState
        extends AbstractTraciCommand<String>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightGetState {

    /**
     * Creates a new {@link TrafficLightGetState} object.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Traffic_Lights_Value_Retrieval.html">Traffic Lights Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public TrafficLightGetState() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveTrafficLightValue.COMMAND)
                .variable(CommandRetrieveTrafficLightValue.VAR_STATE)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .readStringWithType();
    }

    /**
     * This method executes the command with the given arguments in order to get the traffic light state.
     *
     * @param bridge Connection to SUMO.
     * @param tlId Id of the traffic light.
     * @return The current state of the traffic light program.
     * @throws CommandException     if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public String execute(Bridge bridge, String tlId) throws CommandException, InternalFederateException {
        return executeAndReturn(bridge, tlId).orElseThrow(
                () -> new CommandException(
                        String.format(Locale.ENGLISH, "Could not read State for TrafficLight %s.", tlId)
                )
        );
    }

    @Override
    protected String constructResult(Status status, Object... objects) {
        return (String) objects[0];
    }
}
