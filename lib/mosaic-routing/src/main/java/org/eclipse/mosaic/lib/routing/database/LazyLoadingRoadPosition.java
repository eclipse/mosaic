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
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;

/**
 * <p>
 * Provides information about a road position. All missing information is gathered lazy by requesting the
 * scenario-database as soon as the respective getter method is called. Those information is cached for later calls of the same method by
 * storing the respective items from the scenario-database.</p>
 * <p>
 * Note, a minimum knowledge about the road position is always needed:
 * <ul>
 *   <li><b>Previous node (id)</b> and <b>upcoming node (id)</b>, or</li>
 *   <li><b>Connection (id)</b> and <b>previous node (id)</b></li>
 * </ul>
 * </p><p>
 * For example, if the connection and the previous node is given, the upcoming node is calculated as soon as the
 * method {@link #getUpcomingNode()} is called. The information gathered is then being cached in case the method
 * is called again.</p>
 */
public class LazyLoadingRoadPosition implements IRoadPosition {

    private static final long serialVersionUID = 1L;

    /* This reference must be kept transient, since it should never be serialized (e.g. by GSON) */
    @Nullable
    private final transient Database database;

    private final IRoadPosition currentRoadPosition;

    private final int laneIndex;
    private final double lateralLanePosition;
    private final double offset;
    private LazyLoadingNode previousNode;
    private LazyLoadingNode upcomingNode;
    private LazyLoadingConnection connection;

    /**
     * Creates an {@link IRoadPosition} with all required information provided.
     *
     * @param connection the connection this road position can be found on
     * @param previousNode the previous node which is part of the given connection and lies behind this road position
     * @param upcomingNode the upcoming node which is part of the given connection and lies in front of this road position
     * @param roadOffset the distance in meters from the previous node to the exact road position
     */
    public LazyLoadingRoadPosition(final LazyLoadingConnection connection, final LazyLoadingNode previousNode, final LazyLoadingNode upcomingNode, double roadOffset) {
        this.connection = connection;
        this.previousNode = previousNode;
        this.upcomingNode = upcomingNode;
        this.offset = roadOffset;
        this.laneIndex = 0;

        this.database = null;
        this.currentRoadPosition = null;
        this.lateralLanePosition = 0;
    }

    /**
     * Creates an {@link IRoadPosition} object which provides further values taken from
     * the given {@link Database}, based on the given {@link IRoadPosition} which only provides little information.
     *
     * @param currentRoadPosition the {@link IRoadPosition} which only contains little information, such as upcoming and previous node ids
     * @param database            a reference to the scenario {@link Database} which is used to obtain missing information
     */
    public LazyLoadingRoadPosition(final IRoadPosition currentRoadPosition, final Database database) {
        this.database = database;
        this.currentRoadPosition = currentRoadPosition;

        laneIndex = currentRoadPosition.getLaneIndex();
        lateralLanePosition = currentRoadPosition.getLateralLanePosition();
        offset = currentRoadPosition.getOffset();
    }

    @Override
    public String getConnectionId() {
        return getConnection().getId();
    }

    @Override
    public int getLaneIndex() {
        return laneIndex;
    }

    @Override
    public double getLateralLanePosition() {
        return lateralLanePosition;
    }

    @Override
    public double getOffset() {
        return offset;
    }

    @Override
    public INode getPreviousNode() {
        if (previousNode != null) {
            return previousNode;
        }
        if (currentRoadPosition.getPreviousNode() != null) {
            previousNode = new LazyLoadingNode(currentRoadPosition.getPreviousNode(), database);
        } else if (currentRoadPosition.getUpcomingNode() != null) {
            final Connection roadConnection = getConnection().getConnectionFromDatabase();
            if (roadConnection != null) {
                Node prevNode = null;
                for (Node node : roadConnection.getNodes()) {
                    if (node.getId().equals(currentRoadPosition.getUpcomingNode().getId())) {
                        previousNode = new LazyLoadingNode(prevNode);
                        break;
                    }
                    prevNode = node;
                }
            }

        } else {
            // no possibility to calculate upcoming node.
            return currentRoadPosition.getPreviousNode();
        }
        return previousNode;
    }

    @Override
    public INode getUpcomingNode() {
        if (upcomingNode != null) {
            return upcomingNode;
        }

        if (currentRoadPosition.getUpcomingNode() != null) {
            upcomingNode = new LazyLoadingNode(currentRoadPosition.getUpcomingNode(), database);
        } else if (currentRoadPosition.getPreviousNode() != null) {
            final Connection roadConnection = getConnection().getConnectionFromDatabase();
            if (roadConnection != null) {
                Node prevNode = null;
                for (Node node : roadConnection.getNodes()) {
                    if (prevNode != null && prevNode.getId().equals(currentRoadPosition.getPreviousNode().getId())) {
                        upcomingNode = new LazyLoadingNode(node);
                        break;
                    }
                    prevNode = node;
                }
            }
        } else {
            // no possibility to calculate upcoming node.
            return currentRoadPosition.getUpcomingNode();
        }
        return upcomingNode;
    }

    @Override
    public LazyLoadingConnection getConnection() {
        if (connection == null) {
            connection = new LazyLoadingConnection(currentRoadPosition, database);
        }
        return connection;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 43)
                .append(this.laneIndex)
                .append(this.lateralLanePosition)
                .append(this.offset)
                .append(this.previousNode)
                .append(this.upcomingNode)
                .append(this.connection)
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

        LazyLoadingRoadPosition sn = (LazyLoadingRoadPosition) obj;
        return new EqualsBuilder()
                .append(this.laneIndex, sn.laneIndex)
                .append(this.lateralLanePosition, sn.lateralLanePosition)
                .append(this.offset, sn.offset)
                .append(this.previousNode, sn.previousNode)
                .append(this.upcomingNode, sn.upcomingNode)
                .append(this.connection, sn.connection)
                .isEquals();
    }


}
