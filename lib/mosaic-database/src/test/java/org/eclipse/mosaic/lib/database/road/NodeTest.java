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

package org.eclipse.mosaic.lib.database.road;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.junit.Test;

/**
 * This tests the basic {@link Node} functionality. There shouldn't be to many surprises here
 * except the connection handling (automatic detection of connection type).
 */
public class NodeTest {

    /**
     * Test of all the simple getter (without any functionality).
     */
    @Test
    public void testSimpleGetter() {
        // constructor & getter
        Node node = new Node("1", GeoPoint.lonLat(1.0, 2.0));
        assertEquals("1", node.getId());
        assertEquals(1.0, node.getPosition().getLongitude(), 0.0d);
        assertEquals(2.0, node.getPosition().getLatitude(), 0.0d);

        // intersections
        node.setIntersection(true);
        assertTrue(node.isIntersection());
        node.setIntersection(false);
        assertFalse(node.isIntersection());

        // ways
        node.addWay(new Way("1", "testway", "primary"));
        assertEquals("Ways amount doesn't match the expected one", 1, node.getWays().size());
        assertEquals("Way ID doesn't match the expected one", "1", node.getWays().get(0).getId());
    }


    /**
     * Test of the methods {@link Node#getIncomingConnections()}, {@link Node#getPartOfConnections()}
     * and {@link Node#getOutgoingConnections()}. The test scenario is as follows
     * (numbers are nodes, lines are connections):
     * <pre>
     *   1-2-3
     * </pre>
     */
    @Test
    public void testConnection() {
        Node node1 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node2 = new Node("2", GeoPoint.lonLat(0, 0));
        Node node3 = new Node("3", GeoPoint.lonLat(0, 0));

        Connection con = new Connection("1", new Way("1", "test", "primary"));
        con.addNode(node1);
        con.addNode(node2);
        con.addNode(node3);
        node1.addConnection(con);
        node2.addConnection(con);
        node3.addConnection(con);

        assertEquals("Incoming connections amount of node 1 doesn't match the expected one", 0, node1.getIncomingConnections().size());
        assertEquals("Part connections amount of node 1 doesn't match the expected one", 0, node1.getPartOfConnections().size());
        assertEquals("Outgoing connections amount of node 1 doesn't match the expected one", 1, node1.getOutgoingConnections().size());

        assertEquals("Incoming connections amount of node 2 doesn't match the expected one", 0, node2.getIncomingConnections().size());
        assertEquals("Part connections amount of node 2 doesn't match the expected one", 1, node2.getPartOfConnections().size());
        assertEquals("Outgoing connections amount of node 2 doesn't match the expected one", 0, node2.getOutgoingConnections().size());

        assertEquals("Incoming connections amount of node 3 doesn't match the expected one", 1, node3.getIncomingConnections().size());
        assertEquals("Part connections amount of node 3 doesn't match the expected one", 0, node3.getPartOfConnections().size());
        assertEquals("Outgoing connections amount of node 3 doesn't match the expected one", 0, node3.getOutgoingConnections().size());
    }

    @Test
    public void equalsTestEqual() throws Exception {
        Node node1 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node2 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node3 = new Node("3", GeoPoint.lonLat(0, 0));

        Connection con = new Connection("1", new Way("1", "test", "primary"));
        con.addNode(node3);
        con.addNode(node1);
        con.addNode(node2);
        con.addNode(node3);
        node1.addConnection(con);
        node2.addConnection(con);

        assertTrue(node1.equals(node2));

    }

    @Test
    public void equalsTestNotEqual() throws Exception {
        Node node1 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node2 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node3 = new Node("3", GeoPoint.lonLat(0, 0));

        Connection con = new Connection("1", new Way("1", "test", "primary"));
        con.addNode(node3);
        con.addNode(node1);
        con.addNode(node2);
        con.addNode(node3);
        node1.addConnection(con);

        assertFalse(node1.equals(node2));

    }

    @Test
    public void hashCodeTestEqualsHashForSameObject() throws Exception {
        Node node1 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node2 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node3 = new Node("3", GeoPoint.lonLat(0, 0));

        Connection con = new Connection("1", new Way("1", "test", "primary"));
        con.addNode(node3);
        con.addNode(node1);
        con.addNode(node2);
        con.addNode(node3);
        node1.addConnection(con);

        int hash1 = node1.hashCode();
        int hash2 = node1.hashCode();
        assertTrue(hash1 == hash2);
    }

    @Test
    public void hashCodeTestNotEqualsHashForDifferentObjects() throws Exception {
        Node node1 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node2 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node3 = new Node("3", GeoPoint.lonLat(0, 0));

        Connection con = new Connection("1", new Way("1", "test", "primary"));
        con.addNode(node3);
        con.addNode(node1);
        con.addNode(node2);
        con.addNode(node3);
        node1.addConnection(con);

        int hash1 = node1.hashCode();
        int hash2 = node3.hashCode();
        assertEquals(true, hash1 != hash2);
        assertFalse(hash1 == hash2);
    }

}
