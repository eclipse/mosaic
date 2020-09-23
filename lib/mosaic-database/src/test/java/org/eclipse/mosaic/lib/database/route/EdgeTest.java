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

package org.eclipse.mosaic.lib.database.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.junit.Test;

/**
 * Testing basic functionality of {@link Edge}.
 */
public class EdgeTest {

    /**
     * Test of getConnection method, of class Edge.
     */
    @Test
    public void testSimpleGetter() {
        // set up test usage
        Node node1 = new Node("1", GeoPoint.lonLat(0, 0));
        Node node2 = new Node("2", GeoPoint.lonLat(0, 0));
        // attention: in real usage, way, from and to may NOT be null!!!
        Connection connection = new Connection("1", new Way("1", "test", "primary"));

        // create edge
        Edge instance = new Edge(connection, node1, node2);

        // and check
        assertEquals("Wrong edge id from a constructed edge", connection.getId() + "_" + node1.getId(), instance.getId());
        assertEquals("Wrong edge connection", connection, instance.getConnection());
        assertEquals("Wrong edge start node", node1, instance.getFromNode());
        assertEquals("Wrong edge end node", node2, instance.getToNode());
    }

    /**
     * Tests the static methods which creates or analyzes the edge IDs.
     */
    @Test
    public void testIdMethods() {
        // prepare test
        Node fromNode = new Node("1", GeoPoint.lonLat(0, 0));
        Node toNode = new Node("2", GeoPoint.lonLat(1, 1));
        Way way = new Way("1", "teststreet", "primary");
        Connection conn = new Connection("1_1_2", way);
        way.addNode(fromNode);
        way.addNode(toNode);
        conn.addNode(fromNode);
        conn.addNode(toNode);

        // execute test cases and save results for later analysis
        final String id = Edge.createEdgeId(conn, fromNode);
        final boolean generatedConform = Edge.isScenarioCompatible(id);
        final String connectionId = Edge.getConnectionIdFromId(id);
        final String fromNodeId = Edge.getFromNodeIdFromId(id);

        assertEquals("created edge ID doesn't match the expected one", "1_1_2_1", id);
        assertTrue("created edge ID isn't recognized as conform", generatedConform);
        assertEquals("extracted edge connection id doesn't match the expected one", "1_1_2", connectionId);
        assertEquals("extracted edge start node id doesn't match the expected one", "1", fromNodeId);
    }
}
