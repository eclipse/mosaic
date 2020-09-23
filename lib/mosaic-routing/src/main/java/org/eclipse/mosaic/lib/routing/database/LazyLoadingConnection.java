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

package org.eclipse.mosaic.lib.routing.database;

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

    private final IConnection currentConnection;
    private final IRoadPosition currentRoadPosition;

    private LazyLoadingNode conStartNode;
    private LazyLoadingNode conEndNode;
    private LazyLoadingWay way;

    private String id;

    public LazyLoadingConnection(Connection connection) {
        this.id = connection.getId();
        this.scenarioDatabaseConnection = connection;

        this.database = null;
        this.currentConnection = null;
        this.currentRoadPosition = null;
    }

    LazyLoadingConnection(IRoadPosition currentRoadPosition, Database database) {
        this.currentConnection = currentRoadPosition.getConnection();
        this.currentRoadPosition = currentRoadPosition;
        this.database = database;
    }

    @Override
    public String getId() {
        if (id == null) {
            id = new StringBuilder(getWay() != null ? getWay().getId() : "?").append("_")
                    .append(getStartNode() != null ? getStartNode().getId() : "?").append("_")
                    .append(getEndNode() != null ? getEndNode().getId() : "?").toString();
        }
        return id;
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
            if (currentConnection == null || currentConnection.getStartNode() == null) {
                final Connection con = getConnectionFromDatabase();
                if (con != null) {
                    conStartNode = new LazyLoadingNode(con.getFrom());
                }
            } else {
                conStartNode = new LazyLoadingNode(currentConnection.getStartNode(), database);
            }
        }
        return conStartNode;
    }

    @Override
    public INode getEndNode() {
        if (conEndNode == null) {
            if (currentConnection == null || currentConnection.getEndNode() == null) {
                final Connection con = getConnectionFromDatabase();
                if (con != null) {
                    conEndNode = new LazyLoadingNode(con.getTo());
                }
            } else {
                conEndNode = new LazyLoadingNode(currentConnection.getEndNode(), database);
            }
        }
        return conEndNode;
    }

    @Override
    public IWay getWay() {
        if (way == null) {
            if (currentConnection == null || currentConnection.getWay() == null) {
                final Connection con = getConnectionFromDatabase();
                if (con != null) {
                    way = new LazyLoadingWay(con.getWay());
                }
            } else {
                way = new LazyLoadingWay(currentConnection.getWay(), database);
            }
        }
        return way;
    }

    Connection getConnectionFromDatabase() {
        if (scenarioDatabaseConnection != null || database == null) {
            return scenarioDatabaseConnection;
        }

        if (currentConnection != null && currentConnection.getStartNode() != null && currentConnection.getEndNode() != null && currentConnection.getWay() != null) {
            final String currentConnectionId = currentConnection.getWay().getId() + "_"
                    + currentConnection.getStartNode().getId() + "_"
                    + currentConnection.getEndNode().getId();
            scenarioDatabaseConnection = database.getConnection(currentConnectionId);

        } else if (currentConnection != null && currentConnection.getStartNode() != null && currentConnection.getEndNode() != null) {
            scenarioDatabaseConnection = getConnectionBetweenNodes(
                    currentConnection.getStartNode(), currentConnection.getEndNode(), currentConnection.getWay()
            );
        } else if (currentRoadPosition != null && currentRoadPosition.getPreviousNode() != null && currentRoadPosition.getUpcomingNode() != null) {
            scenarioDatabaseConnection = getConnectionBetweenNodes(
                    currentRoadPosition.getPreviousNode(), currentRoadPosition.getUpcomingNode(),
                    currentConnection != null ? currentConnection.getWay() : null
            );
        } else {
            // from here there's no way to get the connection :(
        }
        return scenarioDatabaseConnection;
    }

    private Connection getConnectionBetweenNodes(INode from, INode to, IWay way) {
        final Node fromNode = database.getNode(from.getId());
        final Collection<Connection> fromNodeConnections = fromNode.getOutgoingConnections().isEmpty() ? fromNode.getPartOfConnections() : fromNode.getOutgoingConnections();

        final Node toNode = database.getNode(to.getId());
        final Collection<Connection> toNodeConnections = toNode.getIncomingConnections().isEmpty() ? toNode.getPartOfConnections() : toNode.getIncomingConnections();

        for (Connection conFromStart : fromNodeConnections) {
            for (Connection conToEnd : toNodeConnections) {
                if (conFromStart.getId().equals(conToEnd.getId()) && // 
                        (way == null || way.getId().equals(conFromStart.getWay().getId()))) {
                    return conFromStart;
                }
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 37)
                .append(this.currentConnection)
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

        LazyLoadingConnection sdc = (LazyLoadingConnection) obj;
        return new EqualsBuilder()
                .append(this.currentConnection, sdc.currentConnection)
                .append(this.conStartNode, sdc.conStartNode)
                .append(this.conEndNode, sdc.conEndNode)
                .append(this.way, sdc.way)
                .isEquals();
    }

}
