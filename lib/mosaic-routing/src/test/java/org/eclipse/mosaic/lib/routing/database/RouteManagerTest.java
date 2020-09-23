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

package org.eclipse.mosaic.lib.routing.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.route.Edge;
import org.eclipse.mosaic.lib.database.route.Route;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.IllegalRouteException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RouteManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final static String dbFile = "/tiergarten.db";

    private RouteManager instance = null;

    private Database database;

    private final String routeIDs = "32909782_26704482_26785753, "
            + "25185001_26785753_26704584, "
            + "25185007_26704584_21487146, "
            + "25185006_21487146_21487168, "
            + "4068038_21487168_251150126, "
            + "4068038_251150126_428788319, "
            + "4068038_428788319_408194194, "
            + "4068038_408194194_423839224, "
            + "4068038_423839224_26704448, "
            + "36337928_26704448_27537750, "
            + "4609244_27537750_27537749, "
            + "4609243_27537749_252864801, "
            + "4609243_252864801_252864802, "
            + "32935479_252864802_21487170, "
            + "30806885_21487170_299080425, "
            + "30194724_299080425_21487175, "
            + "4413638_21487175_21487174, "
            + "4397063_21487174_26873453, "
            + "4397063_26873453_26873454";


    @Before
    public void setUp() throws IOException {
        final File dbFileCopy = folder.newFile("tiergarten.db");

        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(dbFile), dbFileCopy);

        database = Database.loadFromFile(dbFileCopy);

        instance = new RouteManager(database);
    }

    @Test
    public void createRouteByCandidateRoute_routeIsComplete() throws IllegalRouteException {
        CandidateRoute candidateRoute = new CandidateRoute(Arrays.asList("27537749", "252864801", "265786533", "252864802"), 0, 0);

        //RUN
        Route route = instance.createRouteByCandidateRoute(candidateRoute);

        List<String> firstNodesOfConnections = new ArrayList<>();
        for (Edge edge : route.getRoute()) {
            if (edge.getFromNode().equals(edge.getConnection().getFrom())
                    || edge.getFromNode().equals(edge.getConnection().getTo())) {
                firstNodesOfConnections.add(edge.getFromNode().getId());
            }
        }
        assertEquals("1", route.getId());
        assertEquals(Arrays.asList("4609243_27537749_252864801", "4609243_252864801_252864802"), route.getConnectionIdList());
        assertEquals(Arrays.asList("4609243_27537749_252864801_27537749", "4609243_252864801_252864802_252864801", "4609243_252864801_252864802_265786533"), route.getEdgeIdList());

        assertEquals(Arrays.asList("27537749", "252864801"), firstNodesOfConnections);
        assertEquals(candidateRoute.getNodeIdList(), route.getNodeIdList());
    }

    @Test
    public void getRouteForRTI_transformationSuccessful() throws IllegalRouteException {
        CandidateRoute candidateRoute = new CandidateRoute(Arrays.asList("27537749", "252864801", "265786533", "252864802"), 0, 0);
        Route route = instance.createRouteByCandidateRoute(candidateRoute);

        //RUN
        VehicleRoute rtiRoute = instance.createRouteForRTI(route);

        assertEquals("1", rtiRoute.getId());
        assertEquals(Arrays.asList("4609243_27537749_252864801_27537749", "4609243_252864801_252864802_252864801", "4609243_252864801_252864802_265786533"), rtiRoute.getEdgeIdList());
        assertEquals(candidateRoute.getNodeIdList(), rtiRoute.getNodeIdList());
        assertEquals(290.5, rtiRoute.getLength(), 0.1d);
    }

    @Test
    public void getRouteForRTI_transformationSuccessful__twoConnectionsWithSameStartEndNode() throws IllegalRouteException {
        CandidateRoute candidateRoute = new CandidateRoute(Arrays.asList("27011311", "21677261", "21668930", "27537748"), 0, 0);
        Route route = instance.createRouteByCandidateRoute(candidateRoute);

        //RUN
        VehicleRoute rtiRoute = instance.createRouteForRTI(route);

        assertEquals("1", rtiRoute.getId());
        assertEquals(Arrays.asList("4400154_21487169_21677261_27011311", "32935480_21677261_21668930_21677261", "32935480_21668930_27537748_21668930"), rtiRoute.getEdgeIdList());
        assertEquals(candidateRoute.getNodeIdList(), rtiRoute.getNodeIdList());
        assertEquals(1213.4, rtiRoute.getLength(), 0.1d);
    }

    @Test
    public void getRoutesFromDatabaseForMessage() {

        //RUN
        Map<String, VehicleRoute> routesFromDatabaseForMessage = instance.getRoutesFromDatabaseForMessage();

        assertEquals(1, routesFromDatabaseForMessage.size());
        assertNotNull(routesFromDatabaseForMessage.get("0"));
        assertEquals("0", routesFromDatabaseForMessage.get("0").getId());
        assertEquals(4142.9, routesFromDatabaseForMessage.get("0").getLength(), 0.1d);
        assertFalse(routesFromDatabaseForMessage.get("0").getNodeIdList().isEmpty());
        assertFalse(routesFromDatabaseForMessage.get("0").getEdgeIdList().isEmpty());

    }
}
