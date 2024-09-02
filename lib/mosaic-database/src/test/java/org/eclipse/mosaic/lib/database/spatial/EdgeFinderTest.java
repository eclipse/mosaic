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
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.util.junit.TestFileRule;

import com.google.common.collect.Iterables;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class EdgeFinderTest {

    @Rule
    public TestFileRule rule = new TestFileRule()
            .with("tiergarten.db", "/tiergarten.db");

    @Rule
    public TestFileRule rule2 = new TestFileRule().with("edgeFinderTest.db", "/edgeFinderTest.db");

    @Rule
    public GeoProjectionRule projectionRule = new GeoProjectionRule(GeoPoint.latLon(52, 13));

    @Test
    public void findClosestEdge() {
        // SETUP
        Database db = Database.loadFromFile(rule.get("tiergarten.db"));
        assertFalse(db.getConnections().isEmpty());

        final EdgeFinder edgeFinder = new EdgeFinder(db);

        // RUN
        Edge edge = Iterables.getOnlyElement(edgeFinder.findClosestEdge(GeoPoint.latLon(52.51303, 13.32743)));

        // ASSERT
        assertEquals("36337926_408194196_408194192", edge.getConnection().getId());
        assertEquals("410846037", edge.getPreviousNode().getId());

    }

    @Test
    public void findClosestEdge2() {
        // SETUP
        Database db = Database.loadFromFile(rule2.get("edgeFinderTest.db"));
        assertFalse(db.getConnections().isEmpty());

        final EdgeFinder edgeFinder = new EdgeFinder(db);

        // RUN
        List<Edge> edgesWest = edgeFinder.findClosestEdge(GeoPoint.latLon(0.0, 10.510506));
        List<Edge> edgesEast = edgeFinder.findClosestEdge(GeoPoint.latLon(0.0, 10.511805));
        Edge edgeWest = Iterables.getOnlyElement(edgesWest);
        Edge edgeEast = Iterables.getOnlyElement(edgesEast);
        // ASSERT
        assertEquals("E0", edgeEast.getConnection().getId());
        assertEquals("-E0", edgeWest.getConnection().getId());
    }

}