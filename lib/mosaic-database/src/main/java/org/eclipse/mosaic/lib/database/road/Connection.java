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

package org.eclipse.mosaic.lib.database.road;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This represents an abstraction layer on top of a {@link Way}. A connection is a directed
 * part of a way that connects two intersection nodes.
 */
public class Connection {

    private final String id;

    private final Way way;
    private int lanes;
    private double length;

    private final List<Node> nodes = new ArrayList<>();
    private final Map<String, Connection> outgoing = new HashMap<>();
    private final Map<String, Connection> incoming = new HashMap<>();

    public Connection(@Nonnull String id, @Nonnull Way way) {
        this(id, way, false);
    }

    /**
     * Default constructor.
     *
     * @param id         Unique identifier of the connection.
     * @param way        Way represents a number of edges in the network graph.
     * @param isBackward if this connection represents the forward or backward direction of the way
     */
    public Connection(@Nonnull String id, @Nonnull Way way, boolean isBackward) {
        // parameters
        this.id = Objects.requireNonNull(id);
        this.way = Objects.requireNonNull(way);

        // dependant values
        lanes = (isBackward) ? way.getNumberOfLanesBackward() : way.getNumberOfLanesForward();
    }

    /**
     * Returns the ID of this connection.
     *
     * @return Unique identifier of the connection.
     */
    @Nonnull
    public String getId() {
        return id;
    }

    /**
     * This returns the {@link Way} this connection belongs to.
     *
     * @return The way of the connection.
     */
    @Nonnull
    public Way getWay() {
        return way;
    }

    /**
     * This returns the {@link Node} the connection starts from.
     *
     * @return Null if no from node exist, otherwise the node.
     */
    public Node getFrom() {
        if (!nodes.isEmpty()) {
            return nodes.get(0);
        }

        return null;
    }

    /**
     * This returns the {@link Node} that the connections ends at.
     *
     * @return Null if no to node exist, otherwise the node.
     */
    public Node getTo() {
        if (!nodes.isEmpty()) {
            return nodes.get(nodes.size() - 1);
        }
        return null;
    }

    /**
     * Returns the number of lanes this Connection presents.
     *
     * @return Number of the lanes.
     */
    public int getLanes() {
        return lanes;
    }

    /**
     * This connections length in meters.
     *
     * @return The length of the connection.
     */
    public double getLength() {
        return length;
    }

    /**
     * Gets the maximum speed allowed on this connection in km/h.
     *
     * @return The speed in [km/h].
     */
    public double getMaxSpeedInKmh() {
        return way.getMaxSpeedInKmh();
    }

    /**
     * Gets the maximum speed allowed on this connection in m/s.
     *
     * @return The speed in [m/s].
     */
    public double getMaxSpeedInMs() {
        return way.getMaxSpeedInMs();
    }

    /**
     * These are the {@link Node}s that are part of this connection.
     * Beware that the order is important.
     *
     * @return List of Nodes representing a connection.
     */
    @Nonnull
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * Returns the connections that are accessible through this one.
     *
     * @return List of accessible / upcoming connections.
     */
    @Nonnull
    public Collection<Connection> getOutgoingConnections() {
        return Collections.unmodifiableCollection(outgoing.values());
    }

    /**
     * Returns the list of connections that lead into this connection..
     *
     * @return List of incoming connections
     */
    @Nonnull
    public Collection<Connection> getIncomingConnections() {
        return Collections.unmodifiableCollection(incoming.values());
    }

    /**
     * Sets the connections length in meters.
     *
     * @param length Length to set.
     */
    public Connection setLength(double length) {
        this.length = length;
        return this;
    }

    /**
     * Sets the number of lanes for this connection.
     * This overrides eventual values copied from the {@link Way}.
     *
     * @param lanes Lanes to set.
     */
    public Connection setLanes(int lanes) {
        this.lanes = lanes;
        return this;
    }

    /**
     * Adds a {@link Node} belonging to this connection. Be aware that the order of nodes is vital
     * to the order of edges belonging to this connection.
     *
     * @param node Node to add.
     */
    public void addNode(@Nonnull Node node) {
        Objects.requireNonNull(node);
        nodes.add(node);
    }

    /**
     * Adds all given {@link Node}s belonging to this connection. Be aware that the order of nodes
     * is vital to the order of edges belonging to this connection.
     *
     * @param node Nodes to add.
     */
    public void addNodes(@Nonnull List<Node> node) {
        Objects.requireNonNull(node);
        nodes.addAll(node);
    }

    /**
     * Adds a connection that is accessible through this one.
     *
     * @param connection Outgoing connection to add.
     */
    public void addOutgoingConnection(@Nonnull Connection connection) {
        Objects.requireNonNull(connection);

        if (connection.getFrom() == null) {
            return;
        }

        if (connection.getFrom().equals(getTo())) {
            outgoing.put(connection.getId(), connection);
        }
    }

    /**
     * Adds a connection through that this connection can be accessed.
     *
     * @param connection Incoming connection to add.
     */
    public void addIncomingConnection(@Nonnull Connection connection) {
        Objects.requireNonNull(connection);

        if (connection.getTo() == null) {
            return;
        }

        if (connection.getTo().equals(getFrom())) {
            incoming.put(connection.getId(), connection);
        }
    }

    /**
     * This applies a turn restriction to the connection. Restrictions can be of type 'only'
     * or 'not' which will cause the outgoing connections to be either trimmed down to the target
     * or the target will be removed from the outgoing connections.
     *
     * @param type   either 'only' or 'not'
     * @param target the connection
     */
    public void applyTurnRestriction(@Nonnull Restriction.Type type, @Nonnull Connection target) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(target);
        switch (type) {
            case Only:
                // this means there should be ONLY the target in the connection
                outgoing.clear();
                outgoing.put(target.getId(), target);

                for (Connection outgoingConnection : getTo().getOutgoingConnections()) {
                    if (!outgoingConnection.getId().equals(target.getId())) {
                        outgoingConnection.incoming.remove(getId());
                    }
                }
                break;
            case Not:
                outgoing.remove(target.getId());
                target.incoming.remove(getId());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
