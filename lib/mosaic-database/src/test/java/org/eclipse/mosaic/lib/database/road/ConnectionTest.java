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

package org.eclipse.mosaic.lib.database.road;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ConnectionTest {


    /**
     * This tests if there is an exception when a connection without a way is created.
     */
    @Test(expected = RuntimeException.class)
    public void testRuntimeException() {
        Connection con = new Connection("1", null);
    }

    /**
     * Test of getId method, of class Connection.
     */
    @Test
    public void testGetSimpleGetter() {
        // prepare test object
        Node nodeFrom = new Node("321", GeoPoint.lonLat(0, 0));
        Node nodeTo = new Node("231", GeoPoint.lonLat(0, 0));
        Way way = new Way("123", "test street", "primary");
        way.setMaxSpeedInKmh(50);
        way.setLanes(2, 1);

        // create test object
        Connection instance = new Connection("123", way);
        instance.addNode(nodeFrom);
        instance.addNode(nodeTo);
        instance.setLength(20);

        // test defaults
        assertEquals("Connection id doesn't match the expected one", "123", instance.getId());
        assertEquals("Connection way doesn't match the expected one", way, instance.getWay());
        assertEquals("Connection start node doesn't match the expected one", nodeFrom, instance.getFrom());
        assertEquals("Connection end node doesn't match the expected one", nodeTo, instance.getTo());
        assertEquals("Connection length doesn't match the expected one", 20d, instance.getLength(), 0d);
        assertEquals("Connection max in km/h speed doesn't match the expected one", 50d, instance.getMaxSpeedInKmh(), 0d);
        assertEquals("Connection max in m/s speed doesn't match the expected one", 13.888d, instance.getMaxSpeedInMs(), 0.1d);
        assertEquals("Number of lanes in a connection doesn't match the expected one", 2, instance.getLanes());

        // test if backward is also read correctly
        instance = new Connection("123", way, true);
        assertEquals("Number of lanes in a connection doesn't match the expected one", 1, instance.getLanes());
    }

    /**
     * Test of getNodes method, of class Connection.
     */
    @Test
    public void testNodes() {
        Connection instance = new Connection("1", new Way("1", "teststreet", "primary"));
        instance.addNode(new Node("1", GeoPoint.lonLat(0, 0)));
        instance.addNode(new Node("2", GeoPoint.lonLat(0, 0)));
        instance.addNode(new Node("3", GeoPoint.lonLat(0, 0)));
        List<Node> nodes = instance.getNodes();

        assertEquals("Number of nodes doesn't match the expected one", 3, nodes.size());
        assertEquals("Node order isn't correct #1", "1", nodes.get(0).getId());
        assertEquals("Node order isn't correct #2", "2", nodes.get(1).getId());
        assertEquals("Node order isn't correct #3", "3", nodes.get(2).getId());

        boolean exceptionThrown = false;
        try {
            nodes.add(new Node("4", GeoPoint.lonLat(0, 0)));
        } catch (UnsupportedOperationException uoe) {
            exceptionThrown = true;
        }

        assertTrue("An UnsupportedOperationException wasn't thrown when we tried to modify unmodifiable list", exceptionThrown);
    }

    /**
     * Test of addNodes method, of class Connection.
     */
    @Test
    public void testAddNodes() {
        // prepare list to add
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node("3", GeoPoint.lonLat(0, 0)));
        nodes.add(new Node("2", GeoPoint.lonLat(0, 0)));
        nodes.add(new Node("1", GeoPoint.lonLat(0, 0)));

        // create connection and add
        Connection instance = new Connection("1", new Way("1", "teststraße", "primary"));
        instance.addNodes(nodes);

        // check if order is correct before changing collection
        assertEquals("Number of nodes in the list after adding them wasn't correct", 3, instance.getNodes().size());
        assertEquals("Node order wasn't correct after adding nodes at #1", "3", instance.getNodes().get(0).getId());
        assertEquals("Node order wasn't correct after adding nodes at #2", "2", instance.getNodes().get(1).getId());
        assertEquals("Node order wasn't correct after adding nodes at #3", "1", instance.getNodes().get(2).getId());

        // reverse order and recheck if it still is the order from before!
        Collections.reverse(nodes);
        assertEquals("Number of nodes in the list after adding them wasn't correct", 3, instance.getNodes().size());
        assertEquals("Node order wasn't correct after adding nodes at #1", "3", instance.getNodes().get(0).getId());
        assertEquals("Node order wasn't correct after adding nodes at #2", "2", instance.getNodes().get(1).getId());
        assertEquals("Node order wasn't correct after adding nodes at #3", "1", instance.getNodes().get(2).getId());
    }


    /**
     * Test of getOutgoingConnections method, of class Connection.
     */
    @Test
    public void testOutgoingConnections() {
        // prepare connections
        Node junctionNode = new Node("1", GeoPoint.lonLat(0, 0));
        Connection conn1 = new Connection("1", new Way("1", "teststraße", "primary"));
        conn1.addNode(junctionNode);
        conn1.addNode(new Node("2", GeoPoint.lonLat(0, 0)));
        Connection conn2 = new Connection("2", new Way("2", "teststraße", "primary"));
        conn2.addNode(junctionNode);
        conn2.addNode(new Node("3", GeoPoint.lonLat(0, 0)));

        // create test object
        Connection instance = new Connection("3", new Way("3", "teststraße", "primary"));
        instance.addNode(new Node("4", GeoPoint.lonLat(0, 0)));
        instance.addNode(junctionNode);
        instance.addOutgoingConnection(conn1);
        instance.addOutgoingConnection(conn2);

        // check if outgoing connections where build correctly
        Collection<Connection> result = instance.getOutgoingConnections();
        assertEquals("outgoing connections size doesn't match the expected one", 2, result.size());
        assertTrue("outgoing connection #1 is not on the list", result.contains(conn1));
        assertTrue("outgoing connection #2 is not on the list", result.contains(conn2));
    }

    /**
     * Test if turn restrictions are properly applied.
     */
    @Test
    public void testApplyTurnRestriction() {
        // prepare test
        Node junctionNode = new Node("1", GeoPoint.lonLat(0, 0));
        Connection conn1 = new Connection("1", new Way("1", "test", "primary"));
        conn1.addNode(junctionNode);
        conn1.addNode(new Node("2", GeoPoint.lonLat(0, 0)));
        Connection conn2 = new Connection("2", new Way("2", "test", "primary"));
        conn2.addNode(junctionNode);
        conn2.addNode(new Node("3", GeoPoint.lonLat(0, 0)));

        // test 'only'
        Connection instance = new Connection("3", new Way("3", "test", "primary"));
        instance.addNode(new Node("4", GeoPoint.lonLat(0, 0)));
        instance.addNode(junctionNode);
        instance.addOutgoingConnection(conn1);
        instance.addOutgoingConnection(conn2);
        instance.applyTurnRestriction(Restriction.Type.Only, conn2);

        assertEquals("# outgoing connections after applying restrictions wasn't correct", 1, instance.getOutgoingConnections().size());
        boolean hasId = false;
        for (Connection connection : instance.getOutgoingConnections()) {
            if (connection.getId().equals("2")) {
                assertEquals("Wrong outgoing connection object is on the list after applying restrictions", conn2, connection);
                hasId = true;
                break;
            }
        }
        assertTrue("Outgoing connection id wasn't correct", hasId);

        // test 'not'
        instance = new Connection("3", new Way("3", "test", "primary"));
        instance.addNode(new Node("4", GeoPoint.lonLat(0, 0)));
        instance.addNode(junctionNode);
        instance.addOutgoingConnection(conn1);
        instance.addOutgoingConnection(conn2);
        instance.applyTurnRestriction(Restriction.Type.Not, conn2);

        assertEquals("# outgoing connections after applying restrictions wasn't correct", 1, instance.getOutgoingConnections().size());
        hasId = false;
        for (Connection connection : instance.getOutgoingConnections()) {
            if (connection.getId().equals("1")) {
                assertEquals("Wrong outgoing connection object is on the list after applying restrictions", conn1, connection);
                hasId = true;
                break;
            }
        }
        assertTrue("Outgoing connection id wasn't correct", hasId);
    }
}
