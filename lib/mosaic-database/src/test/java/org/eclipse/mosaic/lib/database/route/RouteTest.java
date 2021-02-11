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
    public void testConnections() {
        Route route = createTestRoute();

        // test path
        assertEquals("Wrong edge amount on the route", 2, route.getConnections().size());
        assertEquals("Wrong edge amount on the route", 2, route.getConnectionIds().size());

        assertEquals("1_1_2", route.getConnections().get(0).getId());
        assertEquals("1_1_2", route.getConnectionIds().get(0));

        assertEquals("1_2_4", route.getConnections().get(1).getId());
        assertEquals("1_2_4", route.getConnectionIds().get(1));
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
        assertEquals("Wrong node IDs amount on the route", 4, nodeIdList.size());
        assertEquals("Wrong node amount on the route", 4, nodeList.size());

        // now check contents
        assertEquals("Wrong first node ID on the route", "1", nodeIdList.get(0));
        assertEquals("Wrong second node ID on the route", "2", nodeIdList.get(1));
        assertEquals("Wrong third node ID on the route", "3", nodeIdList.get(2));
        assertEquals("Wrong fourth node ID on the route", "4", nodeIdList.get(3));
        assertEquals("Wrong first node on the route", "1", nodeList.get(0).getId());
        assertEquals("Wrong second node on the route", "2", nodeList.get(1).getId());
        assertEquals("Wrong third node on the route", "3", nodeList.get(2).getId());
        assertEquals("Wrong fourth node on the route", "4", nodeList.get(3).getId());
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
        Node midNode2 = new Node("3", GeoPoint.lonLat(0, 0));
        Node endNode = new Node("4", GeoPoint.lonLat(0, 0));
        way.addNode(startNode);
        way.addNode(midNode);
        way.addNode(midNode2);
        way.addNode(endNode);

        Connection connection1 = new Connection("1_1_2", way);
        connection1.addNode(startNode);
        connection1.addNode(midNode);

        Connection connection2 = new Connection("1_2_4", way);
        connection2.addNode(midNode);
        connection2.addNode(midNode2);
        connection2.addNode(endNode);

        // now create the route to test along side a path
        Route route = new Route("1");
        route.addConnection(connection1);
        route.addConnection(connection1); //add a second time, should not be added twice
        route.addConnection(connection2);
        route.addConnection(connection2); //add a second time, should not be added twice
        return route;
    }
}
