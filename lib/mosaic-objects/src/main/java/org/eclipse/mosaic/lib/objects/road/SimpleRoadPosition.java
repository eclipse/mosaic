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
    private final int laneIndex;
    private final double lateralLanePosition;

    /**
     * Construct a {@link SimpleRoadPosition}.
     *
     * @param wayId               the ID of the way.
     * @param connectionStartId   the start ID of the road.
     * @param connectionEndId     the end ID of the road.
     * @param previousNodeId      the previous node ID.
     * @param laneIndex           the index of the current lane.
     * @param laneOffset          the lane offset.
     * @param lateralLanePosition the lateral position on the lane.
     */
    public SimpleRoadPosition(String wayId, String connectionStartId, String connectionEndId, String previousNodeId,
                              int laneIndex, double laneOffset, double lateralLanePosition) {
        this.connection = new SimpleConnection(wayId, connectionStartId, connectionEndId);
        this.previousNode = new SimpleNode(previousNodeId);
        this.upcomingNode = null;
        this.laneOffset = laneOffset;
        this.laneIndex = laneIndex;
        this.lateralLanePosition = lateralLanePosition;
    }

    /**
     * Construct a {@link SimpleRoadPosition}.
     *
     * @param wayId             the ID of the way.
     * @param connectionStartId the start ID of the road.
     * @param connectionEndId   the end ID of the road.
     * @param previousNodeId    the previous node ID.
     * @param laneIndex         the index of the current lane.
     * @param laneOffset        the lane offset.
     */
    public SimpleRoadPosition(String wayId, String connectionStartId, String connectionEndId,
                              String previousNodeId, int laneIndex, double laneOffset) {
        this.connection = new SimpleConnection(wayId, connectionStartId, connectionEndId);
        this.previousNode = new SimpleNode(previousNodeId);
        this.upcomingNode = null;
        this.laneOffset = laneOffset;
        this.laneIndex = laneIndex;
        this.lateralLanePosition = 0;
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
        this.lateralLanePosition = 0;
    }

    /**
     * Gets the ID of the edge the vehicle currently driving on as string.
     *
     * @return ID of the edge as string.
     */
    @Override
    public String getEdgeId() {
        return String.format("%s_%s", getConnection() != null ? getConnection().getId() : "?_?_?",
                getPreviousNode() != null ? getPreviousNode().getId() : "?");
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
        return lateralLanePosition;
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
                .append(this.lateralLanePosition)
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

        SimpleRoadPosition srp = (SimpleRoadPosition) obj;
        return new EqualsBuilder()
                .append(this.connection, srp.connection)
                .append(this.laneIndex, srp.laneIndex)
                .append(this.laneOffset, srp.laneOffset)
                .append(this.lateralLanePosition, srp.lateralLanePosition)
                .append(this.previousNode, srp.previousNode)
                .append(this.upcomingNode, srp.upcomingNode)
                .isEquals();
    }

    private static class SimpleConnection implements IConnection {

        private static final long serialVersionUID = 1L;

        private final SimpleWay way;
        private final SimpleNode startNode;
        private final SimpleNode endNode;

        private SimpleConnection(String wayId, String connectionStartId, String connectionEndId) {
            this.way = new SimpleWay(wayId);
            this.startNode = new SimpleNode(connectionStartId);
            this.endNode = new SimpleNode(connectionEndId);
        }

        @Override
        public String getId() {
            return String.format("%s_%s_%s", //
                    getWay() != null ? getWay().getId() : "?", //
                    getStartNode() != null ? getStartNode().getId() : "?", //
                    getEndNode() != null ? getEndNode().getId() : "?");
        }

        @Override
        public double getLength() {
            return Double.NaN;
        }

        @Override
        public INode getStartNode() {
            return startNode;
        }

        @Override
        public INode getEndNode() {
            return endNode;
        }

        @Override
        public IWay getWay() {
            return way;
        }

        @Override
        public int getLanes() {
            return 0;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(19, 37)
                    .append(this.way)
                    .append(this.startNode)
                    .append(this.endNode)
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
                    .append(this.way, sc.way)
                    .append(this.startNode, sc.startNode)
                    .append(this.endNode, sc.endNode)
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

    private static class SimpleWay implements IWay {

        private static final long serialVersionUID = 1L;

        private final String wayId;

        private SimpleWay(String wayId) {
            this.wayId = wayId;
        }

        @Override
        public String getId() {
            return wayId;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public double getMaxSpeedInMs() {
            return Double.NaN;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(5, 71)
                    .append(this.wayId)
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

            SimpleWay sw = (SimpleWay) obj;
            return new EqualsBuilder()
                    .append(this.wayId, sw.wayId)
                    .isEquals();
        }

    }

}
