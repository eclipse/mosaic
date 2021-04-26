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

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.AbstractSimulationUnit;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.IllegalRouteException;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingRequest;
import org.eclipse.mosaic.lib.routing.RoutingResponse;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Implementation of the interface to access the central navigation component.
 */
public class NavigationModule implements INavigationModule, IRoutingModule {

    private static final int POSITION_DIFFERENCE_THRESHOLD = 10;

    private final AbstractSimulationUnit belongingUnit;

    // values changing during runtime
    private VehicleData vehicleData;
    private VehicleRoute currentRoute;
    private GeoPoint currentPosition;

    public NavigationModule(AbstractSimulationUnit owner) {
        this.belongingUnit = owner;
    }

    @Override
    public RoutingResponse calculateRoutes(GeoPoint targetGeoPoint, RoutingParameters routingParameters) {
        return calculateRoutes(new RoutingPosition(targetGeoPoint), routingParameters);
    }

    @Override
    public RoutingResponse calculateRoutes(RoutingPosition target, RoutingParameters routingParameters) {
        belongingUnit.getOsLog().debug(
                "NavigationModule#calculateRoutes: Calculate routes to [{}] with params [{}]",
                target,
                routingParameters
        );

        final RoutingPosition source;
        if (vehicleData.getRoadPosition() != null && vehicleData.getRoadPosition().getConnection() != null) {
            source = new RoutingPosition(
                    vehicleData.getPosition(),
                    vehicleData.getHeading(),
                    vehicleData.getRoadPosition().getConnection().getId()
            );
        } else {
            source = new RoutingPosition(vehicleData.getPosition(), vehicleData.getHeading());
        }
        return calculateRoutes(source, target, routingParameters);
    }

    @Override
    public RoutingResponse calculateRoutes(
            RoutingPosition sourcePosition,
            RoutingPosition targetPosition,
            RoutingParameters routingParameters
    ) {
        final RoutingRequest routingRequest = new RoutingRequest(sourcePosition, targetPosition, routingParameters);
        return SimulationKernel.SimulationKernel.getCentralNavigationComponent().findRoutes(routingRequest);
    }

    @Override
    public boolean switchRoute(CandidateRoute newRoute) {
        belongingUnit.getOsLog().debug(
                "NavigationModule#switchRoute: Initiated route switch to [{}]",
                StringUtils.join(newRoute.getConnectionIds(), ",")
        );

        VehicleRoute route;
        try {
            route = SimulationKernel.SimulationKernel.getCentralNavigationComponent().switchRoute(
                    this.vehicleData, newRoute, currentRoute, belongingUnit.getSimulationTime()
            );

            boolean switched = route != null && (getCurrentRoute() == null || !route.getId().equals(getCurrentRoute().getId()));
            if (switched) {
                setCurrentRoute(route);
                belongingUnit.getOsLog().info(
                        "NavigationModule#switchRoute: Switched to route {} [{}]",
                        route.getId(),
                        StringUtils.join(route.getNodeIds(), ",")
                );
            } else if (route != null) {
                belongingUnit.getOsLog().info("NavigationModule#switchRoute: Stay on route {}", route.getId());
            } else {
                belongingUnit.getOsLog().info("NavigationModule#switchRoute: Error on route switch: null");
            }
            return switched;
        } catch (IllegalRouteException e) {
            belongingUnit.getOsLog().info(
                    "NavigationModule#switchRoute: Could not switch to candidate route[{}]",
                    StringUtils.join(newRoute.getConnectionIds(), ",")
            );
            return false;
        }
    }

    @Override
    public GeoPoint getTargetPosition() {
        return SimulationKernel.SimulationKernel.getCentralNavigationComponent().getTargetPositionOfRoute(vehicleData.getRouteId());
    }

    @Override
    public IRoadPosition getRoadPosition() {
        return vehicleData == null ? null : vehicleData.getRoadPosition();
    }

    @Override
    public VehicleData getVehicleData() {
        return vehicleData;
    }

    public void setVehicleData(VehicleData vehicleData) {
        this.vehicleData = vehicleData;
    }

    @Override
    public GeoPoint getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public VehicleRoute getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentPosition(GeoPoint position) {
        this.currentPosition = position;
    }

    public void setCurrentRoute(VehicleRoute currentRoute) {
        this.currentRoute = currentRoute;
    }

    /**
     * This method refines the road position while obtaining the missing information from the database.
     *
     * @param roadPosition {@link IRoadPosition} containing position information such as upcoming and previous node Ids.
     */
    public void refineRoadPosition(IRoadPosition roadPosition) {
        final IRoadPosition newRoadPosition =
                SimulationKernel.SimulationKernel.getCentralNavigationComponent().refineRoadPosition(roadPosition);
        if (newRoadPosition != null) {
            setVehicleData(
                    new VehicleData.Builder(getVehicleData().getTime(), getVehicleData().getName())
                            .copyFrom(getVehicleData())
                            .road(newRoadPosition).create()
            );
        }
    }

