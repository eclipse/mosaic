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
        assertEquals(Arrays.asList("4609243_27537749_252864801", "4609243_252864801_252864802"), result.get(0).getConnectionIds());
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
        assertEquals(Arrays.asList("4400154_21487169_21677261", "32935480_21677261_21668930", "32935480_21668930_27537748", "4609242_27537748_27537747", "4609241_27537747_26704447", "4609241_26704447_423839225", "4609241_423839225_408194196", "36337926_408194196_408194192", "36337926_408194192_428788320", "36337926_428788320_415838100"), result.get(0).getConnectionIds());
        assertValidRoute(result.get(0));
    }

    private void assertValidRoute(CandidateRoute candidateRoute) {
        Connection currentConnection;
        Connection previousConnection = null;
        for (String connectionId : candidateRoute.getConnectionIds()) {
            currentConnection = database.getConnection(connectionId);
            if (previousConnection != null && !previousConnection.getOutgoingConnections().contains(currentConnection)) {
                throw new AssertionError("Route is not valid, no transition existing");
            }
            previousConnection = currentConnection;
        }
    }

}
