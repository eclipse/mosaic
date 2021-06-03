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

package org.eclipse.mosaic.lib.routing.database;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.route.Route;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.IllegalRouteException;

import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides some helper methods which makes route handling easier.
 */
public class RouteManager {

    private Database database;

    /**
     * Number of generated routes.
     */
    private int nextRouteId;

    public RouteManager(Database database) {
        this.database = database;
        nextRouteId = getMaxRouteID(database.getRoutes()) + 1;
    }

    private int getMaxRouteID(Collection<Route> routes) {
        int id = 0;
        for (Route r : routes) {
            try {
                id = Math.max(id, Integer.parseInt(r.getId()));
            } catch (NumberFormatException e) {
                //ignore error
            }
        }
        return id;
    }

    /**
     * This will return a list of all known routes which are stored
     * in the database in a message friendly way.
     *
     * @return List of all known routes.
     */
    public final Map<String, VehicleRoute> getRoutesFromDatabaseForMessage() {
        // create a new map of the known routes
        Map<String, VehicleRoute> result = new HashMap<>();
        for (Route route : database.getRoutes()) {
            result.put(route.getId(), createRouteForRTI(route));
        }
        return result;
    }

    /**
     * Transforms a database {@link Route} into a {@link VehicleRoute} object
     * which can be send to other ambassadors.
     *
     * @param route a {@link Route} loaded from the scenario database.
     * @return simplified version of the route suitable to send to other ambassadors
     */
    public final VehicleRoute createRouteForRTI(Route route) {

        final List<String> edgeIds = route.getConnectionIds();
        double length = approximateLengthOfRoute(route);
        return new VehicleRoute(route.getId(), edgeIds, route.getNodeIds(), length);
    }

    private double approximateLengthOfRoute(Route route) {
        return route.getConnections().stream().mapToDouble(Connection::getLength).sum();
    }

    /**
     * This creates a new route based on the given nodes.
     *
     * @param candidateRoute list of all nodes to look through
     */
    public final Route createRouteByCandidateRoute(CandidateRoute candidateRoute) throws IllegalRouteException {
        // we always need to create a new Route
        Route route = new Route(Integer.toString(nextRouteId++));
        for (String connectionId : candidateRoute.getConnectionIds()) {
            Connection con = database.getConnection(connectionId);
            if (con == null) {
                throw new IllegalRouteException(
                        String.format("[createRouteByCandidateRoute] given connection ids represent an invalid route. The connection %s is unknown.", connectionId)
                );
            }

            Connection previousConnection = Iterables.getLast(route.getConnections(), null);
            if (previousConnection != null && !previousConnection.getOutgoingConnections().contains(con)) {
                throw new IllegalRouteException(
                        String.format("[createRouteByCandidateRoute] given connection ids represent an invalid route. "
                                + "Two subsequent connections %s and %s are not connected with each other.", previousConnection.getId(), con.getId())
                );
            }
            route.addConnection(con);
        }
        return route;
    }
}