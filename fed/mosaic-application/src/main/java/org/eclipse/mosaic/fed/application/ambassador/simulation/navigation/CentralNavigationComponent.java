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

import org.eclipse.mosaic.fed.application.ambassador.ApplicationAmbassador;
import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.interactions.traffic.VehicleRoutesInitialization;
import org.eclipse.mosaic.interactions.vehicle.VehicleRouteChange;
import org.eclipse.mosaic.interactions.vehicle.VehicleRouteRegistration;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoUtils;
import org.eclipse.mosaic.lib.objects.mapping.OriginDestinationPair;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.IllegalRouteException;
import org.eclipse.mosaic.lib.routing.Routing;
import org.eclipse.mosaic.lib.routing.RoutingCostFunction;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingRequest;
import org.eclipse.mosaic.lib.routing.RoutingResponse;
import org.eclipse.mosaic.lib.routing.config.CPublicTransportRouting;
import org.eclipse.mosaic.lib.routing.database.DatabaseRouting;
import org.eclipse.mosaic.lib.routing.norouting.NoRouting;
import org.eclipse.mosaic.lib.routing.pt.PtRouting;
import org.eclipse.mosaic.lib.routing.pt.PtRoutingRequest;
import org.eclipse.mosaic.lib.routing.pt.PtRoutingResponse;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.RtiAmbassador;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

/**
 * The {@link CentralNavigationComponent} unites functionality concerned with
 * routing. This includes switching the routes of vehicles or calculating
 * distances / costs of routes.
 */
public class CentralNavigationComponent {

    /**
     * logger for the CentralNavigationComponent.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * the given ambassadorParameters of the application.
     */
    private final AmbassadorParameter applicationAmbassadorParameter;

    /**
     * The RTI ambassador handle to send messages.
     */
    private RtiAmbassador rtiAmbassador;

    /**
     * The IRoutingApi.
     */
    private Routing routing;


    /**
     * Public Transport routing
     */
    private PtRouting ptRouting;

    /**
     * The configuration for routingAPI.
     */
    private CApplicationAmbassador.CRoutingByType configuration;

    /**
     * The configuration for public transport routing.
     */
    private CPublicTransportRouting ptConfiguration;

    /**
     * Constructor for the CentralNavigationComponent.
     * Sets the logger and the configuration for navigation.
     *
     * @param ambassadorParameter     The ApplicationAmbassadorParameter.
     *                                These include the parameters for the
     *                                CentralNavigationComponent.
     * @param navigationConfiguration The routing configuration.
     */
    public CentralNavigationComponent(
            final AmbassadorParameter ambassadorParameter,
            @Nonnull CApplicationAmbassador.CRoutingByType navigationConfiguration,
            @Nonnull CPublicTransportRouting publicTransportConfiguration
    ) {
        this.applicationAmbassadorParameter = ambassadorParameter;
        this.configuration = navigationConfiguration;
        this.ptConfiguration = publicTransportConfiguration;
    }

    /**
     * This method initializes the {@link CentralNavigationComponent}. It is called
     * by the {@link ApplicationAmbassador}.
     * The {@link #routing} will be created and initialized and other simulators will be informed
     * using a {@link VehicleRoutesInitialization} interaction.
     *
     * @param rtiAmbassador the ambassador of the run time infrastructure
     * @throws InternalFederateException if the {@link #routing} couldn't be initialized or the
     *                                   {@link VehicleRoutesInitialization} interaction couldn't be send to the rti
     */
    public void initialize(RtiAmbassador rtiAmbassador) throws InternalFederateException {
        this.rtiAmbassador = rtiAmbassador;

        try {
            this.log.info("Initializing CNC-Navigation");

            routing = createFromType(configuration.type);
            routing.initialize(configuration, applicationAmbassadorParameter.configuration.getParentFile());

            ptRouting = new PtRouting();
            ptRouting.initialize(ptConfiguration, applicationAmbassadorParameter.configuration.getParentFile());

            this.log.info("CNC - Navigation-System initialized");

            try {
                final Map<String, VehicleRoute> routeMap = routing.getRoutesFromDatabaseForMessage();
                for (var routeEntry : routeMap.entrySet()) {
                    SimulationKernel.SimulationKernel.registerRoute(routeEntry.getKey(), routeEntry.getValue());
                }

                // generate VehicleRoutesInitialization to inform other simulators
                VehicleRoutesInitialization interaction = new VehicleRoutesInitialization(0, routeMap);

                log.info("Sending vehicle routes initialization");
                rtiAmbassador.triggerInteraction(interaction);
            } catch (IllegalValueException e) {
                throw new InternalFederateException(e);
            }
        } catch (InternalFederateException e) {
            InternalFederateException ex = new InternalFederateException(e);
            log.error("Exception", ex);
            throw ex;
        }
    }

