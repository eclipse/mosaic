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

package org.eclipse.mosaic.lib.objects.road;

import java.io.Serializable;

/**
 * Provides detailed information about a position within the road network.
 */
public interface IRoadPosition extends Serializable {

    /**
     * Returns the ID of the edge the vehicle currently driving on in the form {@code <id-connection>_<id-previous-node>}.
     */
    String getEdgeId();

    /**
     * Returns the index of the lane, where 0 is the rightmost lane.
     */
    int getLaneIndex();

    /**
     * Returns the lateral position of the vehicle on its current lane measured in m.
     */
    double getLateralLanePosition();

    /**
     * Returns the distance (in m) from the start node of the road segment to the current position of the vehicle.
     */
    double getOffset();

    /**
     * Returns the previously traversed node.
     */
    INode getPreviousNode();

    /**
     * Returns the node which will be traversed next.
     */
    INode getUpcomingNode();

    /**
     * Returns the connection the vehicle is currently driving on. A connection
     * is defined as a link between two junctions.
     *
     * Returns the connection the vehicle is currently on.
     */
    IConnection getConnection();

}