    @Override
    public Collection<CandidateRoute> retrieveAllValidRoutesToTarget(RoutingPosition targetPosition) {
        return retrieveAllValidExistingRoutesToTargetHelper(targetPosition);
    }

    @Override
    public Collection<CandidateRoute> retrieveAllValidRoutesToTarget(GeoPoint targetGeoPoint) {
        RoutingPosition targetPosition = new RoutingPosition(targetGeoPoint);
        return retrieveAllValidExistingRoutesToTargetHelper(targetPosition);
    }

    private Collection<CandidateRoute> retrieveAllValidExistingRoutesToTargetHelper(RoutingPosition targetPosition) {
        CentralNavigationComponent centNavComp = SimulationKernel.SimulationKernel.getCentralNavigationComponent();
        ArrayList<CandidateRoute> candidateRoutes = new ArrayList<>();
        for (Map.Entry<String, VehicleRoute> entry : centNavComp.getRouteMap().entrySet()) {
            VehicleRoute route = entry.getValue();
            if (targetQuery(targetPosition, route, centNavComp.getTargetPositionOfRoute(route.getId())) && onRouteQuery(route)) {
                // length and time are no valid values at this point
                candidateRoutes.add(new CandidateRoute(route.getConnectionIds(), 0, 0));
                belongingUnit.getOsLog().debug(
                        "NavigationModule#retrieveAllValidExistingRoutesToTarget found valid existing candidate route {} ", route.getId()
                );
            }
        }
        return candidateRoutes;
    }

    @VisibleForTesting
    boolean onRouteQuery(VehicleRoute route) {
        return route.getConnectionIds().contains(this.getVehicleData().getRoadPosition().getConnectionId());
    }

    @VisibleForTesting
    boolean targetQuery(RoutingPosition targetPosition, VehicleRoute route, GeoPoint routeTargetPoint) {
        boolean reachedLastEdge = targetPosition.getConnectionId() != null && route.getLastConnectionId().startsWith(targetPosition.getConnectionId());
        boolean reachedTargetGeoPoint = targetPosition.getPosition().distanceTo(routeTargetPoint) < POSITION_DIFFERENCE_THRESHOLD;
        return reachedLastEdge || reachedTargetGeoPoint;
    }

    @Override
    public double getDistanceToNodeOnRoute(String nodeId) throws IllegalArgumentException {
        try {
            return SimulationKernel.SimulationKernel.getCentralNavigationComponent()
                    .getDistanceToPointOnRoute(
                            vehicleData.getRouteId(),
                            nodeId,
                            vehicleData.getRoadPosition().getUpcomingNode().getId(),
                            currentPosition);
        } catch (IllegalArgumentException e) {
            belongingUnit.getOsLog().warn("NavigationModule#getDistanceToPointOnRoute received an invalid parameter", e);
            return Double.MAX_VALUE;
        }
    }

    @Override
    public INode getNextJunctionNode() {
        try {
            INode nextNode = SimulationKernel.SimulationKernel
                    .getCentralNavigationComponent().getNextNodeOnRoute(vehicleData.getRouteId(), getRoadPosition(), INode::isIntersection);
            if (nextNode == null) {
                belongingUnit.getOsLog().debug("There is no upcoming junction node for vehicle " + vehicleData.getName() + ".");
            }
            return nextNode;

        } catch (IllegalArgumentException e) {
            belongingUnit.getOsLog().warn("NavigationModule#getNextJunctionNode received an invalid parameter", e);
            return null;
        }
    }

    @Override
    public INode getNextTrafficLightNode() {
        try {
            INode nextNode = SimulationKernel.SimulationKernel
                    .getCentralNavigationComponent().getNextNodeOnRoute(vehicleData.getRouteId(), getRoadPosition(), INode::hasTrafficLight);
            if (nextNode == null) {
                belongingUnit.getOsLog().debug("There is no upcoming traffic light node for vehicle " + vehicleData.getName() + ".");
            }
            return nextNode;
        } catch (IllegalArgumentException e) {
            belongingUnit.getOsLog().warn("NavigationModule#getNextTrafficLightNode received an invalid parameter", e);
            return null;
        }
    }

    @Override
    public INode getNode(String nodeId) {
        return SimulationKernel.SimulationKernel.getCentralNavigationComponent().getRouting().getNode(nodeId);
    }

    @Override
    public INode getClosestNode(GeoPoint geoPoint) {
        return SimulationKernel.SimulationKernel.getCentralNavigationComponent().getRouting().findClosestNode(geoPoint);
    }

    @Override
    public IRoadPosition getClosestRoadPosition(GeoPoint geoPoint) {
        return SimulationKernel.SimulationKernel.getCentralNavigationComponent().getRouting().findClosestRoadPosition(geoPoint);
    }
}
