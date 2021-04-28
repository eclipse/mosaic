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
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.route.Route;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.IllegalRouteException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        // now need to loop over all nodes and find the correct edges to add to route
        Node lastNode = null;
        Node currentNode = null;
        Optional<Connection> currentCon;
        List<Connection> connectionCandidates = new LinkedList<>();
        for (String nodeId : candidateRoute.getNodeIdList()) {
            currentNode = database.getNode(nodeId);

            if (lastNode != null) {
                // somehow need to determine correct outgoing connection
                connectionCandidates.clear();
                // the node can either be part of a connection or start of a connection (not both)
                // determine which is the case
                List<Connection> checkConnections = !lastNode.getOutgoingConnections().isEmpty()
                        ? lastNode.getOutgoingConnections()
                        : lastNode.getPartOfConnections();
                // now go through the possibilities and determine the correct one
                for (Connection connection : checkConnections) {
                    // the node list is complete, we need to check parts of connection
                    int index = connection.getNodes().indexOf(lastNode);
                    if (connection.getNodes().get(index + 1).equals(currentNode)) {
                        connectionCandidates.add(connection);
                    }
                }

                // check if we can create an edge or the route is invalid
                currentCon = chooseFastest(connectionCandidates);
                if (currentCon.isPresent()) {
                    route.addConnection(currentCon.get());
                } else {
                    throw new IllegalRouteException(
                            String.format("[addRouteByNodeList] given nodes represent an invalid route. "
                                    + "There seems to be no valid connection between %s and %s", lastNode.getId(), currentNode.getId())
                    );
                }

            }

            lastNode = currentNode;
        }

        return route;
    }

    private Optional<Connection> chooseFastest(List<Connection> connectionCandidates) {
        double min = Double.MAX_VALUE;
        Connection result = null;
        for (Connection con : connectionCandidates) {
            if (result == null || min > con.getLength() / con.getMaxSpeedInMs()) {
                result = con;
                min = con.getLength() / con.getMaxSpeedInMs();
            }
        }
        return Optional.ofNullable(result);
    }
}