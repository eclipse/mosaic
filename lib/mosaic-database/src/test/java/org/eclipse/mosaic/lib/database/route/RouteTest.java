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

package org.eclipse.mosaic.lib.database.route;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.junit.Test;

import java.util.List;

/**
 * This tests functionality of {@link Route} for users of the class (internal operations are not tested).
 */
public class RouteTest {

    /**
     * Test of getId method, of class Route.
     */
    @Test
    public void testSimpleGetter() {
        // now create the route to test along side a path
        Route route = new Route("1");

        assertEquals("id", "1", route.getId());
    }

    /**
     * Test of addEdge method, of class Route.
     */
    @Test
    public void testEdges() {
        Route route = createTestRoute();

        // test path
        assertEquals("Wrong edge amount on the route", 2, route.getEdges().size());

        Edge edge = route.getEdges().get(0);
        assertEquals("Wrong edge 1 connection id", "1", edge.getConnection().getId());
        assertEquals("Wrong edge 1 start node id", "1", edge.getFromNode().getId());
        assertEquals("Wrong edge 1 end node id", "2", edge.getToNode().getId());

        edge = route.getEdges().get(1);
        assertEquals("Wrong edge 2 connection id", "1", edge.getConnection().getId());
        assertEquals("Wrong edge 2 start node id", "2", edge.getFromNode().getId());
        assertEquals("Wrong edge 2 end node id", "3", edge.getToNode().getId());

        assertEquals("Route string representation wasn't generated correctly", "1_1 1_2", String.join(" ", route.getEdgeIds()));
    }


    /**
     * Test of getNodeX methods.
     */
    @Test
    public void testNodes() {
        Route route = createTestRoute();

        // test
        List<String> nodeIdList = route.getNodeIds();
        List<Node> nodeList = route.getNodes();

        // check counts
        assertEquals("Wrong node IDs amount on the route", 3, nodeIdList.size());
        assertEquals("Wrong node amount on the route", 3, nodeList.size());

        // now check contents
        assertEquals("Wrong first node ID on the route", "1", nodeIdList.get(0));
        assertEquals("Wrong second node ID on the route", "2", nodeIdList.get(1));
        assertEquals("Wrong third node ID on the route", "3", nodeIdList.get(2));
        assertEquals("Wrong first node on the route", "1", nodeList.get(0).getId());
        assertEquals("Wrong second node on the route", "2", nodeList.get(1).getId());
        assertEquals("Wrong third node on the route", "3", nodeList.get(2).getId());
    }

    /**
     * This creates a test route. It is very simple and contains only 1 way/connection
     * being composed of 2 edges. Here is a little representation of it:<br />
     * <br />
     * <pre>
     * {@code
     * Node Structure:  | Way Structure: | Connection Structure: | Edge Structure:
     *   1-2-3          |   + 1 + 1 +    |   + 1_1_3> + 1_1_3> + |   + 1_1_3_1> + 1_1_3_2> +
     * }
     * </pre>
     * legend:
     * <ul>
     * <li>numbers depend on structures: nodes for first structure, etc.</li>
     * <li>= are connections in both directions</li>
     * <li>| is a connection in one direction</li>
     * <li>at each node can be turned in every direction (to every connection)</li>
     * <li>&lt; and &gt; in connection structure represent driving direction</li>
     * </ul>
     *
     * @return
     */
    protected Route createTestRoute() {
        // create basis
        Way way = new Way("1", "test", "primary");
        way.setIsOneway(true);
        Node startNode = new Node("1", GeoPoint.lonLat(0, 0));
        Node midNode = new Node("2", GeoPoint.lonLat(0, 0));
        Node endNode = new Node("3", GeoPoint.lonLat(0, 0));
        way.addNode(startNode);
        way.addNode(midNode);
        way.addNode(endNode);
        Connection connection = new Connection("1", way);
        connection.addNode(startNode);
        connection.addNode(midNode);
        connection.addNode(endNode);

        // now create the route to test along side a path
        Route route = new Route("1");
        route.addEdge(new Edge(connection, startNode, midNode));
        route.addEdge(new Edge(connection, midNode, endNode));
        return route;
    }
}
