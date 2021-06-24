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

package org.eclipse.mosaic.fed.sumo.bridge.facades;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.api.RouteAdd;
import org.eclipse.mosaic.fed.sumo.bridge.api.RouteGetEdges;
import org.eclipse.mosaic.fed.sumo.bridge.api.RouteGetIds;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

public class RouteFacade {

    private final Bridge bridge;

    private final RouteGetIds routeGetIds;
    private final RouteAdd routeAdd;
    private final RouteGetEdges routeGetEdges;

    /**
     * Constructor with TraCI connection.
     *
     * @param bridge connection for communicating with TraCI.
     */
    public RouteFacade(Bridge bridge) {
        this.bridge = bridge;

        this.routeAdd = bridge.getCommandRegister().getOrCreate(RouteAdd.class);
        this.routeGetEdges = bridge.getCommandRegister().getOrCreate(RouteGetEdges.class);
        this.routeGetIds = bridge.getCommandRegister().getOrCreate(RouteGetIds.class);
    }

    /**
     * Adds a new route to the simulation. The given id must not be
     * assigned to another route.
     *
     * @param routeId the id of the new route
     * @param edges   list of edges of the new route
     * @throws InternalFederateException if the wanted route could not be added to simulation
     */
    public final void addRoute(String routeId, List<String> edges) throws InternalFederateException {
        try {
            routeAdd.execute(bridge, routeId, edges);
        } catch (CommandException e) {
            throw new InternalFederateException(String.format("Could not add route '%s'", routeId), e);
        }
    }

    /**
     * Returns the list of edges assigned to the given route id.
     *
     * @param routeId the id of the route, must be existing
     * @return a list of edges
     * @throws InternalFederateException if the edges from the wanted route could not be retrieved
     */
    public final List<String> getRouteEdges(String routeId) throws InternalFederateException {
        try {
            return routeGetEdges.execute(bridge, routeId);
        } catch (CommandException e) {
            throw new InternalFederateException(String.format("Could not retrieve route edges for route '%s'", routeId), e);
        }
    }

    /**
     * Returns the list of loaded route ids.
     *
     * @return a list of route ids
     * @throws InternalFederateException if it wasn't possible to retrieve the list of loaded route ids
     */
    public List<String> getRouteIds() throws InternalFederateException {
        try {
            return routeGetIds.execute(bridge);
        } catch (CommandException e) {
            throw new InternalFederateException("Could not retrieve list of loaded route ids", e);
        }
    }
}
