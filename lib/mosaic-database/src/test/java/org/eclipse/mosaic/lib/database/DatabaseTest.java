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

package org.eclipse.mosaic.lib.database;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoRectangle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * This checks the database class for correct function.
 */
public class DatabaseTest {

    /**
     * This is the old butzbach test scenario. Not changed so convert as well as outdated tests
     * should be possible.
     */
    private File outdatedVersion;

    /**
     * This basic database contains a simple test scenario that looks like this:
     * <pre>
     * Nodes:    | Ways:     | Connections
     *     1     |     +     |          +++
     *     |     |     1     |      1_1_2 ^
     * 4 - 2 - 3 | + 2 + 3 + |         \/ 1_2_1
     *           |           | +        +++ <3_3_2 +
     *           |           | + 2_4_2> +++ 3_2_3> +
     *
     * Restriction: 2_4_2 only to 1_2_1
     * Route: 2_4_2 -> 1_2_1 -> 1_1_2 -> 3_2_3 -> 3_3_2
     * </pre>
     */
    private File basicDatabase;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        outdatedVersion = folder.newFile("outdatedVersion.db");
        try (InputStream in = this.getClass().getResourceAsStream("/butzbach_outdated.db")) {
            Files.copy(in, outdatedVersion.toPath(), REPLACE_EXISTING);
        }

