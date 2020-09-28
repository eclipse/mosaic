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

public class TrafficLightGetTimeOfNextSwitch extends AbstractTraciCommand<Double> {

    /**
     * Creates a new {@link TrafficLightSetProgram} object.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Traffic_Lights_Value_Retrieval.html">Traffic Lights Value Retrieval</a>
     */
    public TrafficLightGetTimeOfNextSwitch() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveTrafficLightValue.COMMAND)
                .variable(CommandRetrieveTrafficLightValue.VAR_TIME_OF_NEXT_SWITCH)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .readDoubleWithType();
    }

    public double execute(TraciConnection con, String tlId) throws TraciCommandException, InternalFederateException {
        return executeAndReturn(con, tlId).orElseThrow(
                () -> new TraciCommandException(
                        String.format(Locale.ENGLISH, "Couldn't get Time of next switch for TrafficLight %s.", tlId),
                        new Status((byte) Status.STATUS_ERR, "")
                )
        );
    }

    @Override
    protected Double constructResult(Status status, Object... objects) {
        return (Double) objects[0];
    }
}
