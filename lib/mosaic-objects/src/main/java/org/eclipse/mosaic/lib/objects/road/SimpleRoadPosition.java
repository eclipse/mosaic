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

package org.eclipse.mosaic.lib.objects.road;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Default implementation io {@link IRoadPosition} which
 * holds the raw data from the traffic simulation. Some of the
 * getters return {@code null} values, as the information is missing
 * at this state. The application simulator, which knows much more about
 * the road topology, uses this incomplete data to identify the road items
 * and can provide more data to the applications.
 */
public class SimpleRoadPosition implements IRoadPosition {

    private static final long serialVersionUID = 1L;

    private final SimpleConnection connection;
    private final SimpleNode previousNode;
    private final SimpleNode upcomingNode;
    private final double laneOffset;
    private final double lateralOffset;
    private final int laneIndex;

    /**
     * Construct a {@link SimpleRoadPosition}.
     *
     * @param connectionId the ID of the connection.
     * @param laneIndex    the index of the current lane.
     * @param laneOffset   the lane offset.
     */
    public SimpleRoadPosition(String connectionId, int laneIndex, double laneOffset, double lateralOffset) {
        this.connection = new SimpleConnection(connectionId);
        this.previousNode = null;
        this.upcomingNode = null;
        this.laneOffset = laneOffset;
        this.laneIndex = laneIndex;
        this.lateralOffset = lateralOffset;
    }

    /**
     * Construct a {@link SimpleRoadPosition}.
     *
     * @param previousNodeId ID of the previous node.
     * @param upcomingNodeId ID of the next node.
     * @param laneIndex      the index of the current lane.
     * @param laneOffset     the lane offset.
     */
    public SimpleRoadPosition(String previousNodeId, String upcomingNodeId, int laneIndex, double laneOffset) {
        this.connection = null;
        this.previousNode = new SimpleNode(previousNodeId);
        this.upcomingNode = new SimpleNode(upcomingNodeId);
        this.laneOffset = laneOffset;
        this.laneIndex = laneIndex;
        this.lateralOffset = 0d;
    }

    /**
     * Gets the ID of the connection the vehicle currently driving on as string.
     *
     * @return ID of the connection as string.
     */
    @Override
    public String getConnectionId() {
        return getConnection() != null ? getConnection().getId() : "?";
    }

    /**
     * Gets the index of the lane the vehicle currently driving on.
     *
     * @return the index of the lane.
     */
    @Override
    public int getLaneIndex() {
        return laneIndex;
    }

    /**
     * Gets the offset.
     *
     * @return the offset as double.
     */
    @Override
    public double getOffset() {
        return laneOffset;
    }

    /**
     * Gets the lateral lane position.
     *
     * @return the lateral lane position.
     */
    @Override
    public double getLateralLanePosition() {
        return lateralOffset;
    }

    /**
     * Gets the previous node which the vehicle already passed as {@link INode}.
     *
     * @return the previous node
     */
    @Override
    public INode getPreviousNode() {
        return previousNode;
    }

    /**
     * Gets the upcoming node of the vehicles road.
     *
     * @return the upcoming node.
     */
    @Override
    public INode getUpcomingNode() {
        return upcomingNode;
    }

    /**
     * Gets the current segment.
     *
     * @return the current segment.
     */
    @Override
    public IConnection getConnection() {
        return connection;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 41)
                .append(this.connection)
                .append(this.laneIndex)
                .append(this.laneOffset)
                .append(this.lateralOffset)
                .append(this.previousNode)
                .append(this.upcomingNode)
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

        SimpleRoadPosition other = (SimpleRoadPosition) obj;
        return new EqualsBuilder()
                .append(this.connection, other.connection)
                .append(this.laneIndex, other.laneIndex)
                .append(this.laneOffset, other.laneOffset)
                .append(this.lateralOffset, other.laneIndex)
                .append(this.previousNode, other.previousNode)
                .append(this.upcomingNode, other.upcomingNode)
                .isEquals();
    }

    private static class SimpleConnection implements IConnection {

        private static final long serialVersionUID = 1L;

        private final String connectionId;

        private SimpleConnection(String connectionId) {
            this.connectionId = connectionId;
        }

        @Override
        public String getId() {
            return connectionId;
        }

        @Override
        public double getLength() {
            return Double.NaN;
        }

        @Override
        public INode getStartNode() {
            return null;
        }

        @Override
        public INode getEndNode() {
            return null;
        }

        @Override
        public IWay getWay() {
            return null;
        }

        @Override
        public int getLanes() {
            return 0;
        }

        @Override
        public Collection<IConnection> getIncomingConnections() {
            return new ArrayList<>();
        }

        @Override
        public Collection<IConnection> getOutgoingConnections() {
            return new ArrayList<>();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(19, 37)
                    .append(this.connectionId)
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

            SimpleConnection sc = (SimpleConnection) obj;
            return new EqualsBuilder()
                    .append(this.connectionId, sc.connectionId)
                    .isEquals();
        }

    }

    private static class SimpleNode implements INode {

        private static final long serialVersionUID = 1L;

        private final String nodeId;

        private SimpleNode(String nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public String getId() {
            return nodeId;
        }

        @Override
        public GeoPoint getPosition() {
            return null;
        }

        @Override
        public boolean hasTrafficLight() {
            return false;
        }

        @Override
        public boolean isIntersection() {
            return false;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(5, 71)
                    .append(this.nodeId)
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

            SimpleNode sn = (SimpleNode) obj;
            return new EqualsBuilder()
                    .append(this.nodeId, sn.nodeId)
                    .isEquals();
        }

    }

}
