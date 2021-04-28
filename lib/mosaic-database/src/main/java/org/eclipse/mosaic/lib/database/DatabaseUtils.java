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

package org.eclipse.mosaic.lib.database;

import org.eclipse.mosaic.lib.database.persistence.DatabaseLoader;
import org.eclipse.mosaic.lib.database.persistence.OutdatedDatabaseException;
import org.eclipse.mosaic.lib.database.persistence.SQLiteLoader;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Restriction;
import org.eclipse.mosaic.lib.database.road.Way;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * This class bundles static utilities to be called on {@link Database} and objects
 * contained in {@link Database}s.
 */
public class DatabaseUtils {
    static final Logger log = LoggerFactory.getLogger(Database.class);

    /**
     * Updates the database from the given {@link File} object to the latest scheme.
     *
     * @param file database file
     * @throws RuntimeException if an SQLException or IOException were caught
     */
    public static void updateDb(@Nonnull final File file) {
        DatabaseLoader loader = new SQLiteLoader(true);
        try {
            loader.updateDatabase(file.getCanonicalPath());
        } catch (SQLException | IOException | OutdatedDatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This detects all graphs that exist in the database and returns them as lists of nodes.
     *
     * @param nodes the collection of {@link Node}s that graphs should be detected in
     * @return Lists of graph nodes.
     */
    public static ArrayList<Set<Node>> detectGraphs(Collection<Node> nodes) {
        Set<Node> visitedNodes = new HashSet<>();
        ArrayList<Set<Node>> foundGraphs = new ArrayList<>();

        // go through all nodes in the database
        for (Node currentValue : nodes) {
            // we are only interested in nodes we haven't visited yet
            if (!visitedNodes.contains(currentValue)) {
                Set<Node> newGraph = DatabaseUtils.searchGraph(currentValue);

                // If the graph has only one node the node probably hasn't got any outgoing connections!
                // This means we will most likely visit the node at a later iteration or the node
                // has no connections to any graph whatsoever!
                if (newGraph.size() > 1) {
                    foundGraphs.add(newGraph);
                    visitedNodes.addAll(newGraph);
                }
            }
        }

        return foundGraphs;
    }

    /**
     * This searches all nodes of a graph by breadth-first search.
     *
     * @param startNode Start node for searching.
     */
    public static Set<Node> searchGraph(Node startNode) {
        // prepare temp and result variables
        Queue<Node> searchQueue = new LinkedList<>();
        Set<Node> graphNodes = new HashSet<>();

        // init algorithm
        searchQueue.add(startNode);

        // run algorithm
        while (!searchQueue.isEmpty()) {
            // get current node
            Node currentNode = searchQueue.poll();

            // look at all incoming connections
            for (Connection inc : currentNode.getIncomingConnections()) {
                // add to node to queue if not already visited
                if (!graphNodes.contains(inc.getFrom()) && !searchQueue.contains(inc.getFrom())) {
                    searchQueue.add(inc.getFrom());
                }

                // process all other nodes of the connection
                graphNodes.addAll(inc.getNodes());
            }

            // look at all outgoing connections
            for (Connection out : currentNode.getOutgoingConnections()) {
                // add to node to queue if not already known
                if (!graphNodes.contains(out.getTo()) && !searchQueue.contains(out.getTo())) {
                    searchQueue.add(out.getTo());
                }

                // process all other nodes of the connection
                graphNodes.addAll(out.getNodes());
            }
        }
        // return all nodes found to be part of that graph
        return graphNodes;
    }

    /**
     * This checks if there already is a similar connection to the given one in the database
     * without it being the same {@link Connection}. This will always return false if one of the
     * nodes to check against or the found duplicate connection was not referenced in the database!
     * The check goes down to the edge level and checks all nodes.
     *
     * @param connection Connection to check whether there is a duplicate in the database.
     * @return connection which was duplicated by the given one or null
     */
    public static Connection getDuplicateConnection(Database.Builder databaseBuilder, Connection connection) {
        // make sure the nodes to check again are in the database
        if (!databaseBuilder.nodeExists(connection.getFrom().getId()) || !databaseBuilder.nodeExists(connection.getTo().getId())) {
            log.warn("either the 'from' or the 'to' node of the connection to check against "
                    + "duplicates was not found in the database, this COULD be an unwanted inconsistency");
            return null;
        }

        // check all connections that start at the from Node
        List<Node> connectionNodes;
        List<Node> currentConnectionNodes;
        for (Connection currentConnection : connection.getFrom().getOutgoingConnections()) {
            if (!currentConnection.equals(connection) && currentConnection.getTo().equals(connection.getTo())) {
                // make sure the found duplicate connection is in the database
                if (!databaseBuilder.connectionExists(connection.getId())) {
                    log.warn("a duplicate connection was found referenced in the nodes but not "
                            + "in the database, this COULD be an unwanted inconsistency.");
                    return null;
                }

                // last but not least, make sure the connection is completely equal
                // (including all in between nodes)
                connectionNodes = connection.getNodes();
                currentConnectionNodes = currentConnection.getNodes();
                if (currentConnectionNodes.size() == connectionNodes.size()) {

                    // if any of the nodes is not equal this is no duplicate
                    for (int i = 0; i < currentConnectionNodes.size(); i++) {
                        if (!connectionNodes.get(i).equals(currentConnectionNodes.get(i))) {
                            return null;
                        }
                    }

                    // it seems the connection is the same down to the edge lvl, ignoring
                    return currentConnection;
                }
            }
        }

        return null;
    }

    /**
     * Determines, if a node is a intersection node, that is, it has more than 2 outgoing or incoming streets.
     * @param node the node to check
     * @return {@code true} if the node is an intersection
     */
    public static boolean isIntersection(Node node) {
        final Set<String> neighborNodes = new HashSet<>();
        if (node.getWays().size() <= 1) {
            return false;
        } else {
            // collects all neighboring nodes from related ways. if more than 2 found, it's a junction!
            for (Way way : node.getWays()) {

                String[] neighbors = getNeighborNodes(node, way);

                for (String neighborNode : neighbors) {
                    if (neighborNode != null) {
                        neighborNodes.add(neighborNode);
                    }
                }
            }
            return neighborNodes.size() > 2;
        }
    }

    /**
     * Returns all the neighbor nodes of a way.
     *
     * @param node Neighbors of this node.
     * @param way  Way including the nodes.
     * @return All neighbors of the given node.
     */
    private static String[] getNeighborNodes(Node node, Way way) {
        String[] result = {null, null};
        boolean found = false;
        for (Node nodeOnWay : way.getNodes()) {
            if (found) {
                result[1] = nodeOnWay.getId();
                break;
            }
            if (nodeOnWay == node) {
                found = true;
            }
            if (!found) {
                result[0] = nodeOnWay.getId();
            }
        }
        return result;
    }

    /**
     * This processes all connections already read and tries to extract possible turn restrictions
     * on junctions. For this the outgoing connections of the {@link Connection} are checked against
     * the outgoing connections of the junction {@link Node}.
     */
    public static void generateRestrictions(Database.Builder databaseBuilder) {
        // running vars
        int restrictionIdCounter = -1;
        Collection<Connection> connectionOutgoing;
        Collection<Connection> nodeOutgoing;

        // we need to check every connection for possible restrictions
        for (Connection connection : databaseBuilder.getConnections()) {
            // convenience access
            connectionOutgoing = connection.getOutgoingConnections();
            nodeOutgoing = connection.getTo().getOutgoingConnections();

            // restrictions apply when the sizes don't match
            if (connectionOutgoing.size() < nodeOutgoing.size()) {

                // special case only one possible way => generate restriction of type only
                if (connectionOutgoing.size() == 1) {
                    databaseBuilder.addRestriction(
                            ++restrictionIdCounter + "",
                            Restriction.Type.Only,
                            connection.getWay().getId(),
                            connection.getTo().getId(),
                            connectionOutgoing.iterator().next().getWay().getId());
                } else {
                    // obviously multiple restrictions, use type not
                    for (Connection outgoing : nodeOutgoing) {
                        // restriction applies if outgoing connection of node does not exist
                        // in outgoing connections of current connection
                        if (!connectionOutgoing.contains(outgoing)) {
                            databaseBuilder.addRestriction(
                                    ++restrictionIdCounter + "",
                                    Restriction.Type.Not,
                                    connection.getWay().getId(),
                                    connection.getTo().getId(),
                                    outgoing.getWay().getId()
                            );
                        }
                    }
                }
            }
        }
    }

}