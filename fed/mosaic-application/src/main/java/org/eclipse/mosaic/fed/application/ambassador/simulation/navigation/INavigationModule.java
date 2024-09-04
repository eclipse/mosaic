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

package org.eclipse.mosaic.fed.application.ambassador.simulation.navigation;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.road.IConnection;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingResponse;

import java.util.Collection;
import javax.annotation.Nullable;

/**
 * Interface to access the central navigation component from vehicle applications.
 */
public interface INavigationModule {

    /**
     * Calculates one or more routes from the position of the vehicle to the given target location.
     *
     * @param targetPosition    The target position of the required route.
     * @param routingParameters Properties defining the way routes are calculated (e.g. number of routes, weighting).
     * @return The response including a set of routes towards the target.
     */
    RoutingResponse calculateRoutes(RoutingPosition targetPosition, RoutingParameters routingParameters);

    /**
     * Calculates one or more routes from the position of the vehicle to the given target {@link GeoPoint} given as geographical location.
     *
     * @param targetGeoPoint    The target position of the required route given as {@link GeoPoint}.
     * @param routingParameters Properties defining the way routes are calculated (e.g. number of routes, weighting).
     * @return The response including a set of routes towards the target.
     */
    RoutingResponse calculateRoutes(GeoPoint targetGeoPoint, RoutingParameters routingParameters);

    /**
     * Returns all existing routes that can be taken to target position using {@link RoutingPosition}.
     *
     * @param targetPosition The target position to calculate routes for.
     * @return All valid routes to target position.
     */
    Collection<CandidateRoute> retrieveAllValidRoutesToTarget(RoutingPosition targetPosition);

    /**
     * Returns all existing routes that can be taken to target position using {@link GeoPoint}.
     *
     * @param targetGeoPoint The target position to calculate routes for.
     * @return All valid routes to target position.
     */
    Collection<CandidateRoute> retrieveAllValidRoutesToTarget(GeoPoint targetGeoPoint);

    /**
     * Switch to a specific route.
     *
     * @param route The route to switch to.
     * @return boolean whether route is switched.
     */
    boolean switchRoute(CandidateRoute route);

    /**
     * Returns the current route.
     *
     * @return the route the vehicle is currently driving on
     */
    VehicleRoute getCurrentRoute();

    /**
     * Returns the target position of the vehicle.
     *
     * @return The target position of the vehicle.
     */
    GeoPoint getTargetPosition();

    /**
     * Returns the current position of the vehicle.
     *
     * @return The current position of the vehicle.
     */
    GeoPoint getCurrentPosition();

    /**
     * Returns the road position the vehicle is currently driving on.
     *
     * @return The road position the vehicle is currently driving on.
     */
    IRoadPosition getRoadPosition();

    /**
     * Returns the distance in m to the specified node.
     *
     * @return The distance in m to the specified node.
     */
    double getDistanceToNodeOnRoute(String nodeId);

    /**
     * Returns next node that is a junction on the vehicle's current route.
     *
     * @return Next node that is a junction on the vehicle's current route.
     */
    INode getNextJunctionNode();

    /**
     * Returns next node that has a traffic light on the vehicle's current route.
     *
     * @return Next node that has a traffic light on the vehicle's current route.
     */
    INode getNextTrafficLightNode();

    /**
     * Returns the current vehicle information.
     *
     * @return the current vehicle information.
     */
    @Nullable
    VehicleData getVehicleData();

    /**
     * Returns data for the specified node id.
     *
     * @param node the id of the node
     * @return the {@link INode} containing data for the specified node id.
     */
    INode getNode(String node);

    /**
     * Returns data for the specified connection id.
     *
     * @param connection the id of the node
     * @return the {@link IConnection} containing data for the specified connection id.
     */
    IConnection getConnection(String connection);

    /**
     * Returns the node which is closest to specified {@link GeoPoint}.
     *
     * @param geoPoint the geographic position to search a node for
     * @return the closest {@link INode}
     */
    INode getClosestNode(GeoPoint geoPoint);

    /**
     * Returns the road position (i.e. an edge) which is closest to specified {@link GeoPoint}
     *
     * @param geoPoint the geographic position to search a road position for
     * @return the closest {@link IRoadPosition}
     */
    IRoadPosition getClosestRoadPosition(GeoPoint geoPoint);

    /**
     * Returns the road position, which is closest to the given {@link GeoPoint}.
     * If two adjacent edges overlap, the heading will be used as a similarity measure.
     *
     * @param geoPoint The geographical location to search a road position for.
     * @param heading  used as a measure of similarity if multiple edges match
     * @return The road position, which is closest to the given location.
     */
    IRoadPosition getClosestRoadPosition(GeoPoint geoPoint, double heading);
}
