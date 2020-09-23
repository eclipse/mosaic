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
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveRouteValue;
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.fed.sumo.traci.reader.ListTraciReader;
import org.eclipse.mosaic.fed.sumo.traci.reader.StringTraciReader;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

public class RouteGetIds extends AbstractTraciCommand<List<String>> {

    /**
     * Constructs a {@link RouteGetIds} traci command, which
     * will return a list of ids of all loaded routes.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Route_Value_Retrieval.html">Route Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public RouteGetIds() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveRouteValue.COMMAND)
                .variable(CommandRetrieveRouteValue.VAR_ID_LIST)
                .writeString("0"); // will be ignored

        read()
                .skipBytes(2)
                .skipString()
                .expectByte(TraciDatatypes.STRING_LIST)
                .readComplex(new ListTraciReader<>(new StringTraciReader()));
    }

    public List<String> execute(TraciConnection traciCon) throws TraciCommandException, InternalFederateException {
        return super.executeAndReturn(traciCon).orElseThrow(
                () -> new TraciCommandException("Couldn't get Route-Id's.", new Status((byte) Status.STATUS_ERR, ""))
        );
    }

    @Override
    protected List<String> constructResult(Status status, Object... objects) {
        return (List<String>) objects[0];
    }
}
