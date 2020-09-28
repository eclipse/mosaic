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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingRequest;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GraphHopperRoutingTest {

    @Rule
    public GeoProjectionRule transformationRule = new GeoProjectionRule(GeoPoint.latLon(52, 13));

    private final static String dbFile = "/tiergarten.db";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Database database;
    private GraphHopperRouting routing;

    @Before
    public void setUp() throws IOException {
        final File dbFileCopy = folder.newFile("tiergarten.db");

        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(dbFile), dbFileCopy);

        database = Database.loadFromFile(dbFileCopy);

        routing = new GraphHopperRouting();
        routing.loadGraphFromDatabase(database);
    }

    @Test
    public void findPaths_27537749_to_252864802() {
        Node startNode = database.getNode("27537749");
        Node endNode = database.getNode("252864802");
        List<CandidateRoute> result = routing
                .findRoutes(new RoutingRequest(new RoutingPosition(startNode.getPosition()), new RoutingPosition(endNode.getPosition()), new RoutingParameters()));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Arrays.asList("27537749", "252864801", "265786533", "252864802"), result.get(0).getNodeIdList());
        assertValidRoute(result.get(0));
    }

    @Test
    public void generatePaths_twoConnectionsWithSameStartEndNode() {
        Node startNode = database.getNode("21487169");
        Node endNode = database.getNode("415838100");

        List<CandidateRoute> result = routing.findRoutes(
                new RoutingRequest(new RoutingPosition(startNode.getPosition())
                        , new RoutingPosition(endNode.getPosition())
                        , new RoutingParameters().alternativeRoutes(0))

        );
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Arrays.asList("21487169", "281787660", "281787659", "27011219", "282745683", "271749621", "341364267", "27011311", "21677261", "21668930", "27537748", "27537747", "26704447", "423839225", "408194196", "410846037", "408194217", "408194192", "415838099", "428788320", "411091943", "290001020", "415838100"), result.get(0).getNodeIdList());
        assertValidRoute(result.get(0));
    }

    private void assertValidRoute(CandidateRoute candidateRoute) {
        Node lastNode = null, currentNode;
        Connection currentConnection;
        for (String nodeId : candidateRoute.getNodeIdList()) {
            currentNode = database.getNode(nodeId);

            if (lastNode != null) {
                currentConnection = null;
                List<Connection> checkConnections = (!lastNode.getOutgoingConnections().isEmpty()) ? lastNode.getOutgoingConnections() : lastNode.getPartOfConnections();
                for (Connection connection : checkConnections) {
                    int index = connection.getNodes().indexOf(lastNode);
                    if (connection.getNodes().get(index + 1).equals(currentNode)) {
                        if (currentConnection != null && !currentConnection.getOutgoingConnections().contains(connection)) {
                            throw new AssertionError("Route is not valid, no transition existing");
                        }
                        currentConnection = connection;
                        break;
                    }
                }
                if (currentConnection == null) {
                    throw new AssertionError("Route is not valid.");
                }
            }
            lastNode = currentNode;
        }
    }

}