    public void close() {
        if (ptRouting != null) {
            ptRouting.close();
        }
    }

    /**
     * Returns an unmodifiable view of all routes known to the {@link SimulationKernel}.
     *
     * @return unmodifiable view of all routes known to the {@link SimulationKernel}
     */
    public Map<String, VehicleRoute> getAllRoutes() {
        return SimulationKernel.SimulationKernel.getRoutes();
    }

    Routing createFromType(String type) throws InternalFederateException {
        if (type == null || "database".equalsIgnoreCase(type) || "graphhopper".equalsIgnoreCase(type)) {
            return new DatabaseRouting();
        } else if ("no-routing".equalsIgnoreCase(type)) {
            return new NoRouting();
        } else {
            try {
                Class<?> routingImplClass = Class.forName(type);
                return (Routing) routingImplClass.getConstructor().newInstance();
            } catch (Exception e) {
                String msg = "Could not create Routing instance from type '" + type + "'.";
                InternalFederateException ex = new InternalFederateException(msg, e);
                log.error(msg, ex);
                throw ex;
            }
        }
    }

    /**
     * Find a route from your actual position to the target position.
     *
     * @param routingRequest A {@link RoutingRequest} that contains
     *                       the origin, the end and additional
     *                       routing parameters to calculate the route
     *                       for.
     */
    RoutingResponse findRoutes(RoutingRequest routingRequest) {
        return routing.findRoutes(routingRequest);
    }

    /**
     * Switch to a specific route.
     *
     * @param rawRoute    A {@link CandidateRoute}, which the vehicle might change to.
     * @param vehicleData The {@link VehicleData} of the vehicle,
     *                    which is going to switch routes.
     * @param time        Time at which the route change should happen.
     */
    public VehicleRoute switchRoute(VehicleData vehicleData, CandidateRoute rawRoute, VehicleRoute currentRoute, long time) throws IllegalRouteException {
        log.debug("Request to switch to new route for vehicle {} (currently on route {})", vehicleData.getName(), currentRoute.getId());

        boolean newRouteOnOriginalRoute = isNewRouteOnOriginalRoute(rawRoute.getConnectionIds(), currentRoute.getConnectionIds());
        // no switch is needed, just stay on the previous route
        if (newRouteOnOriginalRoute) {
            log.debug("Discard route change for vehicle {}: route matches current route {}.", vehicleData.getName(), currentRoute.getId());
            return currentRoute;
        } else {
            // new route:
            // — first check if already a route exists
            //  — generate a complete route with an ID and propagate it
            VehicleRoute knownRoute = null;
            for (VehicleRoute route : getAllRoutes().values()) {
                newRouteOnOriginalRoute = isNewRouteOnOriginalRoute(rawRoute.getConnectionIds(), getAllRoutes().get(route.getId()).getConnectionIds());
                if (newRouteOnOriginalRoute) {
                    knownRoute = route;
                    break;
                }
            }
            // there already is a known route -> switch to it
            if (knownRoute != null) {
                // change route for sumo
                return requestStaticRouteChange(vehicleData, knownRoute, currentRoute, time);
            } else {
                // generate a new route
                VehicleRoute route = routing.createRouteForRTI(rawRoute);

                // propagate the new route
                try {
                    log.debug("Propagate unknown route {}.", route.getId());
                    propagateRoute(route, time);
                } catch (InternalFederateException e) {
                    log.error("[CNC.switchRoute]: unable to send propagate a new route.");
                    return currentRoute;
                }

                // change route for sumo
                return requestStaticRouteChange(vehicleData, route, currentRoute, time);
            }
        }
    }

