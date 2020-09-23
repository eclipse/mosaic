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


import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.junit.GeoProjectionRule;

import org.junit.Rule;
import org.junit.Test;

public class RectangleTest {

    private static GeoPoint BERLIN = GeoPoint.latLon(52.5, 13.4);

    @Rule
    public GeoProjectionRule geoProjectionRule = new GeoProjectionRule(BERLIN);

    @Test
    public void area() {
        CartesianPoint a = GeoPoint.latLon(52.7, 13.2).toCartesian();
        CartesianPoint b = CartesianPoint.xy(a.getX() + 100, a.getY() + 500);

        GeoRectangle geoRectangle = new GeoRectangle(a.toGeo(), b.toGeo());
        assertEquals(49.8, geoRectangle.getArea() / 1000d, 0.1d);

        CartesianRectangle cartesianRectangle = geoRectangle.toCartesian();
        assertEquals(50.0, cartesianRectangle.getArea() / 1000d, 0.1);
    }

    @Test
    public void center() {
        CartesianPoint a = GeoPoint.latLon(52.7, 13.2).toCartesian();
        CartesianPoint b = CartesianPoint.xy(a.getX() + 100, a.getY() + 500);

        GeoRectangle geoRectangle = new GeoRectangle(a.toGeo(), b.toGeo());
        GeoPoint centerPoint = geoRectangle.getCenter();

        assertEquals(52.702246, centerPoint.getLatitude(), 0.0001d);
        assertEquals(13.200738, centerPoint.getLongitude(), 0.0001d);
    }

}