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

package org.eclipse.mosaic.lib.database.spatial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.util.junit.TestFileRule;

import org.junit.Rule;
import org.junit.Test;

public class NodeFinderTest {

    @Rule
    public TestFileRule rule = new TestFileRule()
            .with("tiergarten.db", "/tiergarten.db");

    @Rule
    public GeoProjectionRule projectionRule = new GeoProjectionRule(GeoPoint.latLon(52, 13));

    @Test
    public void findClosestNode() {
        // SETUP
        Database db = Database.loadFromFile(rule.get("tiergarten.db"));
        assertFalse(db.getConnections().isEmpty());

        final NodeFinder nodeFinder = new NodeFinder(db);

        // RUN + ASSERT
        GeoPoint search = GeoPoint.latLon(52.51303, 13.32743);
        Node closest = nodeFinder.findClosestNode(search);
        assertEquals("408194217", closest.getId());

        for (Node node : db.getNodes()) {
            if (node != closest) {
                assertFalse(
                        search.distanceTo(node.getPosition()) < search.distanceTo(closest.getPosition())
                );
            }
        }

    }

}