    /**
     * Helper-method for {@link #switchRoute}.
     *
     * @param vehicleData The {@link VehicleData} of the vehicle,
     *                    which is going to switch routes.
     * @param newRoute    The anticipated {@link VehicleRoute} to switch to.
     * @param time        Time at which the route change should happen.
     * @return If the {@link VehicleRouteChange} interaction was properly send, returns new route.
     * Else returns the original route of the vehicle, stored in {@code vehicleInfo}.
     */
    private VehicleRoute requestStaticRouteChange(VehicleData vehicleData, VehicleRoute newRoute, VehicleRoute previousRoute, long time) {
        VehicleRouteChange interaction = new VehicleRouteChange(time, vehicleData.getName(), newRoute.getId());
        try {
            log.info("Change to route {} for vehicle {}.", newRoute.getId(), vehicleData.getName());
            rtiAmbassador.triggerInteraction(interaction);
        } catch (IllegalValueException | InternalFederateException e) {
            log.error("[CNC.switchRoute]: unable to send VehicleRouteChange message.");
            return previousRoute;
        }
        return newRoute;
    }

    /**
     * Get the target position of a route for a specific route ID.
     *
     * @param routeId The id of the route, that the target position is wanted of.
     * @return {@link GeoPoint}-target of the given route, {@code null} if route doesn't exist.
     */
    GeoPoint getTargetPositionOfRoute(String routeId) {
        if (getAllRoutes().containsKey(routeId)) {
            String lastConnectionId = Iterables.getLast(getAllRoutes().get(routeId).getConnectionIds(), null);
            return routing.getConnection(lastConnectionId).getEndNode().getPosition();
        } else {
            return null;
        }
    }

    /**
     * Get the source position of a route for a specific route ID.
     *
     * @param routeId The id of the route, that the source position is wanted of.
     * @return {@link GeoPoint}-target of the given route, {@code null} if route doesn't exist.
     */
    public GeoPoint getSourcePositionOfRoute(String routeId) {
        if (getAllRoutes().containsKey(routeId)) {
            String firstConnectionId = Iterables.getFirst(getAllRoutes().get(routeId).getConnectionIds(), null);
            return routing.getConnection(firstConnectionId).getStartNode().getPosition();
        } else {
            return null;
        }
    }

    /**
     * Find a public transport route from a provided position to a provided target position at a specific request time.
     *
     * @param routingRequest A {@link PtRoutingRequest} that contains
     *                       the origin, the end, the request time, and additional
     *                       routing parameters to calculate the public transport route.
     */
    PtRoutingResponse findPtRoute(PtRoutingRequest routingRequest) {
        return ptRouting.findPtRoute(routingRequest);
    }

    /**
     * Provides the current routing API implementation.
     *
     * @return The {@link Routing}.
     */
    public Routing getRouting() {
        return routing;
    }

    /**
     * Get the position of a specific node given by its id.
     *
     * @param nodeId The nodeId, that the position is wanted of
     * @return A {@link GeoPoint} representing the position of the node.
     */
    private GeoPoint getPositionOfNode(String nodeId) {
        return routing.getNode(nodeId).getPosition();
    }

    /**
     * Get the length of a specific connection.
     *
     * @param connectionId the id of the connection
     * @return the length of the connection in [m]
     */
    double getLengthOfConnection(String connectionId) {
        return routing.getConnection(connectionId).getLength();
    }

    /**
     * Propagates a newly created route to all other federates.
     *
     * @param newRoute The route to propagate.
     * @param time     Time of propagation message.
     * @throws InternalFederateException If the {@link Interaction} could not be send.
     */
    private void propagateRoute(VehicleRoute newRoute, long time) throws InternalFederateException {
        if (getAllRoutes().containsKey(newRoute.getId())) {
            throw new InternalFederateException(
                    String.format("Route %s is already known but is tried to be propagated, which is not allowed.", newRoute.getId())
            );
        }

        VehicleRouteRegistration interaction = new VehicleRouteRegistration(time, newRoute);
        try {
            this.rtiAmbassador.triggerInteraction(interaction);
            // store route in local map
            SimulationKernel.SimulationKernel.registerRoute(newRoute.getId(), newRoute);
        } catch (IllegalValueException e) {
            throw new InternalFederateException(e);
        }
    }

