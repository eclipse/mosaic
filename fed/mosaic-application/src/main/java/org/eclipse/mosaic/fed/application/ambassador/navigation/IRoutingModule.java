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

package org.eclipse.mosaic.fed.application.ambassador.navigation;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingResponse;

/**
 * Interface to access the central navigation component from road side units.
 */
public interface IRoutingModule {

    /**
     * Calculates one or more routes from the position of the vehicle to the given target location.
     *
     * @param sourcePosition    The source position of the required route.
     * @param targetPosition    The target position of the required route.
     * @param routingParameters Properties defining the way routes are calculated (e.g. number of routes, weighting).
     * @return The response including a set of routes towards the target.
     */
    RoutingResponse calculateRoutes(RoutingPosition sourcePosition, RoutingPosition targetPosition, RoutingParameters routingParameters);

    /**
     * Returns the node object identified by the given nodeId.
     *
     * @param nodeId The id of the requested node.
     * @return The node object identified by the given nodeId.
     */
    INode getNode(String nodeId);

    /**
     * Returns the node object, which is closest to the given {@link GeoPoint}.
     *
     * @param geoPoint The geographical location to search a node for.
     * @return The node object, which is closest to the given location.
     */
    INode getClosestNode(GeoPoint geoPoint);

    /**
     * Returns the road position, which is closest to the given {@link GeoPoint}.
     *
     * @param geoPoint The geographical location to search a road position for.
     * @return The road position, which is closest to the given location.
     */
    IRoadPosition getClosestRoadPosition(GeoPoint geoPoint);
}
