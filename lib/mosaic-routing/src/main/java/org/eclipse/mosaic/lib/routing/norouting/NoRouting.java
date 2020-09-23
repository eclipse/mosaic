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

package org.eclipse.mosaic.lib.routing.norouting;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.IllegalRouteException;
import org.eclipse.mosaic.lib.routing.Routing;
import org.eclipse.mosaic.lib.routing.RoutingRequest;
import org.eclipse.mosaic.lib.routing.RoutingResponse;
import org.eclipse.mosaic.lib.routing.config.CRouting;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link Routing} if no scenario database
 * or any other road traffic map is present. In that case, online-routing
 * during the simulation is disabled.
 */
public class NoRouting implements Routing {

    @Override
    public void initialize(CRouting configuration, File configurationLocation) throws InternalFederateException {
        // nop
    }

    @Override
    public RoutingResponse findRoutes(RoutingRequest routingRequest) {
        return new RoutingResponse(null, Lists.newArrayList());
    }

    @Override
    public VehicleRoute createRouteForRTI(CandidateRoute candidateRoute) throws IllegalRouteException {
        return null;
    }

    @Override
    public Map<String, VehicleRoute> getRoutesFromDatabaseForMessage() {
        return new HashMap<>();
    }

    @Override
    public double getMaxSpeedOfConnection(String scenarioDatabaseConnectionId) {
        return 0;
    }

    @Override
    public IRoadPosition refineRoadPosition(IRoadPosition roadPosition) {
        return roadPosition;
    }

    @Override
    public CandidateRoute approximateCostsForCandidateRoute(CandidateRoute route, String lastNodeId) {
        return route;
    }

    @Override
    public IRoadPosition findClosestRoadPosition(GeoPoint point) {
        return null;
    }

    @Override
    public INode findClosestNode(GeoPoint point) {
        return null;
    }

    @Override
    public INode getNode(String nodeId) {
        return null;
    }

}