    /**
     * This method checks if the given route is equal to or the end of the given original route.
     *
     * @param newRoute      A list of node-ids representing the new route.
     * @param originalRoute A list of node-ids representing the original route.
     * @return Returns {@code true} if the new route is a part of the original route.
     * {@code false} if the new route is <b>NOT</b> a part of the original route.
     */
    private boolean isNewRouteOnOriginalRoute(List<String> newRoute, List<String> originalRoute) {
        // if new the new route is bigger than the original route it can't be a part of the original route
        if (newRoute.size() > originalRoute.size()) {
            return false;
        }
        // check if new route is the end of the given route
        for (int i = originalRoute.size() - 1, j = newRoute.size() - 1; j >= 0; i--, j--) {
            if (!originalRoute.get(i).equals(newRoute.get(j))) {
                return false;
            }
        }
        // everything matched
        return true;
    }

    /**
     * This method tries to create a route for an {@link OriginDestinationPair}.
     * If the {@link OriginDestinationPair} contains a valid route, that route is used for the
     * {@link VehicleDeparture}. Otherwise, a route will be generated and checked.
     * If that created route is valid it will be used for the {@link VehicleDeparture}.
     *
     * @param time      Time of propagation message
     * @param odInfo    The {@link OriginDestinationPair}, that a route should be created for.
     * @param departure Prior departure information read from configuration.
     * @return A {@link VehicleDeparture} if a valid route was found, otherwise {@code null}.
     */
    public VehicleDeparture createRouteForOdInfo(long time, OriginDestinationPair odInfo, VehicleDeparture departure) {

        if (odInfo.origin != null && odInfo.destination != null) {
            // create request
            final GeoPoint sourcePoint = chooseGeoPointInCircle(odInfo.origin);
            final GeoPoint targetPoint = chooseGeoPointInCircle(odInfo.destination);
            final RoutingParameters params = new RoutingParameters().alternativeRoutes(0).costFunction(RoutingCostFunction.Fastest);
            final RoutingRequest request = new RoutingRequest(new RoutingPosition(sourcePoint), new RoutingPosition(targetPoint), params);

            // find route
            final RoutingResponse response = routing.findRoutes(request);
            // check if best route, matches one of the existing routes and if so choose that existing route
            if (response.getBestRoute() != null) {
                VehicleRoute route = null;
                for (VehicleRoute existingRoute : getAllRoutes().values()) {
                    if (isNewRouteOnOriginalRoute(response.getBestRoute().getConnectionIds(), existingRoute.getConnectionIds())) {
                        route = existingRoute;
                        break;
                    }
                }
                if (route == null) {
                    try {
                        route = routing.createRouteForRTI(response.getBestRoute());
                        propagateRoute(route, time);
                    } catch (IllegalRouteException e) {
                        log.error("[CNC.createRouteForODInfo]: Could not create route.", e);
                        return null;
                    } catch (InternalFederateException e) {
                        log.error("[CNC.createRouteForODInfo]: unable to send PropagateRoute message.", e);
                        return null;
                    }
                }
                return new VehicleDeparture.Builder(route.getId())
                        .departureLane(departure.getLaneSelectionMode(), departure.getDepartureLane(), departure.getDeparturePos())
                        .departureSpeed(departure.getDepartureSpeedMode(), departure.getDepartureSpeed())
                        .create();
            }
        }

        log.error("[CNC.createRouteForODInfo]: Insufficient or wrong data in OD info.");
        return null;
    }

    private GeoPoint chooseGeoPointInCircle(GeoCircle origin) {
        return GeoUtils.getRandomGeoPoint(
                SimulationKernel.SimulationKernel.getRandomNumberGenerator(),
                origin.getCenter(), origin.getRadius()
        );
    }

