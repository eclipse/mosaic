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

package org.eclipse.mosaic.lib.database.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.building.Building;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.util.junit.TestFileRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the loader class for SQLite Databases.
 */
public class SQLiteLoaderTest {

    @Rule
    public TestFileRule testFileRule = new TestFileRule()
            .with("/butzbach.db")
            .with("/kaiserdammPristine.db");

    @Test
    public void testLoadFromFile() throws OutdatedDatabaseException {
        // SETUP
        String path = testFileRule.get("butzbach.db").getAbsolutePath();

        // RUN
        Database result = new SQLiteLoader().loadFromFile(path).build();

        // check counts
        assertEquals("Wrong nodes amount in the database", 25, result.getNodes().size());
        assertEquals("Wrong ways amount in the database", 13, result.getWays().size());
        assertEquals("Wrong connections amount in the database", 46, result.getConnections().size());
        assertEquals("Wrong routes amount in the database", 3, result.getRoutes().size());
        assertEquals("Wrong roundabouts amount in the database", 1, result.getRoundabouts().size());

        // check consistency for nodes
        Node node = result.getNode("264506252");
        assertEquals("Wrong ways amount for node", 2, node.getWays().size());
        assertEquals("Wrong amount of incoming connections for node 264506252", 1, node.getIncomingConnections().size());
        assertEquals("Wrong amount of \"part of\" connections for node 264506252", 0, node.getPartOfConnections().size());
        assertEquals("Wrong amount of outgoing connections for node 264506252", 1, node.getOutgoingConnections().size());

        // ... consistency for ways
        Way way = result.getWay("1024378029");
        assertEquals("Wrong amount of nodes for way 1024378029", 7, way.getNodes().size());
        assertTrue("Way 1024378029 doesn't contain node 264506252", way.getNodes().contains(node));
        assertEquals("Wrong amount of connections for way 1024378029", 6, way.getConnections().size());

        // ... consistency for connections
        Connection connection = result.getConnection("24421700_265306279_265306324");
        assertEquals("Wrong amount of nodes for connection 24421700_265306279_265306324", 3, connection.getNodes().size());
        assertTrue("Outgoing connections list of start node don't contain expected connection", connection.getFrom().getOutgoingConnections().contains(connection));
        Node currNode = connection.getNodes().get(1);
        assertTrue("Incoming connections of in between nodes should be empty!", currNode.getIncomingConnections().isEmpty());
        assertTrue("\"Part of\" connections of an in-between-node don't contain expected connection", currNode.getPartOfConnections().contains(connection));
        assertTrue("Outgoing connections of in between nodes should be empty!", currNode.getOutgoingConnections().isEmpty());
        assertTrue("Incoming connections of end node don't contain expected connection", connection.getTo().getIncomingConnections().contains(connection));
    }

    @Test
    public void testLoadBuildings() throws OutdatedDatabaseException {
        // SETUP
        String path = testFileRule.get("kaiserdammPristine.db").getAbsolutePath();

        // RUN
        Database result = new SQLiteLoader().loadFromFile(path).build();
        assertEquals("Wrong amount of buildings in the database", 100, result.getBuildings().size());

        Building building = result.getBuilding("82504793");
        assertEquals("Wrong amount of walls for building 82504793", 7, building.getWalls().size());
        assertSame("Last corner must be first corner.",
                building.getWalls().get(0).getFromCorner(),
                building.getWalls().get(6).getToCorner()
        );
    }
}
