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
import org.eclipse.mosaic.fed.sumo.traci.CommandRegister;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveLaneValue;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.Locale;

public class LaneGetLength extends AbstractTraciCommand<Double> {

    /**
     * Creates a new {@link LaneGetLength} object.
     * Called by {@link CommandRegister#getOrCreate(java.lang.Class)}.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Lane_Value_Retrieval.html">Lane Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public LaneGetLength() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveLaneValue.COMMAND)
                .variable(CommandRetrieveLaneValue.VAR_LENGTH)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .readDoubleWithType();
    }

    /**
     * This method executes the command with the given arguments and returns the length of the lane.
     *
     * @param traciCon  Connection to Traci.
     * @param edgeId    Id of the edge.
     * @param laneIndex Id of the lane.
     * @return The length of the lane.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public Double execute(TraciConnection traciCon, String edgeId, int laneIndex) throws TraciCommandException, InternalFederateException {
        return super.executeAndReturn(traciCon, edgeId + "_" + laneIndex).orElseThrow(
                () -> new TraciCommandException(
                        String.format(Locale.ENGLISH, "Couldn't get Length of Edge %s on Lane %d.", edgeId, laneIndex),
                        new Status((byte) Status.STATUS_ERR,"")
                )
        );

    }

    @Override
    protected Double constructResult(Status status, Object... objects) {
        return (Double) objects[0];
    }
}
