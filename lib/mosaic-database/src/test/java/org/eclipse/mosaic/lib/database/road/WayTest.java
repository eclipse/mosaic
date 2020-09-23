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

import org.junit.Before;
import org.junit.Test;

/**
 * This tests the basic way functionality. There shouldn't be any functional surprises here.
 */
public class WayTest {

    private Way way;

    /**
     * We use the same basic way for all tests.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        way = new Way("1", "testway", "primary");
    }

    /**
     * Test of all the simple getter (without any functionality)
     */
    @Test
    public void testSimpleGetter() {
        assertEquals("Wrong way id", "1", way.getId());
        assertEquals("Wrong way name", "testway", way.getName());
        assertEquals("Wrong way type", "primary", way.getType());
    }

    /**
     * Test the automatic unit conversion for set speed.
     */
    @Test
    public void testSpeed() {
        way.setMaxSpeedInKmh(50);
        assertEquals("Wrong max speed km/h", 50, way.getMaxSpeedInKmh(), 0.0d);
        assertEquals("Wrong max speed m/s", 13.88888d, way.getMaxSpeedInMs(), 0.00001d);
    }

    /**
     * Test of lanes specific functionality.
     */
    @Test
    public void testGetLanes() {
        way.setLanes(3, 2);
        assertEquals("Wrong lanes forward amount", 3, way.getNumberOfLanesForward());
        assertEquals("Wrong lanes backward amount", 2, way.getNumberOfLanesBackward());

        way.setIsOneway(true);
        assertTrue("Way is not a one-way street as should be", way.isOneway());
        assertEquals("Wrong lanes backward amount", 0, way.getNumberOfLanesBackward());
        way.setIsOneway(false);
        assertFalse("Way is a one-way street although it shouldn't be", way.isOneway());
        assertEquals("Wrong lanes backward amount", 2, way.getNumberOfLanesBackward());
    }

    /**
     * Test of node specific functionality.
     */
    @Test
    public void testNodes() {
        way.addNode(new Node("1", GeoPoint.lonLat(0, 0)));
        way.addNode(new Node("2", GeoPoint.lonLat(0, 0)));
        assertEquals("Wrong amount of nodes on the way", 2, way.getNodes().size());
        assertEquals("Wrong node id on the ways node list at #1", "1", way.getNodes().get(0).getId());
        assertEquals("Wrong node id on the ways node list at #2", "2", way.getNodes().get(1).getId());
    }

    /**
     * Test connection specific functionality
     */
    @Test
    public void testConnections() {
        way.addConnection(new Connection("1", way));
        way.addConnection(new Connection("2", way));
        assertEquals("Wrong connections amount", 2, way.getConnections().size());
        assertEquals("Wrong connection id on the ways node list at #1", "1", way.getConnections().get(0).getId());
        assertEquals("Wrong connection id on the ways node list at #2", "2", way.getConnections().get(1).getId());
    }

}
