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

package org.eclipse.mosaic.lib.routing.graphhopper;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.spatial.NodeFinder;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.RoutingCostFunction;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingRequest;
import org.eclipse.mosaic.lib.routing.graphhopper.algorithm.AlternativeRoutesRoutingAlgorithm;
import org.eclipse.mosaic.lib.routing.graphhopper.algorithm.DijkstraCamvitChoiceRouting;
import org.eclipse.mosaic.lib.routing.graphhopper.extended.ExtendedGraphHopper;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.BikeFlagEncoder;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.storage.TurnCostExtension;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GraphHopperRouting {

    /**
     * If the requested target point is this X meters away from the last node of
     * the found route, another connection is added on which the target
     * point is matched on.
     */
    public static double TARGET_REQUEST_CONNECTION_THRESHOLD = 5d;

    /**
     * Sometimes edges are dead ends. In these cases routing fails and invalid
     * routes are returned. To omit this, we check if the distance between the route end
     * and the original query target lies within the given threshold.
     */
    private static final double MAX_DISTANCE_TO_TARGET = 500d;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private GraphHopper ghApi;
    private final DistanceCalc distanceCalculation = new DistancePlaneProjection();

    private GraphhopperToDatabaseMapper graphMapper;
    private Database db;
    private NodeFinder nodeFinder;

    public GraphHopperRouting loadGraphFromDatabase(Database db) {
        this.db = db;

        //initialize reader and mapper for database import into graphhopper
        GraphLoader reader = new DatabaseGraphLoader(db);
        graphMapper = new GraphhopperToDatabaseMapper();

        ghApi = new ExtendedGraphHopper(reader, graphMapper).forDesktop();
        //we only need a encoder for speed,flag and turn costs for cars
        ghApi.setEncodingManager(EncodingManager.create(
                new CarFlagEncoder(5, 5, 127),
                new BikeFlagEncoder(4, 2, 0)
        ));

        //load graph from database
        ghApi.importOrLoad();
        return this;
    }

    public List<CandidateRoute> findRoutes(RoutingRequest routingRequest) {
        if (ghApi == null) {
            throw new IllegalStateException("Load database at first");
        }

        final FlagEncoder flagEncoder;
        if (routingRequest.getRoutingParameters().getVehicleClass() == VehicleClass.Bicycle) {
            flagEncoder = ghApi.getEncodingManager().getEncoder("bike");
        } else {
            flagEncoder = ghApi.getEncodingManager().getEncoder("car");
        }

        final GraphHopperWeighting graphhopperWeighting = new GraphHopperWeighting(flagEncoder, graphMapper);

        // if there is no cost function given (initial routes), use the default
        if (routingRequest.getRoutingParameters().getRoutingCostFunction() == null) {
            graphhopperWeighting.setRoutingCostFunction(RoutingCostFunction.Default);
        } else {
            graphhopperWeighting.setRoutingCostFunction(routingRequest.getRoutingParameters().getRoutingCostFunction());
        }

        final RoutingPosition source = routingRequest.getSource();
        final RoutingPosition target = routingRequest.getTarget();

        // prepare
        final List<Path> paths = new ArrayList<>();
        final List<CandidateRoute> routesList = new ArrayList<>();

        final EdgeFilter fromEdgeFilter = createEdgeFilterForRoutingPosition(source, flagEncoder);
        final EdgeFilter toEdgeFilter = createEdgeFilterForRoutingPosition(target, flagEncoder);

        QueryResult qrFrom = ghApi.getLocationIndex().findClosest(source.getPosition().getLatitude(), source.getPosition().getLongitude(), fromEdgeFilter);
        qrFrom = fixQueryResultIfSnappedPointIsTowerNode(qrFrom, source, fromEdgeFilter, determinePrefixNode(source));
        qrFrom = fixQueryResultIfNoClosestEdgeFound(source, qrFrom, flagEncoder);

        QueryResult qrTo = ghApi.getLocationIndex().findClosest(target.getPosition().getLatitude(), target.getPosition().getLongitude(), toEdgeFilter);
        qrTo = fixQueryResultIfNoClosestEdgeFound(target, qrTo, flagEncoder);

        if (qrFrom.getClosestEdge() == null || qrTo.getClosestEdge() == null) {
            log.warn("Could not find a route from {} to {}", routingRequest.getSource(), routingRequest.getTarget());
            return Lists.newArrayList();
        }

        final QueryGraph queryGraph = new QueryGraph(ghApi.getGraphHopperStorage());
        queryGraph.lookup(qrFrom, qrTo);

        final TurnWeighting weighting = new TurnWeightingOptional(graphhopperWeighting, (TurnCostExtension) queryGraph.getExtension(), routingRequest.getRoutingParameters().isConsiderTurnCosts());

        // create algorithm
        AlternativeRoutesRoutingAlgorithm algo = new DijkstraCamvitChoiceRouting(queryGraph, weighting);
        algo.setRequestAlternatives(routingRequest.getRoutingParameters().getNumAlternativeRoutes());

        // Calculates all paths and returns the best one
        paths.add(algo.calcPath(qrFrom.getClosestNode(), qrTo.getClosestNode()));

        //add alternative paths to path set
        paths.addAll(algo.getAlternativePaths());

        final Set<String> duplicateSet = new HashSet<>();

        // convert paths to routes
        for (Path path : paths) {
            final CandidateRoute route = preparePath(path, queryGraph, qrFrom, qrTo, target);
            if (route != null
                    && !route.getConnectionIds().isEmpty()
                    && checkForDuplicate(route, duplicateSet)
                    && checkRouteOnRequiredSourceConnection(route, source)) {
                routesList.add(route);
            }
        }
        return routesList;
    }

    private QueryResult fixQueryResultIfNoClosestEdgeFound(RoutingPosition source, QueryResult queryResultOriginal, FlagEncoder flagEncoder) {
        if (queryResultOriginal.getClosestEdge() == null && source.getConnectionId() != null) {
            log.warn("Wrong routing request: The from-connection {} does not fit with the given position {}", source.getConnectionId(), source.getPosition());
            queryResultOriginal = ghApi.getLocationIndex().findClosest(source.getPosition().getLatitude(), source.getPosition().getLongitude(), DefaultEdgeFilter.allEdges(flagEncoder));
        }
        return queryResultOriginal;
    }

    private QueryResult fixQueryResultIfSnappedPointIsTowerNode(QueryResult queryResultOriginal, RoutingPosition routingPosition, EdgeFilter fromEdgeFilter, Node fallbackNode) {
        /* If the requested position is outside of the edge it is mapped either on the start or end of the edge (one of the tower nodes).
         * As a result, the resulting route can bypass turn restrictions in very rare cases. To avoid this, we can choose a fallback
         * node to be the requested position instead of the routing position.*/
        if (queryResultOriginal.getSnappedPosition() == QueryResult.Position.TOWER && routingPosition.getConnectionId() != null) {
            queryResultOriginal = ghApi.getLocationIndex().findClosest(fallbackNode.getPosition().getLatitude(), fallbackNode.getPosition().getLongitude(),
                    fromEdgeFilter);
        }
        return queryResultOriginal;
    }

    /**
     * Checks the {@param duplicateSet} whether it contains the {@param route}'s nodeIdList.
     *
     * @param route        Route to check for duplicate.
     * @param duplicateSet Set of node Ids.
     * @return True, if not duplicate in the set.
     */
    private boolean checkForDuplicate(CandidateRoute route, Set<String> duplicateSet) {
        String nodeIdList = StringUtils.join(route.getConnectionIds(), ",");
        return duplicateSet.add(nodeIdList);
    }

    private EdgeFilter createEdgeFilterForRoutingPosition(final RoutingPosition position, final FlagEncoder flagEncoder) {
        if (position.getConnectionId() == null) {
            return DefaultEdgeFilter.allEdges(flagEncoder);
        }
        final int forcedEdge = graphMapper.fromConnection(db.getConnection(position.getConnectionId()));
        if (forcedEdge < 0) {
            return DefaultEdgeFilter.allEdges(flagEncoder);
        }
        return edgeState -> edgeState.getEdge() == forcedEdge;
    }

    /**
     * Determines the prefix node of the {@param source}. If no node available, the closest node to the {@param source}'s geo location is returned.
     *
     * @param source Source position {@link RoutingPosition}.
     * @return Node of the source.
     */
    private Node determinePrefixNode(final RoutingPosition source) {
        if (source.getConnectionId() != null) {
            return db.getConnection(source.getConnectionId()).getFrom();
        }
        return getClosestNode(source.getPosition());
    }

    private Node getClosestNode(GeoPoint position) {
        if (nodeFinder == null) {
            nodeFinder = new NodeFinder(db);
        }
        return nodeFinder.findClosestNode(position);
    }

    private CandidateRoute preparePath(Path newPath, QueryGraph queryGraph, QueryResult source, QueryResult target, RoutingPosition targetPosition) {
        PointList pointList = newPath.calcPoints();
        GHPoint pathTarget = Iterables.getLast(pointList);
        GHPoint origTarget = new GHPoint(targetPosition.getPosition().getLatitude(), targetPosition.getPosition().getLongitude());
        double distanceToOriginalTarget = distanceCalculation.calcDist(pathTarget.lat, pathTarget.lon, origTarget.lat, origTarget.lon);
        if (distanceToOriginalTarget > MAX_DISTANCE_TO_TARGET) {
            return null;
        }
        Iterator<EdgeIteratorState> edgesIt = newPath.calcEdges().iterator();
        if (!edgesIt.hasNext()) {
            return null;
        }

        List<String> pathConnections = new ArrayList<>();

        while (edgesIt.hasNext()) {
            EdgeIteratorState ghEdge = edgesIt.next();

            /*
             * If the requested source or target point is in the middle of the road, an artificial node
             * (and artificial edges) is created in the QueryGraph. As a consequence, the first
             * and/or last edge of the route might be such virtual edge. We use the queried source
             * and target to extract the original edge where the requested points have been matched on.
             */
            if (queryGraph.isVirtualEdge(ghEdge.getEdge())) {
                if (pathConnections.isEmpty() && queryGraph.isVirtualNode(source.getClosestNode())) {
                    ghEdge = queryGraph.getOriginalEdgeFromVirtNode(source.getClosestNode());
                } else if (!edgesIt.hasNext() && queryGraph.isVirtualNode(target.getClosestNode())) {
                    ghEdge = queryGraph.getOriginalEdgeFromVirtNode(target.getClosestNode());
                } else {
                    continue;
                }
            }

            Connection con = graphMapper.toConnection(ghEdge.getEdge());
            if (con != null) {
                /*
                 * In some cases, virtual edges are created at the target even though they are only some
                 * centimeters away from the requested node. In that case, we would have an unwanted
                 * last connection at the end of the route, which is eliminated here.
                 */
                boolean lastConnectionStartsAtTarget = !edgesIt.hasNext()
                        && targetPosition.getConnectionId() == null
                        && targetPosition.getPosition().distanceTo(con.getFrom().getPosition()) < TARGET_REQUEST_CONNECTION_THRESHOLD;
                if (lastConnectionStartsAtTarget) {
                    continue;
                }

                pathConnections.add(con.getId());

                if (Double.isInfinite(newPath.getWeight())) {
                    log.warn(
                            "Something went wrong during path search: The found route has infinite weight. Maybe there's a turn restriction or unconnected "
                                    + "sub-graphs in the network. Route will be ignored.");
                    return null;
                }
            } else {
                log.debug(String.format("A connection could be resolved by internal ID %d.", ghEdge.getEdge()));
            }
        }

        return new CandidateRoute(pathConnections, newPath.getDistance(), newPath.getTime() / (double) 1000);
    }

    private boolean checkRouteOnRequiredSourceConnection(CandidateRoute route, RoutingPosition source) {
        if (source.getConnectionId() != null) {
            return source.getConnectionId().equals(route.getConnectionIds().get(0));
        }
        return true;
    }

}
