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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
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

    @Before
    public void setUp() throws IOException {
        final File dbFileCopy = folder.newFile("tiergarten.db");

        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(dbFile), dbFileCopy);

        database = spy(Database.loadFromFile(dbFileCopy));

        instance = new RouteManager(database);
    }

    @Test
    public void createRouteByCandidateRoute_routeIsComplete() throws IllegalRouteException {
        CandidateRoute candidateRoute = new CandidateRoute(Arrays.asList("4609243_27537749_252864801", "4609243_252864801_252864802"), 0, 0);

        //RUN
        Route route = instance.createRouteByCandidateRoute(candidateRoute);

        List<String> firstNodesOfConnections = new ArrayList<>();
        for (Connection connection : route.getConnections()) {
            firstNodesOfConnections.add(connection.getFrom().getId());
        }
        assertEquals("1", route.getId());
        assertEquals(Arrays.asList("4609243_27537749_252864801", "4609243_252864801_252864802"), route.getConnectionIds());

        assertEquals(Arrays.asList("27537749", "252864801"), firstNodesOfConnections);
        assertEquals(candidateRoute.getConnectionIds(), route.getConnectionIds());
    }

    @Test
    public void getRouteForRTI_transformationSuccessful() throws IllegalRouteException {
        CandidateRoute candidateRoute = new CandidateRoute(Arrays.asList("4609243_27537749_252864801", "4609243_252864801_252864802"), 0, 0);
        Route route = instance.createRouteByCandidateRoute(candidateRoute);

        //RUN
        VehicleRoute rtiRoute = instance.createRouteForRTI(route);

        assertEquals("1", rtiRoute.getId());
        assertEquals(Arrays.asList("4609243_27537749_252864801", "4609243_252864801_252864802"), rtiRoute.getConnectionIds());
        assertEquals(candidateRoute.getConnectionIds(), rtiRoute.getConnectionIds());
        assertEquals(290.5, rtiRoute.getLength(), 0.1d);
    }

    @Test
    public void getRouteForRTI_transformationSuccessful__twoConnectionsWithSameStartEndNode() throws IllegalRouteException {
        CandidateRoute candidateRoute = new CandidateRoute(Arrays.asList("32935480_21677261_21668930", "32935480_21668930_27537748"), 0, 0);
        Route route = instance.createRouteByCandidateRoute(candidateRoute);

        //RUN
        VehicleRoute rtiRoute = instance.createRouteForRTI(route);

        assertEquals("1", rtiRoute.getId());
        assertEquals(Arrays.asList("32935480_21677261_21668930", "32935480_21668930_27537748"), rtiRoute.getConnectionIds());
        assertEquals(candidateRoute.getConnectionIds(), rtiRoute.getConnectionIds());
        assertEquals(291.7, rtiRoute.getLength(), 0.1d);
    }

    @Test
    public void getRouteForRTI_originSumo() throws IllegalRouteException {
        // SETUP
        // override behavior of getImportOrigin to simulate import origin from network file
        doReturn(Database.IMPORT_ORIGIN_SUMO).when(database).getImportOrigin();

        CandidateRoute candidateRoute = new CandidateRoute(Arrays.asList("32935480_21677261_21668930", "32935480_21668930_27537748"), 0, 0);
        Route route = instance.createRouteByCandidateRoute(candidateRoute);

        //RUN
        VehicleRoute rtiRoute = instance.createRouteForRTI(route);

        assertEquals("1", rtiRoute.getId());
        assertEquals(Arrays.asList("32935480_21677261_21668930", "32935480_21668930_27537748"), rtiRoute.getConnectionIds());
        assertEquals(candidateRoute.getConnectionIds(), rtiRoute.getConnectionIds());
        assertEquals(291.7, rtiRoute.getLength(), 0.1d);
    }

    @Test
    public void getRoutesFromDatabaseForMessage() {

        //RUN
        Map<String, VehicleRoute> routesFromDatabaseForMessage = instance.getRoutesFromDatabaseForMessage();

        assertEquals(1, routesFromDatabaseForMessage.size());
        assertNotNull(routesFromDatabaseForMessage.get("0"));
        assertEquals("0", routesFromDatabaseForMessage.get("0").getId());
        assertEquals(4142.9, routesFromDatabaseForMessage.get("0").getLength(), 0.1d);
        assertFalse(routesFromDatabaseForMessage.get("0").getNodeIds().isEmpty());
        assertFalse(routesFromDatabaseForMessage.get("0").getConnectionIds().isEmpty());

    }
}
