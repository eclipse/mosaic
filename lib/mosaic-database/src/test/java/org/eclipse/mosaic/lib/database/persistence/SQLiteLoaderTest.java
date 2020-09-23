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

package org.eclipse.mosaic.lib.database.persistence;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Way;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Tests the loader class for SQLite Databases.
 */
public class SQLiteLoaderTest {

    private final String butzbachDB = "/butzbach.db";

    private File databaseCopy;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Before
    public void setUp() throws Exception {
        // create a temporary copy
        databaseCopy = folder.newFile("test.db");

        try (InputStream in = this.getClass().getResourceAsStream(butzbachDB)) {
            Files.copy(in, databaseCopy.toPath(), REPLACE_EXISTING);
        }
    }

    /**
     * Test of loadFromFile method, of class SQLiteLoader.
     */
    @Test
    public void testLoadFromFile() throws OutdatedDatabaseException {
        testLoadFromFileWithPath(databaseCopy.getAbsolutePath());
    }

    private void testLoadFromFileWithPath(String path) throws OutdatedDatabaseException {
        SQLiteLoader instance = new SQLiteLoader();
        Database result = instance.loadFromFile(path).build();

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
}
