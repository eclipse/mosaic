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

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.objects.road.IConnection;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.road.IWay;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;

/**
 * Provides information about the connection of a {@link IRoadPosition}. Any missing information is gathered lazy by requesting the
 * scenario-database as soon as the respective getter method is called. Those information is cached for later calls of the same method by
 * storing the connection from the scenario-database..<br><br>
 * <p>
 * In order to retrieve more information about a connection such as start or end node, one of the following information is required at least:
 * <ul>
 *   <li><b>Previous node (id)</b> and <b>upcoming node (id)</b>, or</li>
 *   <li><b>Connection (id)</b> and <b>previous node (id)</b></li>
 * </ul>
 * </p>
 */
public class LazyLoadingConnection implements IConnection {

    private static final long serialVersionUID = 1L;

    /* This reference must be kept transient, since it should never be serialized (e.g. by GSON) */
    @Nullable
    private final transient Database database;

    /* This reference must be kept transient, since it should never be serialized (e.g. by GSON) */
    @SuppressWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    @Nullable
    private transient Connection scenarioDatabaseConnection;

    private final String id;

    private final IRoadPosition currentRoadPosition;

    private LazyLoadingNode conStartNode;
    private LazyLoadingNode conEndNode;
    private LazyLoadingWay way;
    private Collection<IConnection> incomingConnections;
    private Collection<IConnection> outgoingConnections;

    public LazyLoadingConnection(Connection connection) {
        this.id = connection.getId();
        this.scenarioDatabaseConnection = connection;

        this.database = null;
        this.currentRoadPosition = null;
    }

    LazyLoadingConnection(IRoadPosition currentRoadPosition, Database database) {
        this.id = currentRoadPosition.getConnectionId();
        this.currentRoadPosition = currentRoadPosition;
        this.database = database;
    }

    @Override
    public String getId() {
        final Connection con = getConnectionFromDatabase();
        return con != null ? con.getId() : defaultIfNull(id, "?");
    }

    @Override
    public double getLength() {
        final Connection con = getConnectionFromDatabase();
        return con != null ? con.getLength() : 0;
    }

    @Override
    public int getLanes() {
        final Connection con = getConnectionFromDatabase();
        return con != null ? con.getLanes() : 0;
    }

    @Override
    public INode getStartNode() {
        if (conStartNode == null) {
            final Connection con = getConnectionFromDatabase();
            if (con != null) {
                conStartNode = new LazyLoadingNode(con.getFrom());
            }
        }
        return conStartNode;
    }

    @Override
    public INode getEndNode() {
        if (conEndNode == null) {
            final Connection con = getConnectionFromDatabase();
            if (con != null) {
                conEndNode = new LazyLoadingNode(con.getTo());
            }
        }
        return conEndNode;
    }

    @Override
    public IWay getWay() {
        if (way == null) {
            final Connection con = getConnectionFromDatabase();
            if (con != null) {
                way = new LazyLoadingWay(con.getWay());
            }
        }
        return way;
    }

    @Override
    public Collection<IConnection> getIncomingConnections() {
        if (incomingConnections == null) {
            final Connection con = getConnectionFromDatabase();
            if (con != null) {
                incomingConnections = new ArrayList<>();
                for (Connection incomingConnection : con.getIncomingConnections()) {
                    incomingConnections.add(new LazyLoadingConnection(incomingConnection));
                }
            }
        }
        return incomingConnections;
    }

    @Override
    public Collection<IConnection> getOutgoingConnections() {
        if (outgoingConnections == null) {
            final Connection con = getConnectionFromDatabase();
            if (con != null) {
                outgoingConnections = new ArrayList<>();
                for (Connection outgoingConnection : con.getOutgoingConnections()) {
                    outgoingConnections.add(new LazyLoadingConnection(outgoingConnection));
                }
            }
        }
        return outgoingConnections;
    }

    Connection getConnectionFromDatabase() {
        if (scenarioDatabaseConnection != null || database == null) {
            return scenarioDatabaseConnection;
        }

        scenarioDatabaseConnection = database.getConnection(id);
        if (scenarioDatabaseConnection != null) {
            return scenarioDatabaseConnection;
        }

        if (currentRoadPosition != null && currentRoadPosition.getPreviousNode() != null && currentRoadPosition.getUpcomingNode() != null) {
            scenarioDatabaseConnection = getConnectionBetweenNodes(
                    currentRoadPosition.getPreviousNode(),
                    currentRoadPosition.getUpcomingNode()
            );
        }

        return scenarioDatabaseConnection;
    }

    private Connection getConnectionBetweenNodes(INode from, INode to) {
        final Node fromNode = database.getNode(from.getId());
        final Collection<Connection> fromNodeConnections = fromNode.getOutgoingConnections().isEmpty() ? fromNode.getPartOfConnections() : fromNode.getOutgoingConnections();

        final Node toNode = database.getNode(to.getId());
        final Collection<Connection> toNodeConnections = toNode.getIncomingConnections().isEmpty() ? toNode.getPartOfConnections() : toNode.getIncomingConnections();

        for (Connection conFromStart : fromNodeConnections) {
            for (Connection conToEnd : toNodeConnections) {
                if (conFromStart.getId().equals(conToEnd.getId())) {
                    return conFromStart;
                }
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 37)
                .append(this.conStartNode)
                .append(this.conEndNode)
                .append(this.way)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        LazyLoadingConnection other = (LazyLoadingConnection) obj;
        return new EqualsBuilder()
                .append(this.conStartNode, other.conStartNode)
                .append(this.conEndNode, other.conEndNode)
                .append(this.way, other.way)
                .isEquals();
    }

}
