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

package org.eclipse.mosaic.lib.routing.graphhopper.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.junit.Test;

public class GraphhopperToDatabaseMapperTest {

    /**
     * Tests, if the mapping functionality works correctly
     */
    @Test
    public void testCoreFunctions() {
        Node n1 = new Node("1", GeoPoint.lonLat(0, 0));
        Node n2 = new Node("1337", GeoPoint.lonLat(0, 0));
        Connection con1 = new Connection("con1", new Way("", "", ""));
        Connection con2 = new Connection("1337", new Way("", "", ""));

        GraphhopperToDatabaseMapper mapper = new GraphhopperToDatabaseMapper();

        //assert if not yet filled
        assertTrue(mapper.fromConnection(con1) < 0);
        assertTrue(mapper.fromConnection(con2) < 0);
        assertTrue(mapper.fromNode(n1) < 0);
        assertTrue(mapper.fromNode(n2) < 0);

        assertNull(mapper.toConnection(1));
        assertNull(mapper.toNode(1));

        //fill
        mapper.setConnection(con1, 101);
        mapper.setConnection(con2, 102);
        mapper.setNode(n1, 1);
        mapper.setNode(n2, 2);

        //assert
        assertEquals(101, mapper.fromConnection(con1), 0d);
        assertEquals(102, mapper.fromConnection(con2), 0d);

        assertEquals(1, mapper.fromNode(n1), 0d);
        assertEquals(2, mapper.fromNode(n2), 0d);

        assertSame(n1, mapper.toNode(1));
        assertSame(n2, mapper.toNode(2));

        assertSame(con1, mapper.toConnection(101));
        assertSame(con2, mapper.toConnection(102));

        assertNull(mapper.toConnection(0));
        assertNull(mapper.toNode(0));
        assertTrue(mapper.fromConnection(new Connection("con2", con1.getWay())) < 0);
        assertTrue(mapper.fromNode(new Node("3", GeoPoint.lonLat(0, 0))) < 0);
    }

}
