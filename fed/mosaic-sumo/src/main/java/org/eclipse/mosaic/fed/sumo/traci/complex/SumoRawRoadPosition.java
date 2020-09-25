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

package org.eclipse.mosaic.fed.sumo.traci.complex;

import org.eclipse.mosaic.lib.objects.road.IConnection;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;

/**
 * Implementation of IRoadPosition which contains the raw road Id from SUMO and no further information.
 * {@link #getPreviousNode()}, {@link #getUpcomingNode()}, {@link #getConnection()} will always return {@code null}
 */
public class SumoRawRoadPosition implements IRoadPosition {

    private static final long serialVersionUID = 1L;

    private final String edgeId;
    private double offset;
    private int laneIndex = -1;
    private double lateralLanePosition;

    public SumoRawRoadPosition(String rawRoadId, double offset, double lateralLanePosition) {
        this.edgeId = rawRoadId;
        this.offset = offset;
        this.lateralLanePosition = lateralLanePosition;
    }

    public SumoRawRoadPosition(String rawRoadId, int laneIndex, double offset) {
        this.edgeId = rawRoadId;
        this.laneIndex = laneIndex;
        this.offset = offset;
    }

    @Override
    public String getEdgeId() {
        return edgeId;
    }

    @Override
    public int getLaneIndex() {
        return laneIndex;
    }

    @Override
    public double getOffset() {
        return offset;
    }

    @Override
    public double getLateralLanePosition() {
        return lateralLanePosition;
    }

    @Override
    public INode getPreviousNode() {
        return null;
    }

    @Override
    public INode getUpcomingNode() {
        return null;
    }

    @Override
    public IConnection getConnection() {
        return null;
    }
}
