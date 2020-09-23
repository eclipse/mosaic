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

package org.eclipse.mosaic.lib.spatial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoPolygon;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.math.Vector3d;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class KdTreeTest {

    private static GeoPoint BERLIN = GeoPoint.latLon(52.5, 13.4);

    @Rule
    public TestRule geoProjectionRule = new GeoProjectionRule(BERLIN);

    @Test
    public void emptyTest() {
        KdTree<Vector3d> empty = new KdTree<>(new SpatialItemAdapter.PointAdapter<>(), new ArrayList<>());

        SpatialTreeTraverser.Nearest<Vector3d> nearest = new SpatialTreeTraverser.Nearest<>();
        nearest.setup(new Vector3d(0, 0, 0));
        nearest.traverse(empty);
        Assert.assertNull(nearest.nearest);

        SpatialTreeTraverser.InRadius<Vector3d> inRadius = new SpatialTreeTraverser.InRadius<>();
        inRadius.setup(new Vector3d(0, 0, 0), 1);
        inRadius.traverse(empty);
        assertTrue(inRadius.result.isEmpty());
    }

    @Test
    public void nearestTest() {
        RandomNumberGenerator rand = new DefaultRandomNumberGenerator(1337L);
        List<Vector3d> points = new ArrayList<>();

        Vector3d n = new Vector3d(1, 2, 3);
        points.add(n);
        for (int i = 0; i < 100; i++) {
            points.add(new Vector3d(17 + rand.nextDouble(), 17 + rand.nextDouble(), 17 + rand.nextDouble()));
        }
        KdTree<Vector3d> tree = new KdTree<>(new SpatialItemAdapter.PointAdapter<>(), points);

        SpatialTreeTraverser.Nearest<Vector3d> nearest = new SpatialTreeTraverser.Nearest<>();
        nearest.setup(new Vector3d(5, 0, 0));
        nearest.traverse(tree);
        Assert.assertSame(nearest.nearest, n);
    }

    @Test
    public void inRadiusTest() {
        RandomNumberGenerator rand = new DefaultRandomNumberGenerator(1337L);
        List<Vector3d> points = new ArrayList<>();

        Vector3d n = new Vector3d(1, 2, 3);
        points.add(n);
        for (int i = 0; i < 100; i++) {
            points.add(new Vector3d(17 + rand.nextDouble(), 17 + rand.nextDouble(), 17 + rand.nextDouble()));
        }
        KdTree<Vector3d> tree = new KdTree<>(new SpatialItemAdapter.PointAdapter<>(), points);

        SpatialTreeTraverser.InRadius<Vector3d> inRadius = new SpatialTreeTraverser.InRadius<>();
        inRadius.setup(new Vector3d(0, 0, 0), 5);
        inRadius.traverse(tree);
        assertEquals(1, inRadius.result.size());
        Assert.assertSame(inRadius.result.get(0), n);

        inRadius.setup(new Vector3d(0, 0, 0), 50);
        inRadius.traverse(tree);
        assertEquals(101, inRadius.result.size());
    }


    @Test
    public void searchInPolygons() throws Exception {
        List<PolygonWithId> polygons = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/brandenburg_polygons.csv")))) {
            reader.lines().forEach(line -> {
                String[] fields = line.split(";");
                List<GeoPoint> vertices = new ArrayList<>();
                for (int i = 1; i < fields.length; i += 2) {
                    vertices.add(GeoPoint.latLon(Double.parseDouble(fields[i]), Double.parseDouble(fields[i + 1])));
                }
                polygons.add(new PolygonWithId(fields[0], vertices));
            });
        }

        final KdTree<PolygonWithId> tree = new KdTree<>(new SpatialItemAdapter.AreaAdapter<>(), polygons);
        final SpatialTreeTraverser.InGeoPolygon<PolygonWithId> nearest = new SpatialTreeTraverser.InGeoPolygon<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/brandenburg_assert1k.csv")))) {
            reader.lines().forEach(line -> {
                String[] fields = line.split(";");
                GeoPoint search = GeoPoint.latLon(Double.parseDouble(fields[0]), Double.parseDouble(fields[1]));

                nearest.setup(search);
                nearest.traverse(tree);

                String expected = fields[2];
                assertNotNull(nearest.getNearest());
                assertTrue(nearest.getNearest().contains(search));
                assertEquals(expected, nearest.getNearest().id);
            });
        }
    }

    static class PolygonWithId extends GeoPolygon {

        private final String id;

        PolygonWithId(String id, List<GeoPoint> vertices) {
            super(vertices);
            this.id = id;
        }
    }
}
