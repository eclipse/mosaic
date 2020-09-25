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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.junit.Test;

/**
 * This tests basic functionality of {@link Restriction}
 */
public class RestrictionTest {

    /**
     * Test of getId method, of class Restriction.
     */
    @Test
    public void testSimpleGetter() {
        // prepare restriction
        Way sourceWay = new Way("1", "source way", "primary");
        Node viaNode = new Node("1", GeoPoint.lonLat(0, 0));
        Way targetWay = new Way("2", "target way", "residential");

        // create restriction
        Restriction instance = new Restriction("1", Restriction.Type.Not, sourceWay, viaNode, targetWay);

        // test basic getters
        assertEquals("Wrong restriction id", "1", instance.getId());
        assertEquals("Wrong restriction type", Restriction.Type.Not, instance.getType());
        assertEquals("Wrong restriction source way", sourceWay, instance.getSource());
        assertEquals("Wrong restriction via node", viaNode, instance.getVia());
        assertEquals("Wrong restriction target way", targetWay, instance.getTarget());
    }

    /**
     * Test of applyRestriction method, of class Restriction.
     * 4
     * |^
     * 1-2-3
     * -->
     */
    @Test
    public void testApplyRestriction() {
        // prepare testcase
        Node startNode = new Node("1", GeoPoint.lonLat(0, 0));
        Node viaNode = new Node("2", GeoPoint.lonLat(0, 0));
        Node endNode1 = new Node("3", GeoPoint.lonLat(0, 0));
        Node endNode2 = new Node("4", GeoPoint.lonLat(0, 0));

        // These ways are supposed to be oneway.
        // As this value is not important for the test however it is not explicitly set.
        Way sourceWay = new Way("1", "source way", "primary");
        Way targetWay = new Way("2", "target way", "primary");
        Way alternWay = new Way("3", "alternative way", "primary");

        // reference the nodes to the ways (just to be safe)
        sourceWay.addNode(startNode);
        sourceWay.addNode(viaNode);
        targetWay.addNode(viaNode);
        targetWay.addNode(endNode1);
        alternWay.addNode(viaNode);
        alternWay.addNode(endNode2);

        // Backreferencing ways from nodes. As the backreference is not needed in all nodes
        // they are left out in those. These affected nodes are startNode and both endNodes.
        viaNode.addWay(sourceWay);
        viaNode.addWay(targetWay);
        viaNode.addWay(alternWay);

        // and last but not least creating the connections
        Connection sourceConnection = new Connection("1", sourceWay);
        sourceConnection.addNode(startNode);
        sourceConnection.addNode(viaNode);
        Connection targetConnection = new Connection("2", targetWay);
        targetConnection.addNode(viaNode);
        targetConnection.addNode(endNode1);
        Connection alternConnection = new Connection("3", alternWay);
        alternConnection.addNode(viaNode);
        alternConnection.addNode(endNode2);

        // reference/backreference the nodes
        viaNode.addConnection(sourceConnection);
        viaNode.addConnection(targetConnection);
        viaNode.addConnection(alternConnection);

        // create outgoing connections for source
        sourceConnection.addOutgoingConnection(targetConnection);
        sourceConnection.addOutgoingConnection(alternConnection);

        // now test restriction for type Restriction.Type.Only
        Restriction restriction = new Restriction("1", Restriction.Type.Only, sourceWay, viaNode, targetWay);
        restriction.applyRestriction();
        assertTrue("Source connection doesn't have the target connection on the list of outgoing connections", sourceConnection.getOutgoingConnections().contains(targetConnection));
        assertFalse("Source connection shouldn't have the alternative connection on the list of outgoing connections!", sourceConnection.getOutgoingConnections().contains(alternConnection));

        // reset outgoing connections and test for type Not
        sourceConnection.addOutgoingConnection(targetConnection);
        sourceConnection.addOutgoingConnection(alternConnection);
        restriction = new Restriction("1", Restriction.Type.Not, sourceWay, viaNode, targetWay);
        restriction.applyRestriction();
        assertTrue("Source connection doesn't have the alternative connection on the list of outgoing connections", sourceConnection.getOutgoingConnections().contains(alternConnection));
        assertFalse("Source connection shouldn't have the target connection on the list of outgoing connections!", sourceConnection.getOutgoingConnections().contains(targetConnection));
    }
}
