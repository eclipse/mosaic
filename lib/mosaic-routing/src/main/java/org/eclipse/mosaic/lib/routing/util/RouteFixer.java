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

package org.eclipse.mosaic.lib.routing.util;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides methods to fix routes, e.g. by adding missing connections to unfinished routes.
 */
public class RouteFixer {

    private final static Logger log = LoggerFactory.getLogger(RouteFixer.class);

    private final Database database;
    private final int maxDepthConnectionSearch;

    /**
     * Creates a {@link RouteFixer} based on the given {@link Database} with a
     * default search depth of 4.
     *
     * @param database the database containing connections which are used to fix routes.
     *                 The database must be compliant to SUMO edge name format.
     */
    public RouteFixer(Database database) {
        this(database, 4);
    }

    /**
     * Creates a {@link RouteFixer} based on the given {@link Database}.
     *
     * @param database                 the database containing connections which are used to fix routes.
     *                                 The database must be compliant to SUMO edge name format.
     * @param maxDepthConnectionSearch the maximum depth of connections to search before resulting in an error during route fixing.
     */
    public RouteFixer(Database database, int maxDepthConnectionSearch) {
        this.maxDepthConnectionSearch = maxDepthConnectionSearch;
        if (database == null) {
            log.error("No database given. Routes can not be fixed");
            this.database = null;
        } else if (!isSumoCompliant(database)) {
            log.error("Connections in the given database are not compatible with MOSAIC and routes can not be fixed");
            this.database = null;
        } else {
            this.database = database;
        }
    }

    /**
     * Given a list of edge IDs, this method finds all intermediate edges if they are missing.
     * It only processes the route, if a scenario database exists and if it is compliant to the
     * SUMO edge format "<way-id>_<connection-from-node-id>_<connection-to-node-id>_<previous-node-id>".
     *
     * @param route the original route which may contain missing edges
     * @return the fixed route
     */
    public List<String> fixRoute(List<String> route) {
        if (database == null) {
            return route;
        }

        try {
            List<Connection> fixedRoute = new ArrayList<>();

            Connection currConnection = null;
            Connection prevConnection = null;
            for (String currConnectionId : route) {
                currConnection = getConnection(database, currConnectionId);
                if (prevConnection != null && currConnection.getFrom() != prevConnection.getTo()) {
                    fixedRoute.addAll(findIntermediateConnections(prevConnection, currConnection)); //always includes currConnection
                } else {
                    fixedRoute.add(currConnection);
                }
                prevConnection = currConnection;
            }
            return convertConnectionsToIds(fixedRoute);

        } catch (Exception e) {
            log.error("Could not fix route with edges [" + StringUtils.join(route, " ") + "]. The route shall be fixed manually.", e);
            return route;
        }
    }

    private boolean isSumoCompliant(Database database) {
        if (Database.IMPORT_ORIGIN_SUMO.equals(database.getImportOrigin())) {
            return true;
        }
        return !database.getConnections().isEmpty()
                && database.getConnections().iterator().next().getId().matches("^\\w+_\\w+_\\w+$");
    }

    private List<Connection> findIntermediateConnections(Connection from, Connection to) {
        if (from.getOutgoingConnections().contains(to)) {
            return Lists.newArrayList(to);
        }
        return Validate.notNull(
                findIntermediateConnectionsRecursively(from, to, maxDepthConnectionSearch),
                "Could not find intermediate connection from %s to %s.", from.getId(), to.getId());
    }

    private List<Connection> findIntermediateConnectionsRecursively(Connection from, Connection to, int depth) {
        if (depth == 0) {
            return null;
        }

        if (from == to) {
            return Lists.newArrayList();
        }

        List<Connection> shortest = null;
        List<Connection> tmp;
        for (Connection con : from.getOutgoingConnections()) {
            tmp = findIntermediateConnectionsRecursively(con, to, depth - 1);
            if (tmp != null) {
                tmp.add(0, con);
                if (lengthOfConnections(tmp) < lengthOfConnections(shortest)) {
                    shortest = tmp;
                }
            }
        }
        return shortest;
    }

    private double lengthOfConnections(List<Connection> connections) {
        return connections == null ? Double.MAX_VALUE : connections.stream().map(Connection::getLength).reduce(0d, Double::sum);
    }

    public static Connection getConnection(Database db, String connectionId) {
        Connection con = db.getConnection(connectionId);
        if (con != null) {
            return con;
        }
        throw new IllegalStateException("Could not find Edge");
    }

    private static String getPreviousNodeId(String edgeId) {
        return StringUtils.substringAfterLast(edgeId, "_");
    }

    private List<String> convertConnectionsToIds(List<Connection> fixedRoute) {
        return fixedRoute.stream().map(Connection::getId).collect(Collectors.toList());
    }
}
