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
    private final List<Connection> connections = new ArrayList<>();

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
     * Adds an {@link Connection} to the route.
     *
     * @param connection Connection to add.
     */
    public void addConnection(@Nonnull Connection connection) {
        if (connections.size() > 0 && connections.get(connections.size() - 1).getId().equals(connection.getId())) {
            connections.remove(connections.size() - 1);
        }
        connections.add(Objects.requireNonNull(connection));
    }

    /**
     * This extracts a list of {@link Node}s that vehicles using this {@link Route} are passing.
     *
     * @return Extracted nodes.
     */
    @Nonnull
    public List<Node> getNodes() {
        if (connections.isEmpty()) {
            return new ArrayList<>();
        } else {
            //FIXME find out if we really need all nodes, or only start/end nodes of connections
            List<Node> nodes = new ArrayList<>();
            for (Connection connection : connections) {
                if (!nodes.isEmpty()) {
                    // avoid double nodes as end and start node from consecutive connections are the same
                    nodes.remove(nodes.size() - 1);
                }
                nodes.addAll(connection.getNodes());
            }
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
        return Collections.unmodifiableList(connections);
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