    /**
     * This method refines the road position depending on the
     * implementation of the {@link Routing} interface this can have
     * different levels of complexity.
     *
     * @param roadPosition the {@link IRoadPosition} to be refined
     * @return the refined {@link IRoadPosition}
     */
    public IRoadPosition refineRoadPosition(IRoadPosition roadPosition) {
        if (roadPosition == null) {
            return null;
        }
        return routing.refineRoadPosition(roadPosition);
    }

    /**
     * Calculates the distance to a node given the current position
     * along a given route.<br><br>
     * The {@code upcomingNode} is important, so that the distance from the
     * starting position to the first node ahead is also taken into consideration.<br><br>
     * Note: It is assumed that {@code routeId}, {@code upcomingNode} and {@code currentPosition}
     * match together, meaning the {@code currentPosition} and {@code upcomingNode} lay on the
     * route given by the {@code routeId}. That is why we only sanity-check the {@code finalNode}
     *
     * @param routeId         The route along which the distance should be measured.
     * @param finalNode       The node that the distance should be measured to.
     * @param upcomingNode    The next node a vehicle will pass.
     * @param currentPosition The position the vehicle is currently on.
     * @return The distance from a given position to a target node along a route.
     * @throws IllegalArgumentException If the {code finalNode} is invalid.
     */
    double getDistanceToPointOnRoute(String routeId, String finalNode, String upcomingNode, GeoPoint currentPosition) throws IllegalArgumentException {
        if (finalNode == null) {
            throw new IllegalArgumentException("finalNode is null.");
        }
        List<String> currentRouteNodes = getAllRoutes().get(routeId).getNodeIds();

        if (!currentRouteNodes.contains(finalNode)) {
            return Double.POSITIVE_INFINITY;
        }
        int indexUpcomingNode = currentRouteNodes.indexOf(upcomingNode);
        int indexLastNode = currentRouteNodes.indexOf(finalNode) + 1;
        if (indexUpcomingNode > indexLastNode) {
            throw new IllegalArgumentException("finalNode lies in front of upcomingNode.");
        }
        double distanceToPoint = 0;
        List<String> nodeList = currentRouteNodes.subList(indexUpcomingNode, indexLastNode);
        for (String nodeId : nodeList) {
            distanceToPoint += currentPosition.distanceTo(getPositionOfNode(nodeId));
            currentPosition = getPositionOfNode(nodeId);
        }
        return distanceToPoint;
    }

    /**
     * This method tries to find the next node on a route, that
     * fulfills the {@code nodeCondition}.
     *
     * @param routeId       The id of the route.
     * @param roadPosition  The current position of the vehicle.
     * @param nodeCondition A condition that has to be fulfilled by the node.
     * @return A node if a valid one is found, otherwise {@code null}.
     */
    INode getNextNodeOnRoute(String routeId, IRoadPosition roadPosition, Predicate<INode> nodeCondition) {
        VehicleRoute currentRoute = getAllRoutes().get(routeId);

        int indexOfUpcomingNode = currentRoute.getNodeIds().indexOf(roadPosition.getUpcomingNode().getId());
        // check if there is an upcoming node
        if (indexOfUpcomingNode < 0) {
            return null;
        }

        List<String> nodeList = currentRoute.getNodeIds().subList(indexOfUpcomingNode, currentRoute.getNodeIds().size());
        for (String nodeId : nodeList) {
            INode node = routing.getNode(nodeId);
            if (nodeCondition.test(node)) {
                return node;
            }
        }

        return null;
    }

    /**
     * Approximates the costs for all {@link CandidateRoute}s passed to the function.
     *
     * @param candidateRoutes A collection of {@link CandidateRoute}s
     * @param lastNodeId      The ID of the Node last passed
     * @return A new collection of {@link CandidateRoute}s wit approximated costs.
     */
    @SuppressWarnings("unused")
    public Collection<CandidateRoute> approximateCosts(Collection<CandidateRoute> candidateRoutes, String lastNodeId) {
        List<CandidateRoute> routesWithCosts = new ArrayList<>();
        for (CandidateRoute candidateRoute : candidateRoutes) {
            routesWithCosts.add(routing.approximateCostsForCandidateRoute(candidateRoute, lastNodeId));
        }
        return routesWithCosts;
    }
}



 