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
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.fed.sumo.traci.reader.ListTraciReader;
import org.eclipse.mosaic.fed.sumo.traci.reader.StringTraciReader;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;
import java.util.Locale;

/**
 * This class represents the traci command which allows to get the controlled lanes by traffic light apps.
 */
public class TrafficLightGetControlledLanes extends AbstractTraciCommand<List<String>> {

    /**
     * Creates a new {@link TrafficLightGetControlledLanes} object.
     */
    public TrafficLightGetControlledLanes() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveTrafficLightValue.COMMAND)
                .variable(CommandRetrieveTrafficLightValue.VAR_CONTROLLED_LANES)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .expectByte(TraciDatatypes.STRING_LIST)
                .readComplex(new ListTraciReader<>(new StringTraciReader()));
    }

    /**
     * This method executes the command with the given arguments in order to get the controlled lines in the traffic light simulations.
     *
     * @param traciConnection Connection to Traci.
     * @param tlId            Id of the traffic light.
     * @return List of the traffic light Id's
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public List<String> execute(TraciConnection traciConnection, String tlId) throws TraciCommandException, InternalFederateException {
        return super.executeAndReturn(traciConnection, tlId).orElseThrow(
                () -> new TraciCommandException(String.format(
                        Locale.ENGLISH, "Couldn't get controlled Lanes for TrafficLight %s.", tlId),
                        new Status((byte) Status.STATUS_ERR, "")
                )
        );
    }

    @Override
    protected List<String> constructResult(Status status, Object... objects) {
        return (List<String>) objects[0];
    }
}
