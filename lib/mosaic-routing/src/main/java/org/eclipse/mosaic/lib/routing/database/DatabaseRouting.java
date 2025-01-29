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

package org.eclipse.mosaic.lib.routing.database;

import static java.lang.Double.min;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.route.Route;
import org.eclipse.mosaic.lib.database.spatial.Edge;
import org.eclipse.mosaic.lib.database.spatial.EdgeFinder;
import org.eclipse.mosaic.lib.database.spatial.NodeFinder;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoUtils;
import org.eclipse.mosaic.lib.objects.road.IConnection;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.IllegalRouteException;
import org.eclipse.mosaic.lib.routing.RoutingRequest;
import org.eclipse.mosaic.lib.routing.RoutingResponse;
import org.eclipse.mosaic.lib.routing.VehicleRouting;
import org.eclipse.mosaic.lib.routing.config.CVehicleRouting;
import org.eclipse.mosaic.lib.routing.graphhopper.GraphHopperRouting;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the {@link VehicleRouting} interface which provides access to routing functions
 * based on data of the scenario-database.
 */
public class DatabaseRouting implements VehicleRouting {

    private final static Logger log = LoggerFactory.getLogger(DatabaseRouting.class);

    private Database scenarioDatabase;
    private RouteManager routeManager;
    private EdgeFinder edgeFinder;
    private NodeFinder nodeFinder;

    private GraphHopperRouting routing;

    @Override
    public void initialize(final CVehicleRouting configuration, final File baseDirectory) throws InternalFederateException {

        File dbFile;
        // try to find the database file
        if (configuration == null || configuration.source == null) {
            dbFile = seekDatabase(baseDirectory);
        } else {
            dbFile = new File(configuration.source).exists()
                    ? new File(configuration.source)
                    : new File(baseDirectory, configuration.source);
        }
        log.trace("loading database '" + dbFile.getAbsolutePath() + "'");

        // actually try to load
        try {
            this.scenarioDatabase = Database.loadFromFile(dbFile);
        } catch (RuntimeException re) {
            throw new InternalFederateException("Could not load database file! Invalid type or outdated?", re);
        }

        //creates an implementation of IRoutingGraph according to the configuration
        this.routing = new GraphHopperRouting(scenarioDatabase);

        this.routeManager = new RouteManager(this.scenarioDatabase);
    }

    @Override
    public Map<String, VehicleRoute> getRoutesFromDatabaseForMessage() {
        return routeManager.getRoutesFromDatabaseForMessage();
    }

    @Override
    public double getMaxSpeedOfConnection(String scenarioDatabaseConnectionId) {

        Connection connection = scenarioDatabase.getConnection(scenarioDatabaseConnectionId);
        if (connection == null) {
            log.warn("Could not determine connection for given ID {}.", scenarioDatabaseConnectionId);
            return 0;
        }
        return connection.getMaxSpeedInMs();
    }

    @Override
    public INode getNode(String nodeId) {
        Node n = scenarioDatabase.getNode(nodeId);
        if (n == null) {
            throw new IllegalArgumentException(String.format("No such (%s) node existing.", nodeId));
        }
        return new LazyLoadingNode(n);
    }

    @Override
    public IConnection getConnection(String connectionId) {
        Connection c = scenarioDatabase.getConnection(connectionId);
        if (c == null) {
            throw new IllegalArgumentException(String.format("No such (%s) connection existing.", connectionId));
        }
        return new LazyLoadingConnection(c);
    }

    /**
     * This method finds alternative routes including the "best" route in regards to the given cost function.
     *
     * @param routingRequest Information for a routing request.
     * @return Response to the routing request.
     */
    @Override
    public RoutingResponse findRoutes(RoutingRequest routingRequest) {
        final List<CandidateRoute> candidateRoutes = this.routing.findRoutes(routingRequest);
        final CandidateRoute bestRoute = Iterables.getFirst(candidateRoutes, null);
        final List<CandidateRoute> alternatives;
        if (candidateRoutes.size() > 1) {
            alternatives = candidateRoutes.subList(1, candidateRoutes.size());
        } else {
            alternatives = Lists.newArrayList();
        }
        return new RoutingResponse(bestRoute, alternatives);
    }

    @Override
    public VehicleRoute createRouteForRTI(CandidateRoute candidateRoute) throws IllegalRouteException {
        Route route = routeManager.createRouteByCandidateRoute(candidateRoute);
        return routeManager.createRouteForRTI(route);
    }


    /**
     * Tries to find a database file inside any folder inside the configuration directory.
     *
     * @param baseDir Directory which contains the database file.
     * @throws InternalFederateException if database file does not exist.
     */
    private File seekDatabase(File baseDir) throws InternalFederateException {

        // try to find the database file
        // first from a json-string
        String dbFileAbsolutePath = findFileWithExtension(baseDir, "db");

        // last but not least: we failed in our task, throw exception
        if (dbFileAbsolutePath == null) {
            throw new InternalFederateException(
                    "Navigation database file not configured and could not be found automatically."
            );
        }
        return new File(dbFileAbsolutePath);
    }

