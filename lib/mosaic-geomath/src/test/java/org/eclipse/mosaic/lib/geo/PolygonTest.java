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
        assertTrue(polygon.contains(xy(5, 6)));

        assertFalse(polygon.contains(xy(2, 6)));
        assertFalse(polygon.contains(xy(8, 6)));
        assertFalse(polygon.contains(xy(1, 3)));
        assertFalse(polygon.contains(xy(13, 8)));
        assertFalse(polygon.contains(xy(9, 16)));
    }

}