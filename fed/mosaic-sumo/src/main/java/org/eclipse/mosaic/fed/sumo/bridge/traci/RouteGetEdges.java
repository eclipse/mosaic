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
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveRouteValue;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.ListTraciReader;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.StringTraciReader;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;
import java.util.Locale;

/**
 * This class represents the SUMO command which allows to get the edge Id's of a route.
 */
public class RouteGetEdges
        extends AbstractTraciCommand<List<String>>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.RouteGetEdges {

    /**
     * Creates a new {@link RouteGetEdges} traci command,
     * which will return all edge ids along a given route
     * once executed.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Route_Value_Retrieval.html">Route Value Retrieval</a>
     * Access needs to be public, because command is called using Reflection.
     */
    @SuppressWarnings("WeakerAccess")
    public RouteGetEdges() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveRouteValue.COMMAND)
                .variable(CommandRetrieveRouteValue.VAR_EDGES)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .expectByte(TraciDatatypes.STRING_LIST)
                .readComplex(new ListTraciReader<>(new StringTraciReader()));
    }

    /**
     * This method executes the command with the given arguments and returns a list included edge Id's.
     *
     * @param bridge  Connection to SUMO.
     * @param routeId Id of the route.
     * @return List of edges.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public List<String> execute(Bridge bridge, String routeId) throws CommandException, InternalFederateException {
        return super.executeAndReturn(bridge, routeId).orElseThrow(
                () -> new CommandException(
                        String.format(Locale.ENGLISH, "Couldn't extract Edges of Route %s.", routeId)
                )
        );
    }

    @Override
    protected List<String> constructResult(Status status, Object... objects) {
        return (List<String>) objects[0];
    }
}