        basicDatabase = folder.newFile("basicTest.db");
        try (InputStream in = this.getClass().getResourceAsStream("/basicTest.db")) {
            Files.copy(in, basicDatabase.toPath(), REPLACE_EXISTING);
        }
    }

    /**
     * If the database detects an outdated version a {@link RuntimeException} is thrown.
     */
    @Test(expected = RuntimeException.class)
    public void checkOutdatedDatabase() {
        // note: this should throw a RuntimeException, since it's
        Database.Builder.loadFromFile(outdatedVersion);
    }

    /**
     * This test loads a simple database and checks if the expected values are present in the
     * loaded representation.
     */
    @Test
    public void testLoadFromFile() {
        Database database = Database.loadFromFile(basicDatabase);

        // first check simple counts
        assertEquals("Wrong amount of nodes in the database", 4, database.getNodes().size());
        assertEquals("Wrong amount of ways in the database", 3, database.getWays().size());
        assertEquals("Wrong amount of connections in the database", 5, database.getConnections().size());
        assertEquals("Wrong amount of restrictions in the database", 1, database.getRestrictions().size());
        assertEquals("Wrong amount of routes in the database", 1, database.getRoutes().size());

        // now check consistency
        Node node = database.getNode("2");
        Way way = database.getWay("2");
        Connection connection = database.getConnection("2_4_2");

        // do the values we just pulled exist?
        assertNotNull("mid-node doesn't exist", node);
        assertNotNull("oneway way doesn't exist", way);
        assertNotNull("connection with restriction doesn't exist", connection);

        // is the content existent with the known values?
        assertEquals("Wrong amount of ways on mid-node", 3, node.getWays().size());
        assertEquals("Wrong amount of incoming connections on mid-node", 3, node.getIncomingConnections().size());
        assertEquals("Wrong amount of outgoing connections on mid-node", 2, node.getOutgoingConnections().size());
        assertTrue("Way isn't oneway", way.isOneway());
        assertEquals("Connection with restriction has more than one outgoing connection", 1, connection.getOutgoingConnections().size());

        // make sure that the outgoing entries of connections without restrictions also work
        connection = database.getConnection("1_1_2");
        assertEquals("Connection without restriction has to have outgoing connections", 2, connection.getOutgoingConnections().size());
    }

    /**
     * Test of detectIntersections method, of class Database.
     * The test scenario is as follows
     * (numbers are nodes, double-lines (=) are overlapping ways [1 to 7 and 2 to 6]):
     * 2   6
     * |   |
     * 1-3=4=5-7-8-9
     * <p></p>
     * way1: 1-3-4-5-7
     * way2: 2-3-4-5-6 (overlaps with way1)
     * way3: 7-8-9     (extends way1)
     */
    @Test
    public void testDetectIntersections() {
        // the version doesn't really matter for this test
        Database.Builder dbBuilder = new Database.Builder();

        // create all nodes;
        Node node1 = dbBuilder.addNode("1", GeoPoint.lonLat(0, 0));
        Node node2 = dbBuilder.addNode("2", GeoPoint.lonLat(0, 0));
        Node node3 = dbBuilder.addNode("3", GeoPoint.lonLat(0, 0));
        Node node4 = dbBuilder.addNode("4", GeoPoint.lonLat(0, 0));
        Node node5 = dbBuilder.addNode("5", GeoPoint.lonLat(0, 0));
        Node node6 = dbBuilder.addNode("6", GeoPoint.lonLat(0, 0));
        Node node7 = dbBuilder.addNode("7", GeoPoint.lonLat(0, 0));
        Node node8 = dbBuilder.addNode("8", GeoPoint.lonLat(0, 0));
        Node node9 = dbBuilder.addNode("9", GeoPoint.lonLat(0, 0));

        Database db = dbBuilder.build(false);
        // create way 1 with back references from nodes
        Way way1 = new Way("1", "straight street", "primary");

        way1.addNode(node1);
        way1.addNode(node3);
        way1.addNode(node4);
        way1.addNode(node5);
        way1.addNode(node7);

        node1.addWay(way1);
        node3.addWay(way1);
        node4.addWay(way1);
        node5.addWay(way1);
        node7.addWay(way1);

        // create way 2 with back references from nodes
        Way way2 = new Way("2", "u street", "primary");

        way2.addNode(node2);
        way2.addNode(node3);
        way2.addNode(node4);
        way2.addNode(node5);
        way2.addNode(node6);

        node2.addWay(way2);
        node3.addWay(way2);
        node4.addWay(way2);
        node5.addWay(way2);
        node6.addWay(way2);

        Way way3 = new Way("3", "tail street", "primary");

        way3.addNode(node7);
        way3.addNode(node8);
        way3.addNode(node9);

        node7.addWay(way3);
        node8.addWay(way3);
        node9.addWay(way3);

        // finally add ways
        dbBuilder.addWay(way1);
        dbBuilder.addWay(way2);
        dbBuilder.addWay(way3);

        //RUN
        dbBuilder.calculateIntersections();

        //ASSERT
        for (String id : new String[]{"3", "5"}) {
            assertTrue("Node " + id + " should be an intersection", db.getNode(id).isIntersection());
        }

        for (String id : new String[]{"1", "2", "4", "6", "7", "8", "9"}) {
            assertFalse("Node " + id + " should NOT be an intersection", db.getNode(id).isIntersection());
        }
    }

    /**
     * Bounds and center position are necessary for correct UTMZone detection, so we need to test
     * if it is determined correctly.
     */
    @Test
    public void testBoundaryCreationPositivePositive() {
        // create test database and add nodes
        Database.Builder dbBuilder = new Database.Builder();
        dbBuilder.addNode("1", GeoPoint.lonLat(50.5, 13.5));
        dbBuilder.addNode("2", GeoPoint.lonLat(50.0, 13.0));
        dbBuilder.addNode("3", GeoPoint.lonLat(51.0, 14.0));

        Database db = dbBuilder.build();
        // get generated values
        GeoRectangle boundingBox = db.getBoundingBox();
        GeoPoint center = boundingBox.getCenter();

        // check generated values against expected output
        assertEquals("longitude of minimum bounds isn't correct", 50.0, boundingBox.getA().getLongitude(), 0.01);
        assertEquals("longitude of maximum bounds isn't correct", 51.0, boundingBox.getB().getLongitude(), 0.01);
        assertEquals("longitude of center bounds isn't correct", 50.5, center.getLongitude(), 0.01);
        assertEquals("latitude of minimum bounds isn't correct", 13.0, boundingBox.getA().getLatitude(), 0.01);
        assertEquals("latitude of maximum bounds isn't correct", 14.0, boundingBox.getB().getLatitude(), 0.01);
        assertEquals("latitude of center bounds isn't correct", 13.5, center.getLatitude(), 0.01);
    }

    @Test
    public void testBoundaryCreationPositiveNegative() {
        // create test database and add nodes
        Database.Builder dbBuilder = new Database.Builder();
        dbBuilder.addNode("1", GeoPoint.lonLat(50.5, -13.5));
        dbBuilder.addNode("2", GeoPoint.lonLat(50.0, -13.0));
        dbBuilder.addNode("3", GeoPoint.lonLat(51.0, -14.0));

        Database db = dbBuilder.build();
        // get generated values
        GeoRectangle boundingBox = db.getBoundingBox();
        GeoPoint center = boundingBox.getCenter();

        // check generated values against expected output
        assertEquals("longitude of minimum bounds isn't correct", 50.0, boundingBox.getA().getLongitude(), 0.01);
        assertEquals("longitude of maximum bounds isn't correct", 51.0, boundingBox.getB().getLongitude(), 0.01);
        assertEquals("longitude of center bounds isn't correct", 50.5, center.getLongitude(), 0.01);
        assertEquals("latitude of minimum bounds isn't correct", -14.0, boundingBox.getA().getLatitude(), 0.01);
        assertEquals("latitude of maximum bounds isn't correct", -13.0, boundingBox.getB().getLatitude(), 0.01);
        assertEquals("latitude of center bounds isn't correct", -13.5, center.getLatitude(), 0.01);
    }

    @Test
    public void testBoundaryCreationNegativePositive() {
        // create test database and add nodes
        Database.Builder dbBuilder = new Database.Builder();
        dbBuilder.addNode("1", GeoPoint.lonLat(-50.5, 13.5));
        dbBuilder.addNode("2", GeoPoint.lonLat(-50.0, 13.0));
        dbBuilder.addNode("3", GeoPoint.lonLat(-51.0, 14.0));

        Database db = dbBuilder.build();
        // get generated values
        GeoRectangle boundingBox = db.getBoundingBox();
        GeoPoint center = boundingBox.getCenter();

        // check generated values against expected output
        assertEquals("longitude of minimum bounds isn't correct", -51.0, boundingBox.getA().getLongitude(), 0.01);
        assertEquals("longitude of maximum bounds isn't correct", -50.0, boundingBox.getB().getLongitude(), 0.01);
        assertEquals("longitude of center bounds isn't correct", -50.5, center.getLongitude(), 0.01);
        assertEquals("latitude of minimum bounds isn't correct", 13.0, boundingBox.getA().getLatitude(), 0.01);
        assertEquals("latitude of maximum bounds isn't correct", 14.0, boundingBox.getB().getLatitude(), 0.01);
        assertEquals("latitude of center bounds isn't correct", 13.5, center.getLatitude(), 0.01);
    }

    @Test
    public void testBoundaryCreationNegativeNegative() {
        // create test database and add nodes
        Database.Builder dbBuilder = new Database.Builder();
        dbBuilder.addNode("1", GeoPoint.lonLat(-50.5, -13.5));
        dbBuilder.addNode("2", GeoPoint.lonLat(-50.0, -13.0));
        dbBuilder.addNode("3", GeoPoint.lonLat(-51.0, -14.0));

        Database db = dbBuilder.build();
        // get generated values
        GeoRectangle boundingBox = db.getBoundingBox();
        GeoPoint center = boundingBox.getCenter();

        // check generated values against expected output
        assertEquals("longitude of minimum bounds isn't correct", -51.0, boundingBox.getA().getLongitude(), 0.01);
        assertEquals("longitude of maximum bounds isn't correct", -50.0, boundingBox.getB().getLongitude(), 0.01);
        assertEquals("longitude of center bounds isn't correct", -50.5, center.getLongitude(), 0.01);
        assertEquals("latitude of minimum bounds isn't correct", -14.0, boundingBox.getA().getLatitude(), 0.01);
        assertEquals("latitude of maximum bounds isn't correct", -13.0, boundingBox.getB().getLatitude(), 0.01);
        assertEquals("latitude of center bounds isn't correct", -13.5, center.getLatitude(), 0.01);
    }
}
