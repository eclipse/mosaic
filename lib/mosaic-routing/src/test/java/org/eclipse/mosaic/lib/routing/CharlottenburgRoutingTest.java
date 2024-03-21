/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.routing.graphhopper.GraphHopperRouting;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Test routing with a real world map (Charlottenburg extract from BeST scenario).
 */
public class CharlottenburgRoutingTest {

    @Rule
    public GeoProjectionRule transformationRule = new GeoProjectionRule(GeoPoint.latLon(52, 13));

    private final static String dbFile = "/charlottenburg.db";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Database database;
    private GraphHopperRouting routing;

    @Before
    public void setUp() throws IOException {
        final File dbFileCopy = folder.newFile("charlottenburg.db");

        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(dbFile), dbFileCopy);

        database = Database.loadFromFile(dbFileCopy);

        routing = new GraphHopperRouting(database);
    }

    @Test
    public void findPaths_27537749_to_252864802() {
        List<CandidateRoute> result = routing
                .findRoutes(new RoutingRequest(
                        new RoutingPosition(GeoPoint.latLon(52.504185, 13.323964), null, "-440300111"),
                        new RoutingPosition(database.getNode("26761203").getPosition()),
                        new RoutingParameters()
                                .vehicleClass(VehicleClass.Car)
                                .alternativeRoutes(1)
                                .costFunction(RoutingCostFunction.Fastest)
                ));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertValidRoute(result.get(0));
        assertValidRoute(result.get(1));
        assertEquals(Lists.newArrayList("-440300111", "4381160#2", "832017303", "823947542#0", "110008909#2", "547450789#0", "4402682#8", "4490390#2", "-25418285#1", "4500153"),
                result.get(0).getConnectionIds());
        assertEquals(Lists.newArrayList("-440300111", "4381160#2", "832017303", "823947542#0", "110008909#2", "547450789#0", "490351849#0", "490351848#0", "4492013#2", "25418287#6", "318889281#0", "4371002#2", "-4437136#0"),
                result.get(1).getConnectionIds());
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
