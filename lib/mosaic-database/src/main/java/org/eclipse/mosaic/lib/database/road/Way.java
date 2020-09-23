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

package org.eclipse.mosaic.lib.database.road;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This represents a number of edges in the network graph. This is the representation of (street)
 * edges in OSM and only here for convenience if we need a back reference to the original data.
 * Please use connections for routing algorithms. This is NOT intended for representing anything
 * else then streets!
 */
public class Way {

    private final String id;
    private final String name;
    private final String type;

    private double maxSpeedInMs = 0;
    private int lanesForward = 0;
    private int lanesBackward = 0;

    private final List<Node> nodes = new ArrayList<>();

    private final List<Connection> connections = new ArrayList<>();

    /**
     * Tells whether the way is a oneway.
     */
    private boolean isOneway = false;

    /**
     * Default constructor.
     *
     * @param id   The (OSM) ID this way represents.
     * @param name The street name
     * @param type The type of street (residential/primary etc.)
     */
    public Way(@Nonnull String id, String name, @Nonnull String type) {
        this.id = Objects.requireNonNull(id);
        this.name = (name != null) ? name : "";
        this.type = Objects.requireNonNull(type);
    }

    /**
     * Returns the ID of this node.
     *
     * @return Unique identifier of the way.
     */
    @Nonnull
    public String getId() {
        return id;
    }

    /**
     * Returns the street name of this way.
     *
     * @return Name of the way.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Gets the type of street (residential/tertiary/secondary/primary etc.).
     *
     * @return Type of the way.
     */
    @Nonnull
    public String getType() {
        return type;
    }


    /**
     * Gets the maximum speed allowed on this way in m/s.
     *
     * @return Maximum speed allowed on this way in [m/s].
     */
    public double getMaxSpeedInMs() {
        return maxSpeedInMs;
    }

    /**
     * Gets the maximum speed allowed on this way in km/h.
     *
     * @return Maximum speed allowed on this way in [km/h].
     */
    public double getMaxSpeedInKmh() {
        return maxSpeedInMs * 3.6;
    }

    /**
     * This returns the number of lanes into the primary direction of this way.
     * This means the direction in which the nodes are defined.
     *
     * @return Number of the lanes into the primary direction.
     */
    public int getNumberOfLanesForward() {
        return lanesForward;
    }

    /**
     * This returns the number of lanes in the opposite direction of this way.
     * This means the reverse direction in which the nodes are defined.
     * This will be 0 for oneway ways.
     *
     * @return Number of the lanes in the opposite direction.
     */
    public int getNumberOfLanesBackward() {
        return isOneway ? 0 : lanesBackward;
    }

    /**
     * Returns if this is way can only be passed in one direction.
     *
     * @return True if it is one way direction.
     */
    public boolean isOneway() {
        return isOneway;
    }

    /**
     * Sets if the way only has one direction or two.
     *
     * @param oneway Boolean value indicating one way direction.
     */
    public void setIsOneway(boolean oneway) {
        this.isOneway = oneway;
    }

    /**
     * This returns the nodes that belong to this way.
     * Be aware that the order of nodes reflects the order of edges!
     *
     * @return All the nodes of this way.
     */
    @Nonnull
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * This returns the connections that are part of this way.
     *
     * @return Connections of this way.
     */
    @Nonnull
    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    /**
     * Sets the maximum speed allowed on this way in km/h.
     *
     * @param maxSpeedInKmh Maximum speed to set in [km/h].
     */
    public void setMaxSpeedInKmh(double maxSpeedInKmh) {
        this.maxSpeedInMs = maxSpeedInKmh / 3.6d;
    }

    /**
     * Sets the maximum speed allowed on this way in m/s.
     *
     * @param maxSpeedInMs Maximum speed to set in [m/s].
     */
    public void setMaxSpeedInMs(double maxSpeedInMs) {
        this.maxSpeedInMs = maxSpeedInMs;
    }

    /**
     * Sets the forward and backward lanes for this way.
     *
     * @param forward  Forward lane of the way.
     * @param backward Backward lane of the way.
     */
    public void setLanes(int forward, int backward) {
        this.lanesForward = forward;
        this.lanesBackward = backward;
    }

    /**
     * Adds a {@link Node} that belongs to this way. Mind the order of nodes when adding,
     * as they determines the order of edges.
     *
     * @param node a node that is part of the way
     */
    public void addNode(@Nonnull Node node) {
        // nodes CAN be part of the way more than once (circles)!
        nodes.add(Objects.requireNonNull(node));
    }

    /**
     * Adds a {@link Connection} that is a part of this way.
     *
     * @param connection the connection to be added as part of the way
     */
    public void addConnection(@Nonnull Connection connection) {
        Objects.requireNonNull(connection);
        if (!connections.contains(connection) && connection.getWay().equals(this)) {
            connections.add(connection);
        }
    }
}
