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

import com.carrotsearch.hppc.cursors.IntCursor;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GraphHopperRouting {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private GraphHopper ghApi;

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

        final Node prefixNode = determinePrefixNode(source);
        final Node endNode = determineEndNode(target);

        final Set<String> duplicateSet = new HashSet<>();

        // convert paths to routes
        for (Path path : paths) {
            final CandidateRoute route = preparePath(queryGraph, path, prefixNode, endNode);
            if (route != null
                    && route.getNodeIdList().size() > 1
                    && checkForDuplicate(route, duplicateSet)
                    && checkRouteOnRequiredSourceConnection(route, source)) {
                routesList.add(route);
            }
        }
        return routesList;
    }

    private QueryResult fixQueryResultIfNoClosestEdgeFound(RoutingPosition source, QueryResult queryResultOriginal, FlagEncoder flagEncoder) {
        if (queryResultOriginal.getClosestEdge() == null && source.getConnectionID() != null) {
            log.warn("Wrong routing request: The from-connection {} does not fit with the given position {}", source.getConnectionID(), source.getPosition());
            queryResultOriginal = ghApi.getLocationIndex().findClosest(source.getPosition().getLatitude(), source.getPosition().getLongitude(), DefaultEdgeFilter.allEdges(flagEncoder));
        }
        return queryResultOriginal;
    }

    private QueryResult fixQueryResultIfSnappedPointIsTowerNode(QueryResult queryResultOriginal, RoutingPosition routingPosition, EdgeFilter fromEdgeFilter, Node fallbackNode) {
        /* If the requested position is outside of the edge it is mapped either on the start or end of the edge (one of the tower nodes).
         * As a result, the resulting route can bypass turn restrictions in very rare cases. To avoid this, we can choose a fallback
         * node to be the requested position instead of the routing position.*/
        if (queryResultOriginal.getSnappedPosition() == QueryResult.Position.TOWER && routingPosition.getConnectionID() != null) {
            queryResultOriginal = ghApi.getLocationIndex().findClosest(fallbackNode.getPosition().getLatitude(), fallbackNode.getPosition().getLongitude(),
                    fromEdgeFilter);
        }
        return queryResultOriginal;
    }

    /**
     * Checks the {@param duplicateSet} whether it contains the {@param route}'s nodeIdList.
     *
     * @param route Route to check for duplicate.
     * @param duplicateSet Set of node Ids.
     * @return True, if not duplicate in the set.
     */
    private boolean checkForDuplicate(CandidateRoute route, Set<String> duplicateSet) {
        String nodeIdList = StringUtils.join(route.getNodeIdList(), ",");
        return duplicateSet.add(nodeIdList);
    }

    private EdgeFilter createEdgeFilterForRoutingPosition(final RoutingPosition position, final FlagEncoder flagEncoder) {
        if (position.getConnectionID() == null) {
            return DefaultEdgeFilter.allEdges(flagEncoder);
        }
        final int forcedEdge = graphMapper.fromConnection(db.getConnection(position.getConnectionID()));
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
        if (source.getNodeID() != null) {
            return db.getNode(source.getNodeID());
        } else if (source.getConnectionID() != null) {
            return db.getConnection(source.getConnectionID()).getFrom();
        }
        return getClosestNode(source.getPosition());
    }

    /**
     * Determines the end node of the {@param target}. If no node available, the closest node to the {@param target}'s geo location is returned.
     *
     * @param target Target position as {@link RoutingPosition}.
     * @return End node of the target.
     */
    private Node determineEndNode(final RoutingPosition target) {
        if (target.getNodeID() != null) {
            return db.getNode(target.getNodeID());
        } else if (target.getConnectionID() != null) {
            return db.getConnection(target.getConnectionID()).getTo();
        }
        return getClosestNode(target.getPosition());
    }

    private Node getClosestNode(GeoPoint position) {
        if (nodeFinder == null) {
            nodeFinder = new NodeFinder(db);
        }
        return nodeFinder.findClosestNode(position);
    }

    private CandidateRoute preparePath(QueryGraph queryGraph, Path newPath, Node prefixNode, Node endNode) {
        Iterator<IntCursor> nodesIt = newPath.calcNodes().iterator();
        if (!nodesIt.hasNext()) {
            return null;
        }

        final List<String> pathVertices = new ArrayList<String>();

        while (nodesIt.hasNext()) {
            int internalId = nodesIt.next().value;

            /*
             * Skip nodes that are not part of the scenario database.
             * These nodes are just used for routing between database nodes
             * and are not needed anymore.
             */
            if (queryGraph.isVirtualNode(internalId)) {
                continue;
            }


            Node node = graphMapper.toNode(internalId);
            if (node != null) {
                pathVertices.add(node.getId());

                if (Double.isInfinite(newPath.getWeight())) {
                    log.warn(
                            "Something went wrong during path search: The found route has infinite weight. Maybe there's a turn restriction or unconnected "
                                    + "sub-graphs in the network. Route will be ignored.");
                    return null;
                }
            } else {
                log.debug(String.format("A node could be resolved by internal ID %d.", internalId));
            }
        }

        return new CandidateRoute(buildCompleteNodeIdList(prefixNode, pathVertices, endNode), newPath.getDistance(),
                newPath.getTime() / (double) 1000);
    }

    /**
     * This creates a complete list of node ID's out of the given node ID list.
     * This should be used for the node ID list of a CandidateRoute for
     * comparison with the node ID list of an existing route.
     *
     * @param abstractNodeIdList
     * @return complete List<NodeIds>
     */
    private List<String> buildCompleteNodeIdList(Node prefixNode, List<String> abstractNodeIdList, Node finalNode) {
        if (abstractNodeIdList.size() == 0) {
            return abstractNodeIdList;
        }
        NodeListLastItemCheck completed = new NodeListLastItemCheck();

        if (prefixNode != null) {
            //fill from prefix node until start of path
            completed = fillNodeListFromPrefix(completed, prefixNode, abstractNodeIdList);
        }

        //create list of all nodes on path (including nodes on connections)
        completed = fillNodeListOfAbstractPath(completed, abstractNodeIdList);

        if (finalNode != null) {
            //fill from end of path until final node
            completed = fillNodeListToFinalNode(completed, abstractNodeIdList, finalNode);
        }

        //transform to id list
        final List<String> nodeIds = new ArrayList<>(completed.size());
        for (Node node : completed) {
            nodeIds.add(node.getId());
        }
        return nodeIds;
    }

    private boolean checkRouteOnRequiredSourceConnection(CandidateRoute route, RoutingPosition source) {
        if (source.getConnectionID() != null) {
            Connection con = db.getConnection(source.getConnectionID());
            String node1 = Iterables.get(route.getNodeIdList(), 0);
            String node2 = Iterables.get(route.getNodeIdList(), 1);

            int indexOnConnectionNode1 = -1;
            int indexOnConnectionNode2 = -1;

            int i = 0;
            for (Node n : con.getNodes()) {
                if (n.getId().equals(node1)) {
                    indexOnConnectionNode1 = i;
                }
                if (n.getId().equals(node2)) {
                    indexOnConnectionNode2 = i;
                }
                i++;
            }

            if (!(indexOnConnectionNode1 >= 0 && indexOnConnectionNode2 > indexOnConnectionNode1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This fills the node list with the nodes between the prefix node and the end node.
     *
     * @param nodeListResult List to store the nodes.
     * @param prefixNode Current node of the route.
     * @param abstractNodeIdList Node list of the current route.
     * @return
     */
    private NodeListLastItemCheck fillNodeListFromPrefix(final NodeListLastItemCheck nodeListResult, final Node prefixNode, final List<String> abstractNodeIdList) {
        final Node firstNode = db.getNode(Iterables.getFirst(abstractNodeIdList, null));
        for (Connection incoming : firstNode.getIncomingConnections()) {
            int indexOfNode = incoming.getNodes().indexOf(prefixNode);
            if (indexOfNode >= 0) {
                for (Node node : incoming.getNodes().subList(indexOfNode, incoming.getNodes().size())) {
                    nodeListResult.add(node);
                }
            }
        }
        return nodeListResult;
    }

    private NodeListLastItemCheck fillNodeListOfAbstractPath(final NodeListLastItemCheck nodeListResult, final List<String> abstractNodeIdList) {
        List<Connection> candidateConnections = new LinkedList<>();
        Node lastNode = null;
        Node currentNode;
        for (String nodeId : abstractNodeIdList) {
            currentNode = db.getNode(nodeId);

            if (lastNode != null) {
                candidateConnections.clear();
                for (Connection outFromLast : lastNode.getOutgoingConnections()) {
                    for (Connection inFromCurrent : currentNode.getIncomingConnections()) {
                        if (inFromCurrent == outFromLast) {
                            candidateConnections.add(inFromCurrent);
                        }
                    }
                }

                // special case: if there are two parallel connections with the same from- and to-node, then we choose the fastest one
                Connection fastest = null;
                for (Connection conBetween : candidateConnections) {
                    if (fastest == null
                            || (fastest.getLength() / fastest.getMaxSpeedInMs()) > (conBetween.getLength() / conBetween.getMaxSpeedInMs())) {
                        fastest = conBetween;
                    }
                }
                if (fastest != null) {
                    nodeListResult.addAll(fastest.getNodes());
                }

            }
            lastNode = currentNode;
        }
        return nodeListResult;
    }

    private NodeListLastItemCheck fillNodeListToFinalNode(
            final NodeListLastItemCheck nodeListResult, final List<String> abstractNodeIdList, final Node finalNode
    ) {

        final Node lastNode = db.getNode(Iterables.getLast(abstractNodeIdList, null));
        for (Connection outgoing : lastNode.getOutgoingConnections()) {
            int indexOfNode = outgoing.getNodes().indexOf(finalNode);
            if (indexOfNode >= 0) {
                nodeListResult.addAll(outgoing.getNodes().subList(0, indexOfNode + 1));
            }
        }
        return nodeListResult;
    }

    static class NodeListLastItemCheck extends ArrayList<Node> {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(Node e) {
            if (size() > 0 && get(size() - 1) == e) {
                return false;
            }
            return super.add(e);
        }

        @Override
        public boolean addAll(Collection<? extends Node> c) {
            boolean result = false;
            for (Node n : c) {
                result |= add(n);
            }
            return result;
        }
    }

}
