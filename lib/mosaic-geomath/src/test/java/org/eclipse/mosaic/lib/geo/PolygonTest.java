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

package org.eclipse.mosaic.lib.geo;

import static org.eclipse.mosaic.lib.geo.CartesianPoint.xy;
import static org.eclipse.mosaic.lib.geo.GeoPoint.latLon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.junit.GeoProjectionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class PolygonTest {

    private static GeoPoint BERLIN = latLon(52.5, 13.4);

    @Rule
    public TestRule geoProjectionRule = new GeoProjectionRule(BERLIN);

    @Test
    public void conversion() {
        GeoPolygon polygon = new GeoPolygon(
                latLon(52.7, 13.9),
                latLon(52.1, 13.2),
                latLon(52.3, 13.3),
                latLon(52.5, 12.9)
        );

        CartesianPolygon cartesianPolygon = polygon.toCartesian();
        assertEquals(33883.51, cartesianPolygon.getVertices().get(0).getX(), 0.01d);
        assertEquals(22263.89, cartesianPolygon.getVertices().get(0).getY(), 0.01d);

        assertEquals(-13553.40, cartesianPolygon.getVertices().get(1).getX(), 0.01d);
        assertEquals(-44527.80, cartesianPolygon.getVertices().get(1).getY(), 0.01d);

        assertEquals(-6776.70, cartesianPolygon.getVertices().get(2).getX(), 0.01d);
        assertEquals(-22263.90, cartesianPolygon.getVertices().get(2).getY(), 0.01d);

        assertEquals(-33883.51, cartesianPolygon.getVertices().get(3).getX(), 0.01d);
        assertEquals(0.00, cartesianPolygon.getVertices().get(3).getY(), 0.01d);

        assertEquals(33883.51, cartesianPolygon.getVertices().get(4).getX(), 0.01d);
        assertEquals(22263.90, cartesianPolygon.getVertices().get(4).getY(), 0.01d);


        assertEquals(1357.9, polygon.getArea() / 1_000_000, 0.1d);
        assertEquals(1357.9, cartesianPolygon.getArea() / 1_000_000, 0.1d);
    }

    @Test
    public void boundingBox() {
        GeoPolygon polygon = new GeoPolygon(
                latLon(52.7, 13.9),
                latLon(52.1, 13.2),
                latLon(52.3, 13.3),
                latLon(52.5, 12.9)
        );

        Bounds<GeoPoint> boundingBox = polygon.getBounds();
        assertEquals(52.1, boundingBox.getSideA(), 0.01d);
        assertEquals(13.9, boundingBox.getSideB(), 0.01d);
        assertEquals(52.7, boundingBox.getSideC(), 0.01d);
        assertEquals(12.9, boundingBox.getSideD(), 0.01d);
    }

    @Test
    public void areaRectangularPolygon() {
        CartesianPoint a = xy(100, 200);
        CartesianPoint b = xy(300, 200);
        CartesianPoint c = xy(300, 600);
        CartesianPoint d = xy(100, 600);

        CartesianPolygon polygon = new CartesianPolygon(a, b, c, d);

        assertEquals(80, polygon.getArea() / 1000, 0.001d);

        // should match exactly the area of a rectangle
        assertEquals(new CartesianRectangle(a, c).getArea(), polygon.getArea(), 0.1d);
    }

    @Test
    public void areaCircularPolygon() {
        CartesianCircle circle = new CartesianCircle(xy(300, 400), 350);

        CartesianPolygon polygon = circle.toPolygon(1d);

        assertEquals(384.9, polygon.getArea() / 1000, 0.1d);

        // should match exactly the area of the circle
        assertEquals(circle.getArea() / 1000d, polygon.getArea() / 1000, 1d);
    }

    @Test
    public void containsPoints() {
        CartesianPolygon polygon = new CartesianPolygon(
                xy(3, 5),
                xy(7, 7),
                xy(10, 5),
                xy(14, 15),
                xy(5, 13),
                xy(8, 10),
                xy(5, 8)
        );

        assertTrue(polygon.contains(xy(10, 6)));
        assertTrue(polygon.contains(xy(11, 12)));
        assertTrue(polygon.contains(xy(10, 14)));
        assertTrue(polygon.contains(xy(5, 6)));

        assertFalse(polygon.contains(xy(8, 14)));
        assertFalse(polygon.contains(xy(2, 6)));
        assertFalse(polygon.contains(xy(8, 6)));
        assertFalse(polygon.contains(xy(1, 3)));
        assertFalse(polygon.contains(xy(13, 8)));
        assertFalse(polygon.contains(xy(9, 16)));
    }

    @Test
    public void containsPointsRectangle() {
        CartesianPolygon polygon = new CartesianRectangle(
                xy(3, 5),
                xy(7, 1)
        ).toPolygon();

        // Outside x-axis
        assertFalse(polygon.contains(xy(2, 5)));
        assertFalse(polygon.contains(xy(2, 1)));
        assertFalse(polygon.contains(xy(8, 1)));
        assertFalse(polygon.contains(xy(8, 5)));

        // Outside y-axis
        assertFalse(polygon.contains(xy(7, 0)));
        assertFalse(polygon.contains(xy(7, 6)));
        assertFalse(polygon.contains(xy(3, 0)));
        assertFalse(polygon.contains(xy(3, 6)));


        // Inside polygon
        assertTrue(polygon.contains(xy(4, 4)));

        // Polygon vertices
        assertTrue(polygon.contains(xy(3, 5)));
        assertTrue(polygon.contains(xy(3, 1)));
        assertTrue(polygon.contains(xy(7, 5)));
        assertTrue(polygon.contains(xy(7, 1)));

        // Polygon edges
        assertTrue(polygon.contains(xy(6, 1))); // lower limit
        assertTrue(polygon.contains(xy(6, 5))); // upper limit
        assertTrue(polygon.contains(xy(3, 3))); // left limit
        assertTrue(polygon.contains(xy(7, 3))); // right limit
    }

    @Test
    public void intersection() {

        CartesianPolygon polygonA = new CartesianPolygon(
                xy(0, 0),
                xy(0, 1),
                xy(1, 1),
                xy(1, 0)
        );

        CartesianPolygon polygonB = new CartesianPolygon(
                xy(0.1, 0.1),
                xy(0.1, 0.9),
                xy(0.9, 0.9),
                xy(0.9, 0.1)
        );

        CartesianPolygon polygonC = new CartesianPolygon(
                xy(-0.2, 0.5),
                xy(0.5, -0.2),
                xy(1.2, 0.5),
                xy(0.5, 1.2)
        );

        CartesianPolygon polygonD = new CartesianPolygon(
                xy(0, 0),
                xy(0, 1),
                xy(1, 1),
                xy(1, 0)
        );

        CartesianPolygon polygonE = new CartesianPolygon(
                xy(2, 0),
                xy(2, 1),
                xy(1, 1),
                xy(1, 0)
        );

        CartesianPolygon polygonF= new CartesianPolygon(
                xy(2, 0),
                xy(2, 1),
                xy(3, 1),
                xy(3, 0)
        );

        // One polygon is lies completely within another
        assertTrue(polygonA.isIntersectingPolygon(polygonB));
        assertTrue(polygonB.isIntersectingPolygon(polygonA));

        // Edges of the polygons intersect
        assertTrue(polygonA.isIntersectingPolygon(polygonC));

        // Corners of one polygon lie exactly of the edges of the other
        assertTrue(polygonA.isIntersectingPolygon(polygonD));

        // Corners of one polygon lie on the edge of another
        assertTrue(polygonA.isIntersectingPolygon(polygonE));

        // Polygons don't intersect
        assertFalse(polygonA.isIntersectingPolygon(polygonF));
    }
}