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

package org.eclipse.mosaic.lib.routing;

import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.road.IConnection;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.config.CRouting;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.io.File;
import java.util.Map;

/**
 * Interface providing a routing API for applications.
 */
public interface Routing {

    /**
     * Initializes the connection to the belonging database.
     */
    void initialize(CRouting routingConfiguration, File configurationLocation) throws InternalFederateException;

    /**
     * Find a route from your actual position to the target position.
     *
     * @param routingRequest -
     */
    RoutingResponse findRoutes(RoutingRequest routingRequest);

    /**
     * Build a new route out of a list with node ID's. The route gets a new ID
     * and can be stored in the database.
     *
     * @return simplified version of the route
     */
    VehicleRoute createRouteForRTI(CandidateRoute candidateRoute) throws IllegalRouteException;

    /**
     * This will return a list of all known routes which are stored in the
     * database in a message friendly way.
     *
     * @return HashMap<routeId, Route>
     */
    Map<String, VehicleRoute> getRoutesFromDatabaseForMessage();

    /**
     * This will return the speed limit of a connection given by its ID.
     *
     * @return Maximum speed of a given connection.
     */
    double getMaxSpeedOfConnection(String scenarioDatabaseConnectionId);

    /**
     * Extends the given {@link IRoadPosition} object with additional information.
     *
     * @param roadPosition the {@link IRoadPosition} to extend with information
     * @return the extended {@link IRoadPosition} object based on the roadPosition argument
     */
    IRoadPosition refineRoadPosition(IRoadPosition roadPosition);

    /**
     * Approximates the costs of a {@link CandidateRoute}. Pass a {@link CandidateRoute}
     * and the ID of the last Node the vehicle passed on to the method and it will return a new
     * Candidate route with the approximated values in its "length" and "time" field.
     * The method approximates by calculating distance and time between the candidate route's first intersection
     * and last intersection. The calculation will be done on the fly and might thus have impact
     * on overall performance of a simulation.
     *
     * @param route      CandidateRoute to be cost-approximated
     * @param lastNodeId ID of last passed node
     * @return a new {@link CandidateRoute}
     */
    CandidateRoute approximateCostsForCandidateRoute(CandidateRoute route, String lastNodeId);

    /**
     * Searches for the closest road position to {@param point}.
     *
     * @param point The closest road position to the given location.
     * @return The closest road position as {@link IRoadPosition}.
     */
    IRoadPosition findClosestRoadPosition(GeoPoint point);

    /**
     * Searches for the closest node to {@param point}.
     *
     * @param point The closest node to the given location.
     * @return The closest node as {@link INode}.
     */
    INode findClosestNode(GeoPoint point);

    /**
     * Getter for the node by given Id.
     *
     * @param nodeId the id of the requested node.
     * @return the node object identified by the given nodeId.
     */
    INode getNode(String nodeId);

    /**
     * Getter for the connection by given Id.
     *
     * @param nodeId the id of the requested connection.
     * @return the connection object identified by the given nodeId.
     */
    IConnection getConnection(String nodeId);

    /**
     * Returns the cartesian bounds of the scenario.
     *
     * @return the bounds of the scenario
     */
    CartesianRectangle getScenarioBounds();
}