    /**
     * Tries the find a file depends on the extension.
     *
     * @param baseDir   Directory to the file.
     * @param extension Extension to find.
     * @return The absolute path of the file or null if the path is not existent.
     */
    private String findFileWithExtension(File baseDir, String extension) {
        File[] dirs = baseDir.listFiles();
        if (dirs == null) {
            return null;
        }
        for (File file : dirs) {
            if (file.getName().endsWith("." + extension)) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * This method refines the road position while obtaining the missing information from the database.
     *
     * @param roadPosition {@link IRoadPosition} containing position information such as upcoming and previous node Ids.
     * @return The refined road position as{@link IRoadPosition}.
     */
    @Override
    public IRoadPosition refineRoadPosition(IRoadPosition roadPosition) {
        if (roadPosition instanceof LazyLoadingRoadPosition) {
            return roadPosition;
        }
        return new LazyLoadingRoadPosition(roadPosition, scenarioDatabase);
    }

    @Override
    public CandidateRoute approximateCostsForCandidateRoute(CandidateRoute route, String lastNodeId) {
        double length = 0;
        double time = 0;
        for (String connectionId : route.getConnectionIds()) {
            Connection con = getScenarioDatabase().getConnection(connectionId);
            length += con.getLength();
            time += con.getLength() / con.getMaxSpeedInMs();
        }
        return new CandidateRoute(route.getConnectionIds(), length, time);
    }

    private List<Edge> findClosestEdges(GeoPoint location) {
        if (edgeFinder == null) {
            edgeFinder = new EdgeFinder(scenarioDatabase);
        }
        return edgeFinder.findClosestEdges(location);
    }

    private Edge findClosestEdge(GeoPoint location, double heading) {
        if (edgeFinder == null) {
            edgeFinder = new EdgeFinder(scenarioDatabase);
        }
        return edgeFinder.findClosestEdge(location, heading);
    }

    /**
     * Searches for the closest {@link IRoadPosition} to a given geo location.
     *
     * @return Closest edge to the given location.
     */
    @Override
    public IRoadPosition findClosestRoadPosition(GeoPoint location) {
        List<Edge> closestEdges = findClosestEdges(location);
        if (closestEdges == null || closestEdges.isEmpty()) {
            return null;
        }
        if (closestEdges.size() > 1) {
            log.info("findClosestRoadPosition returned more than one edge, returning first result.");
        }
        Edge closestEdge = closestEdges.get(0);
        return redefineRoadPosition(location, closestEdge);
    }

    @Override
    public IRoadPosition findClosestRoadPosition(GeoPoint point, double heading) {
        Edge closestEdge = findClosestEdge(point, heading);
        if (closestEdge == null) {
            return null;
        }
        return redefineRoadPosition(point, closestEdge);
    }

    private static LazyLoadingRoadPosition redefineRoadPosition(GeoPoint location, Edge closestEdge) {
        LazyLoadingNode previousNode = new LazyLoadingNode(closestEdge.getPreviousNode());
        LazyLoadingNode upcomingNode = new LazyLoadingNode(closestEdge.getNextNode());

        GeoPoint startOfEdge = closestEdge.getPreviousNode().getPosition();
        GeoPoint endOfEdge = closestEdge.getNextNode().getPosition();
        GeoPoint closestPointOnEdge = GeoUtils.closestPointOnLine(location, startOfEdge, endOfEdge);
        double distanceFromStart = closestEdge.getPreviousNode().getPosition().distanceTo(closestPointOnEdge);
        distanceFromStart = min(distanceFromStart, previousNode.getPosition().distanceTo(upcomingNode.getPosition()));

        return new LazyLoadingRoadPosition(
                new LazyLoadingConnection(closestEdge.getConnection()), previousNode, upcomingNode, distanceFromStart
        );
    }

    @Override
    public INode findClosestNode(GeoPoint point) {
        if (nodeFinder == null) {
            nodeFinder = new NodeFinder(scenarioDatabase);
        }
        Node node = nodeFinder.findClosestNode(point);
        return node != null
                ? new LazyLoadingNode(node)
                : null;
    }

    /**
     * Provides the {@link Database} of the simulation scenario.
     *
     * @return the {@link Database} containing all required information (nodes, ways, etc).
     */
    public Database getScenarioDatabase() {
        return scenarioDatabase;
    }

    @Override
    public CartesianRectangle getScenarioBounds() {
        return scenarioDatabase.getBoundingBox().toCartesian();
    }
}
