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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.IllegalRouteException;
import org.eclipse.mosaic.lib.routing.RoutingCostFunction;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingRequest;
import org.eclipse.mosaic.lib.routing.RoutingResponse;
import org.eclipse.mosaic.lib.routing.config.CRouting;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DatabaseRoutingTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public GeoProjectionRule transformationRule = new GeoProjectionRule(GeoPoint.latLon(52, 13));

    private final static String dbFile = "/tiergarten.db";

    private DatabaseRouting routingAPIScenarioDatabase;
    private CRouting configuration;

    private File cfgDir;

    @Mock
    Database dbMock;

    @Before
    public void setup() throws IOException {
        cfgDir = folder.newFolder("database");
        final File dbFileCopy = folder.newFile("database/tiergarten.db");

        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(dbFile), dbFileCopy);

        configuration = new CRouting();
        routingAPIScenarioDatabase = new DatabaseRouting();

    }

    @Test
    public void initialize_seekDatabase() throws InternalFederateException {
        //PREPARE
        configuration.source = null;

        //RUN
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);

        //ASSERT
        assertEquals(1, routingAPIScenarioDatabase.getRoutesFromDatabaseForMessage().size());
    }

    @Test
    public void initialize_locateDatabase() throws InternalFederateException {
        //PREPARE
        configuration.source = "tiergarten.db";

        //RUN
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);

        //ASSERT
        assertEquals(1, routingAPIScenarioDatabase.getRoutesFromDatabaseForMessage().size());
    }

    @Test(expected = InternalFederateException.class)
    public void initialize_locateDatabase_error() throws InternalFederateException {
        //PREPARE
        configuration.source = "tiergarten_not_found.db";

        //RUN -> Throw error
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);
    }

    @Test
    public void getRoutesFromDatabaseForMessage_routesCorrectlyLoaded() throws InternalFederateException {
        //PREPARE
        configuration.source = "tiergarten.db";
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);
        final String routeID = "0";

        //RUN
        Map<String, VehicleRoute> routes = routingAPIScenarioDatabase.getRoutesFromDatabaseForMessage();

        //ASSERT
        assertEquals(1, routes.size());
        assertNotNull(routes.get(routeID));
        assertEquals(Arrays.asList("26704482", "26938219", "26938220", "26785753", "26785752", "21487147", "26704584", "26938208", "26938209",
                "21487146", "281787666", "281787664", "21487168", "26938204", "251150126", "21487167", "272365223", "428788319", "272256206",
                "408194194", "26738489", "313006383", "423839224", "26704448", "27537750", "27537749", "252864801", "265786533", "252864802",
                "341364279", "248919692", "82654385", "27011308", "341364277", "341364270", "27011305", "540312558", "281787656", "281787657",
                "21487170", "27011237", "27423744", "27011241", "299080425", "26703663", "299080426", "21487176", "21487175", "197687090",
                "342813080", "27011231", "21487174", "27011842", "27011256", "26873451", "406585016", "414959615", "82654384", "564738832",
                "249734328", "26873453", "152533555", "417709064", "391498256", "26873454"),
                routes.get(routeID).getNodeIds());
    }

    @Test
    public void getMaxSpeedOfConnection() throws InternalFederateException {
        //PREPARE
        configuration.source = "tiergarten.db";
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);

        //RUN
        double speed = routingAPIScenarioDatabase.getMaxSpeedOfConnection("32909782_26704482_26785753");

        //ASSERT
        assertEquals(22.222, speed, 0.001d);
    }

    @Test
    public void getMaxSpeedOfConnection_noSuchConnection_noException() throws InternalFederateException {
        //PREPARE
        configuration.source = "tiergarten.db";
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);

        //RUN
        double speed = routingAPIScenarioDatabase.getMaxSpeedOfConnection("32909782_26704482_0");

        //ASSERT
        assertEquals(0, speed, 0.001d);
    }

    @Test
    public void getPositionOfNode() throws InternalFederateException {
        //PREPARE
        configuration.source = "tiergarten.db";
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);

        //RUN
        GeoPoint gp = routingAPIScenarioDatabase.getNode("21487171").getPosition();

        //ASSERT
        assertEquals(52.515, gp.getLatitude(), 0.001d);
        assertEquals(13.349, gp.getLongitude(), 0.001d);
    }

    @Test
    public void createRouteForRTI() throws InternalFederateException, IllegalRouteException {
        //PREPARE
        configuration.source = "tiergarten.db";
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);

        final CandidateRoute candidateRoute = new CandidateRoute(
                Arrays.asList("4068038_423839224_26704448", "36337928_26704448_27537750", "4609244_27537750_27537749", "4609243_27537749_252864801", "4609243_252864801_252864802"), 0, 0);
        //RUN
        final VehicleRoute route = routingAPIScenarioDatabase.createRouteForRTI(candidateRoute);

        //ASSERT
        assertEquals(376.4d, route.getLength(), 0.1d);
        assertEquals(candidateRoute.getConnectionIds(), route.getConnectionIds());
        assertEquals(Arrays.asList("4068038_423839224_26704448", "36337928_26704448_27537750",
                "4609244_27537750_27537749", "4609243_27537749_252864801", "4609243_252864801_252864802"),
                route.getConnectionIds());
    }

    @Test(expected = IllegalRouteException.class)
    public void createRouteForRTI_falseCandidateRoute() throws InternalFederateException, IllegalRouteException {
        //PREPARE
        configuration.source = "tiergarten.db";
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);

        final CandidateRoute candidateRoute = new CandidateRoute(
                Arrays.asList("4068038_423839224_26704448", "36337928_26704448_27537750", "4609244_27537750_27537749", /* "4609243_27537749_252864801", */ "4609243_252864801_252864802"), 0, 0);
        //RUN (throw error)
        routingAPIScenarioDatabase.createRouteForRTI(candidateRoute);
    }

    @Test
    public void findRoutes() throws InternalFederateException {
        //PREPARE
        configuration.source = "tiergarten.db";
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);

        final RoutingParameters routingParameters = new RoutingParameters().alternativeRoutes(0).costFunction(RoutingCostFunction.Shortest);
        final GeoPoint start = routingAPIScenarioDatabase.getNode("26704482").getPosition();
        final GeoPoint target = routingAPIScenarioDatabase.getNode("26704584").getPosition();
        final RoutingRequest request = new RoutingRequest(new RoutingPosition(start), new RoutingPosition(target), routingParameters);

        //RUN 
        final RoutingResponse response = routingAPIScenarioDatabase.findRoutes(request);

        //ASSERT
        assertEquals(0, response.getAlternativeRoutes().size());
        assertNotNull(response.getBestRoute());
        assertEquals(15.672, response.getBestRoute().getTime(), 0.1d);
        assertEquals(304.74, response.getBestRoute().getLength(), 0.1d);
        assertEquals(Arrays.asList("32909782_26704482_26785753", "25185001_26785753_26704584"),
                response.getBestRoute().getConnectionIds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPositionOfNode_noSuchNode() throws InternalFederateException {
        //PREPARE
        configuration.source = "tiergarten.db";
        routingAPIScenarioDatabase.initialize(configuration, cfgDir);

        //RUN -> throw error
        routingAPIScenarioDatabase.getNode("1234").getPosition();
    }

    @Test
    public void testApproximateCostsForCandidateRoute() throws Exception {

        Node node0 = new Node("0", GeoPoint.lonLat(0.0, 0.0)); //node on route but vehicle already passed it
        Node node1 = new Node("1", GeoPoint.lonLat(1.0, 1.0));
        Node node2 = new Node("2", GeoPoint.lonLat(2.0, 2.0)); //node on Route but no intersection
        Node node3 = new Node("3", GeoPoint.lonLat(3.0, 3.0));
        Node node4 = new Node("4", GeoPoint.lonLat(4.0, 4.0));
        Node node5 = new Node("5", GeoPoint.lonLat(5.0, 5.0)); //node on Route but no intersection

        Way someWay = new Way("someID", "someName", "someType");
        someWay.setMaxSpeedInMs(2);

        Connection node0ToNode1 = new Connection("0-1", someWay);
        node0ToNode1.setLength(1000);
        node0ToNode1.addNode(node0);
        node0ToNode1.addNode(node1);

        Connection node1ToNode3 = new Connection("1-3", someWay);
        node1ToNode3.setLength(2.0);
        node1ToNode3.addNode(node1);
        node1ToNode3.addNode(node3);


        Connection node3ToNode4 = new Connection("3-4", someWay);
        node3ToNode4.setLength(8.0);
        node3ToNode4.addNode(node3);
        node3ToNode4.addNode(node4);

        node1.addConnection(node1ToNode3);
        node3.addConnection(node1ToNode3);
        node3.addConnection(node3ToNode4);
        node4.addConnection(node3ToNode4);

        DatabaseRouting spyRSDB = Mockito.spy(routingAPIScenarioDatabase);
        Mockito.doReturn(dbMock).when(spyRSDB).getScenarioDatabase();

        Mockito.when(dbMock.getConnection("0-1")).thenReturn(node0ToNode1);
        Mockito.when(dbMock.getConnection("1-3")).thenReturn(node1ToNode3);
        Mockito.when(dbMock.getConnection("3-4")).thenReturn(node3ToNode4);

        List<String> connectionIds = Arrays.asList("0-1", "1-3", "3-4");
        CandidateRoute candidateRoute = new CandidateRoute(connectionIds, 0.0, 0.0);

        CandidateRoute approximatedCandidateRoute = spyRSDB.approximateCostsForCandidateRoute(candidateRoute, "1");
        assertEquals(1010, approximatedCandidateRoute.getLength(), 0);
        assertEquals(505, approximatedCandidateRoute.getTime(), 0);
    }

}
