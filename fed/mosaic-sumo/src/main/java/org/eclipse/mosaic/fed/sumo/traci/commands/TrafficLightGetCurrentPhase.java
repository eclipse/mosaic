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

public class TrafficLightGetCurrentPhase extends AbstractTraciCommand<Integer> {

    /**
     * Creates a {@link TrafficLightGetCurrentPhase} traci command.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Traffic_Lights_Value_Retrieval.html">Traffic Lights Value Retrieval</a>
     */
    public TrafficLightGetCurrentPhase() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveTrafficLightValue.COMMAND)
                .variable(CommandRetrieveTrafficLightValue.VAR_CURRENT_PHASE_INDEX)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .readIntegerWithType();
    }

    public int execute(TraciConnection con, String tlId) throws TraciCommandException, InternalFederateException {
        return executeAndReturn(con, tlId).orElseThrow(
                () -> new TraciCommandException(
                        String.format(Locale.ENGLISH, "Couldn't get current Phase for TrafficLight: %s", tlId),
                        new Status((byte) Status.STATUS_ERR, "")
                )
        );
    }

    @Override
    protected Integer constructResult(Status status, Object... objects) {
        return (Integer) objects[0];
    }
}
