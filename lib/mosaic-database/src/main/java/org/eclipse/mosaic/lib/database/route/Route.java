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

package org.eclipse.mosaic.lib.database.route;

import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * This is a complete route that can be driven by a vehicle.
 */
public class Route {

    /**
     * The internal id for this route.
     */
    private final String id;

    /**
     * The list of edges the vehicles using this route have to drive.
     */
    private final List<Edge> edgeList = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param id Id of the route.
     */
    public Route(@Nonnull String id) {
        this.id = Objects.requireNonNull(id);
    }


    /**
     * The id of this route.
     *
     * @return Id of the route.
     */
    @Nonnull
    public String getId() {
        return id;
    }

    /**
     * This is the list of {@link Edge}s that form the route.
     *
     * @return list of edges
     */
    @Nonnull
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edgeList);
    }

    /**
     * This extracts a list of edge IDs.
     *
     * @return Extracted list of edge Ids.
     */
    @Nonnull
    public List<String> getEdgeIds() {
        return edgeList.stream().map(Edge::getId).collect(Collectors.toList());
    }

    /**
     * Adds an {@link Edge} to the route.
     *
     * @param edge Edge to add.
     */
    public void addEdge(@Nonnull Edge edge) {
        if (edgeList.size() > 0 && edgeList.get(edgeList.size() - 1).getId().equals(edge.getId())) {
            edgeList.remove(edgeList.size() - 1);
        }
        edgeList.add(Objects.requireNonNull(edge));
    }

    /**
     * This extracts a list of {@link Node}s that vehicles using this {@link Route} are passing.
     *
     * @return Extracted nodes.
     */
    @Nonnull
    public List<Node> getNodes() {
        if (edgeList.isEmpty()) {
            return new ArrayList<>();
        } else {
            List<Node> nodes = edgeList.stream().map(Edge::getFromNode).collect(Collectors.toList());
            nodes.add(edgeList.get(edgeList.size() - 1).getToNode());
            return nodes;
        }
    }



    /**
     * This extracts a list of all node IDs this {@link Route} passes.
     *
     * @return Extracted list of all node IDs.
     */
    @Nonnull
    public List<String> getNodeIds() {
        return Collections.unmodifiableList(getNodes().stream().map(Node::getId).collect(Collectors.toList()));
    }

    /**
     * This extracts a list of {@link Connection}s that vehicles using this {@link Route} are passing. Multiple adjacent edges belonging to
     * the same connection will result in only one occurrence of the connection.
     *
     * @return Extracted nodes.
     */
    @Nonnull
    public List<Connection> getConnections() {
        List<Connection> connections = new ArrayList<>();
        Connection lastConnection = null;
        for (Edge edge : edgeList) {
            if (lastConnection != edge.getConnection()) {
                connections.add(edge.getConnection());
            }
            lastConnection = edge.getConnection();
        }
        return connections;
    }

    /**
     * This extracts a list of connection IDs. Multiple adjacent edges belonging to
     * the same connection will result in only one occurrence of the connection.
     *
     * @return Extracted list of connection Ids.
     */
    @Nonnull
    public List<String> getConnectionIds() {
        return Collections.unmodifiableList(getConnections().stream().map(Connection::getId).collect(Collectors.toList()));
    }

}
