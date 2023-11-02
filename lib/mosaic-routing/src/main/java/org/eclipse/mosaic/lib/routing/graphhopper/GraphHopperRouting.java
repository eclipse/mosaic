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
import org.eclipse.mosaic.lib.database.DatabaseUtils;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.RoutingCostFunction;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingRequest;
import org.eclipse.mosaic.lib.routing.graphhopper.algorithm.RoutingAlgorithmFactory;
import org.eclipse.mosaic.lib.routing.graphhopper.util.DatabaseGraphLoader;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;
import org.eclipse.mosaic.lib.routing.graphhopper.util.TurnCostsProvider;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
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

    public static final Profile PROFILE_CAR = new Profile("car").setVehicle("car").setTurnCosts(true);

    public static final Profile PROFILE_BIKE = new Profile("bike").setVehicle("bike").setTurnCosts(false);

    public static final List<Profile> PROFILES = new ArrayList<>();

    /**
     * The minimum number of alternatives to calculate when alternative routes have been requested.
     * GraphHopper often returns equal routes, and by calculating more than required we can
     * reduce the result to match the requested number of alternatives.
     */
    private static final int NUM_ALTERNATIVE_PATHS = 5;

    /**
     * Alternative routes may share a maximum of 70% of roads of the best route.
     */
    public static final double ALTERNATIVE_ROUTES_MAX_SHARE = 0.7;

    /**
     * Alternative routes may cost a maximum of 40% more than the best route.
     */
    public static final double ALTERNATIVE_ROUTES_MAX_WEIGHT = 1.4;

    static {
        PROFILES.add(PROFILE_CAR);
        PROFILES.add(PROFILE_BIKE);
    }

    /**
     * If the distance of the query position to the closest node is lower than this
     * value, then the closest node is used definitely as the source or target of the route.
     * If the distance is larger, the route may start or end on the connection the query is matched on.
     */
    public static final double TARGET_NODE_QUERY_DISTANCE = 1d;

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
    private VehicleEncodingManager encoding;

    public GraphHopperRouting loadGraphFromDatabase(Database db) {
        this.db = db;

        //initialize reader and mapper for database import into graphhopper
        GraphLoader reader = new DatabaseGraphLoader(db);
        graphMapper = new GraphhopperToDatabaseMapper();
        encoding = new VehicleEncodingManager(PROFILES);

        ghApi = new ExtendedGraphHopper(encoding, reader, graphMapper);

        //load graph from database
        ghApi.importOrLoad();

        return this;
    }

    public List<CandidateRoute> findRoutes(RoutingRequest routingRequest) {
        if (ghApi == null) {
            throw new IllegalStateException("Load database at first");
        }

        final Profile profile;
        if (routingRequest.getRoutingParameters().getVehicleClass() == VehicleClass.Bicycle) {
            profile = PROFILE_BIKE;
        } else {
            profile = PROFILE_CAR;
        }

        final VehicleEncoding vehicleEncoding = encoding.getVehicleEncoding(profile.getVehicle());

        final TurnCostsProvider turnCostProvider = new TurnCostsProvider(vehicleEncoding, ghApi.getBaseGraph().getTurnCostStorage());

        if (!routingRequest.getRoutingParameters().isConsiderTurnCosts()) {
            turnCostProvider.disableTurnCosts();
        }

        final GraphHopperWeighting graphhopperWeighting = new GraphHopperWeighting(vehicleEncoding, encoding.wayType(), turnCostProvider, graphMapper);

        // if there is no cost function given (initial routes), use the default
        if (routingRequest.getRoutingParameters().getRoutingCostFunction() == null) {
            graphhopperWeighting.setRoutingCostFunction(RoutingCostFunction.Default);
        } else {
            graphhopperWeighting.setRoutingCostFunction(routingRequest.getRoutingParameters().getRoutingCostFunction());
        }

        final RoutingPosition source = routingRequest.getSource();
        final RoutingPosition target = routingRequest.getTarget();

        final Snap snapSource = createQueryForSource(source, vehicleEncoding.access());
        final Snap snapTarget = createQueryForTarget(target, vehicleEncoding.access());

        if (snapSource.getClosestEdge() == null || snapTarget.getClosestEdge() == null) {
            log.warn("Could not find a route from {} to {}", routingRequest.getSource(), routingRequest.getTarget());
            return Lists.newArrayList();
        }

        final QueryGraph queryGraph = QueryGraph.create(ghApi.getBaseGraph(), snapSource, snapTarget);

        final int numberOfAlternatives = routingRequest.getRoutingParameters().getNumAlternativeRoutes();
        final PMap hints = new PMap();
        if (numberOfAlternatives > 0) {
            // We calculate more alternative routes than required, since GraphHopper often seem to return equal alternatives
            hints.putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, Math.max(numberOfAlternatives, NUM_ALTERNATIVE_PATHS) + 1);
            hints.putObject(Parameters.Algorithms.AltRoute.MAX_SHARE, ALTERNATIVE_ROUTES_MAX_SHARE);
            hints.putObject(Parameters.Algorithms.AltRoute.MAX_WEIGHT, ALTERNATIVE_ROUTES_MAX_WEIGHT);
        }

        final Weighting weighting = queryGraph.wrapWeighting(
                graphhopperWeighting
        );

        final RoutingAlgorithm algo = RoutingAlgorithmFactory.DEFAULT.createAlgorithm(queryGraph, weighting, hints);

        final List<Path> paths = algo.calcPaths(snapSource.getClosestNode(), snapTarget.getClosestNode());

        final Set<String> duplicateSet = new HashSet<>();
        final List<CandidateRoute> result = new ArrayList<>();

        // convert paths to routes
        for (final Path path : paths) {
            if (result.size() > numberOfAlternatives) {
                break;
            }
            final CandidateRoute route = convertPath(queryGraph, path, target);
            if (route != null
                    && !route.getConnectionIds().isEmpty()
                    && checkForDuplicate(route, duplicateSet)
                    && checkRouteOnRequiredSourceConnection(route, source)) {
                result.add(route);
            } else if (route != null && log.isDebugEnabled()) {
                log.debug("Path is invalid and will be ignored [" + StringUtils.join(route.getConnectionIds(), ",") + "]");
            }
        }
        return result;
    }

    private Snap createQueryForTarget(RoutingPosition target, BooleanEncodedValue accessEnc) {
        final EdgeFilter toEdgeFilter = createEdgeFilterForRoutingPosition(target, accessEnc);
        Snap queryTarget = ghApi.getLocationIndex().findClosest(target.getPosition().getLatitude(), target.getPosition().getLongitude(), toEdgeFilter);
        if (target.getConnectionId() != null) {
            return fixQueryResultIfNoClosestEdgeFound(queryTarget, target, accessEnc);
        } else {
            return fixQueryResultIfSnappedPointIsCloseToTowerNode(queryTarget, target);
        }
    }

    private Snap createQueryForSource(RoutingPosition source, BooleanEncodedValue accessEnc) {
        final EdgeFilter fromEdgeFilter = createEdgeFilterForRoutingPosition(source, accessEnc);
        Snap querySource = ghApi.getLocationIndex().findClosest(source.getPosition().getLatitude(), source.getPosition().getLongitude(), fromEdgeFilter);
        if (source.getConnectionId() != null) {
            querySource = fixQueryResultIfSnappedPointIsTowerNode(querySource, source, fromEdgeFilter);
            return fixQueryResultIfNoClosestEdgeFound(querySource, source, accessEnc);
        } else {
            return fixQueryResultIfSnappedPointIsCloseToTowerNode(querySource, source);
        }
    }

    private Snap fixQueryResultIfSnappedPointIsCloseToTowerNode(Snap queryResult, RoutingPosition target) {
        if (queryResult.getSnappedPosition() == Snap.Position.TOWER || target.getConnectionId() != null) {
            return queryResult;
        }
        Node closestNode = graphMapper.toNode(queryResult.getClosestNode());
        /* If the query result is snapped to an edge, but the matched node is very close to the query, than we use the actual closest node
         * as the target of the route.*/
        if (closestNode != null && target.getPosition().distanceTo(closestNode.getPosition()) < TARGET_NODE_QUERY_DISTANCE) {
            queryResult.setSnappedPosition(Snap.Position.TOWER);
        }
        return queryResult;
    }

    private Snap fixQueryResultIfNoClosestEdgeFound(Snap queryResult, RoutingPosition routingPosition, BooleanEncodedValue accessEnc) {
        if (queryResult.getClosestEdge() == null) {
            log.warn("Wrong routing request: The from-connection {} does not fit with the given position {}", routingPosition.getConnectionId(), routingPosition.getPosition());

            return ghApi.getLocationIndex().findClosest(
                    routingPosition.getPosition().getLatitude(), routingPosition.getPosition().getLongitude(), AccessFilter.allEdges(accessEnc)
            );
        }
        return queryResult;
    }

    private Snap fixQueryResultIfSnappedPointIsTowerNode(Snap queryResult, RoutingPosition routingPosition, EdgeFilter fromEdgeFilter) {
        /* If the requested position is in front or behind the edge it is mapped either on the start or end of the edge (one of the tower nodes).
         * As a result, the resulting route can bypass turn restrictions in very rare cases. To avoid this, we choose an alternative
         * node based on the queried connection.*/
        if (queryResult.getSnappedPosition() == Snap.Position.TOWER) {
            // use the node before target node (index -2) as the alternative query node to find a QueryResult _on_ the connection.
            Node alternativeQueryNode = DatabaseUtils.getNodeByIndex(db.getConnection(routingPosition.getConnectionId()), -2);
            if (alternativeQueryNode != null) {
                return ghApi.getLocationIndex().findClosest(
                        alternativeQueryNode.getPosition().getLatitude(), alternativeQueryNode.getPosition().getLongitude(), fromEdgeFilter
                );
            }
        }
        return queryResult;
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

    private EdgeFilter createEdgeFilterForRoutingPosition(final RoutingPosition position, final BooleanEncodedValue accessEnc) {
        if (position.getConnectionId() == null) {
            return AccessFilter.allEdges(accessEnc);
        }
        final int forcedEdge = graphMapper.fromConnection(db.getConnection(position.getConnectionId()));
        if (forcedEdge < 0) {
            return AccessFilter.allEdges(accessEnc);
        }
        return edgeState -> edgeState.getEdge() == forcedEdge;
    }

    private CandidateRoute convertPath(Graph graph, Path newPath, RoutingPosition targetPosition) {
        PointList pointList = newPath.calcPoints();
        if (pointList.isEmpty()) {
            return null;
        }
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

        double offsetFromSource = 0;
        int lastNode = -1;
        Connection lastConnection = null;
        NodeAccess nodes = graph.getNodeAccess();

        while (edgesIt.hasNext()) {
            EdgeIteratorState origEdge = edgesIt.next();
            EdgeIteratorState currEdge = origEdge;

            if (currEdge instanceof VirtualEdgeIteratorState) {
                currEdge = ghApi.getBaseGraph().getEdgeIteratorStateForKey(((VirtualEdgeIteratorState) origEdge).getOriginalEdgeKey());
            }

            Connection con = graphMapper.toConnection(currEdge.getEdge());
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

                if (pathConnections.isEmpty()) {
                    offsetFromSource = calcOffset(con, origEdge.getBaseNode(), nodes);
                }

                pathConnections.add(con.getId());
                lastNode = origEdge.getAdjNode();
                lastConnection = con;

                if (Double.isInfinite(newPath.getWeight())) {
                    log.warn(
                            "Something went wrong during path search: The found route has infinite weight. Maybe there's a turn restriction or unconnected "
                                    + "sub-graphs in the network. Route will be ignored.");
                    return null;
                }
            } else {
                log.debug(String.format("A connection could be resolved by internal ID %d.", origEdge.getEdge()));
            }
        }

        double offsetToTarget = 0;
        if (lastConnection != null) {
            offsetToTarget = lastConnection.getLength() - calcOffset(lastConnection, lastNode, nodes);
        }

        return new CandidateRoute(
                pathConnections,
                newPath.getDistance(),
                newPath.getTime() / 1000.0,
                offsetFromSource,
                offsetToTarget
        );
    }

    private double calcOffset(Connection con, int node, NodeAccess nodes) {
        GeoPoint onConnection = GeoPoint.latLon(nodes.getLat(node), nodes.getLon(node));
        double offset = 0;
        Node prev = null;
        for (Node curr : con.getNodes()) {
            if (prev != null) {
                double distancePrevToCurr = prev.getPosition().distanceTo(curr.getPosition());
                double distancePrevToNode = prev.getPosition().distanceTo(onConnection);
                if (distancePrevToNode < distancePrevToCurr) {
                    return Math.max(con.getLength(), offset + distancePrevToNode);
                }
                offset += distancePrevToCurr;
            }
            prev = curr;
        }
        return con.getLength();
    }

    private boolean checkRouteOnRequiredSourceConnection(CandidateRoute route, RoutingPosition source) {
        if (source.getConnectionId() != null) {
            return source.getConnectionId().equals(route.getConnectionIds().get(0));
        }
        return true;
    }

}
