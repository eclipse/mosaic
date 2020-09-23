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

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This represents a node in the network graph. It basically is the same as an OSM node with a
 * few additions for easy access to dependant information.
 */
public class Node {

    /**
     * The ID of this node.
     */
    private final String id;

    /**
     * The position of this node.
     */
    private GeoPoint position;
    
    /**
     * Flag indicating if this node has been generated from an edge's shape.
     */
    private boolean generated = false;
    
    /**
     * Flag indicating if this node is an intersection.
     */
    private boolean intersection = false;

    private final List<Way> ways = new ArrayList<>();
    private final List<Connection> outgoingConnections = new ArrayList<>();
    private final List<Connection> incomingConnections = new ArrayList<>();
    private final List<Connection> partOfConnections = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param id       The ID of this node.
     * @param position The position of this node.
     */
    public Node(@Nonnull String id, @Nonnull GeoPoint position) {
        this.id = Objects.requireNonNull(id);
        this.position = Objects.requireNonNull(position);
    }

    public double getElevation() {
        return position.getAltitude();
    }
    
    public void setElevation(double elevation) {
        double longitude = this.position.getLongitude();
        double latitude = this.position.getLatitude();
        this.position = GeoPoint.lonLat(longitude, latitude, elevation);
    }
        
    /**
     * Returns the ID of this node.
     *
     * @return The ID of this node as String.
     */
    @Nonnull
    public String getId() {
        return id;
    }

    /**
     * Returns the geo-position of this node.
     * position.x is longitude
     * position.y is latitude
     *
     * @return The geo-position.
     */
    @Nonnull
    public GeoPoint getPosition() {
        return position;
    }

    public void setPosition(GeoPoint position) {
        this.position = position;
    }

    /**
     * Returns if this node has been generated from an edge's shape.
     *
     * @return True if this node has been generated, false otherwise.
     */
    public boolean isGenerated() {
        return generated;
    }
    
    /**
     * Sets whether this node has been generated from an edge's shape.
     *
     * @param isGenerated true if this node has been generated, false otherwise.
     */
    public void setGenerated(boolean isGenerated) {
        generated = isGenerated;
    }

    /**
     * Returns if this node is an intersection node or not.
     * See {@link Database.Builder#calculateIntersections()}} for an automatism to
     * detect intersection in nodes.
     *
     * @return True if this node is an intersection, false otherwise.
     */
    public boolean isIntersection() {
        return intersection;
    }

    /**
     * Returns all {@link Connection}s that end at this node.
     *
     * @return An unmodifiable {@link java.util.List} view from {@link #incomingConnections}.
     */
    @Nonnull
    public List<Connection> getIncomingConnections() {
        return Collections.unmodifiableList(incomingConnections);
    }

    /**
     * Returns all {@link Connection}s that start at this node.
     *
     * @return An unmodifiable {@link java.util.List} view from {@link #outgoingConnections}.
     */
    @Nonnull
    public List<Connection> getOutgoingConnections() {
        return Collections.unmodifiableList(outgoingConnections);
    }

    /**
     * Returns all {@link Connection}s that this node is a part of (but does not start or end at).
     *
     * @return An unmodifiable {@link java.util.List} view from {@link #partOfConnections}.
     */
    @Nonnull
    public List<Connection> getPartOfConnections() {
        return Collections.unmodifiableList(partOfConnections);
    }

    /**
     * Returns all {@link Way}s that this node is part of.
     *
     * @return An unmodifiable {@link java.util.List} view from {@link #ways}.
     */
    @Nonnull
    public List<Way> getWays() {
        return Collections.unmodifiableList(ways);
    }

    /**
     * Sets if this node is an intersection node.
     *
     * @param isIntersection set to true if the node is an intersection
     */
    public void setIntersection(boolean isIntersection) {
        this.intersection = isIntersection;
    }

    /**
     * Adds a way to the list of depending ways. This method will check if the node really is
     * part of the way and only add to the internal list if it is!
     *
     * @param way The {@link Way} to add.
     */
    public void addWay(@Nonnull Way way) {
        Objects.requireNonNull(way);
        if (!ways.contains(way)) {
            ways.add(way);
        }
    }

    /**
     * Removes the way relation from the node.
     *  
     * @param way The {@link Way} to remove.
     */
    public void removeWay(Way way) {
        Objects.requireNonNull(way);
        ways.remove(way);
    }

    /**
     * Add a connection internal list. This method automatically checks if this is an incoming
     * or outgoing connection or if the node is simply part of the connection at all.
     *
     * @param connection The {@link Connection} to add.
     */
    public void addConnection(@Nonnull Connection connection) {
        Objects.requireNonNull(connection);

        // determine type of connection and add (once)
        if (this.equals(connection.getFrom())) {
            if (!outgoingConnections.contains(connection)) {
                outgoingConnections.add(connection);
            }
        } else if (this.equals(connection.getTo())) {
            if (!incomingConnections.contains(connection)) {
                incomingConnections.add(connection);
            }
        } else {
            if (!partOfConnections.contains(connection)) {
                partOfConnections.add(connection);
            }
        }

    }

    /**
     * This returns whether this node is a part of any connection, being start, part of or end.
     *
     * @return True if this node is part of any connection, false otherwise.
     */
    public boolean isConnectionNode() {
        return !outgoingConnections.isEmpty() || !partOfConnections.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false; 
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        
        Node rhs = (Node) obj;
        return new EqualsBuilder()
                        .append(this.intersection, rhs.intersection)
                        .append(this.id, rhs.id)
                        .append(this.incomingConnections, rhs.incomingConnections)
                        .append(this.outgoingConnections, rhs.outgoingConnections)
                        .append(this.partOfConnections, rhs.partOfConnections)
                        .append(this.position, rhs.position)
                        .append(this.ways, rhs.ways)
                        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 31)
                        .append(id)
                        .append(position)
                        .append(intersection)
                        .append(ways)
                        .append(outgoingConnections)
                        .append(incomingConnections)
                        .append(partOfConnections)
                        .toHashCode();
    }

    @Override
    public String toString() {
        return "Node{"
                + "id='" + id + '\''
                + ", position=" + position
                + ", intersection=" + intersection
                + ", ways=" + ways
                + ", outgoingConnections=" + outgoingConnections
                + ", incomingConnections=" + incomingConnections
                + ", partOfConnections=" + partOfConnections
                + '}';
    }
}
