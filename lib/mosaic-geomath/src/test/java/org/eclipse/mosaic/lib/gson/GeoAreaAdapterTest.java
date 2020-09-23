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

package org.eclipse.mosaic.lib.gson;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import org.eclipse.mosaic.lib.geo.GeoArea;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoPolygon;
import org.eclipse.mosaic.lib.geo.GeoRectangle;

import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import org.junit.Test;

public class GeoAreaAdapterTest {

    @Test
    public void circleWithoutTypeFromJson() {
        GeoCircle circle = new Gson().fromJson("{center:{latitude:52,longitude:13},radius:300}", GeoCircle.class);
        assertNotNull(circle);
        assertEquals(300, circle.getRadius(), 0.00001d);
    }


    @Test
    public void circleFromJson() {
        CTest cTest = new Gson().fromJson("{area:{type:\"circle\", center:{latitude:52,longitude:13},radius:300}}", CTest.class);
        assertTrue(cTest.area instanceof GeoCircle);
    }

    @Test
    public void rectangleFromJson() {
        CTest cTest = new Gson().fromJson("{area:{type:\"rectangle\", a:{latitude:52,longitude:13},b:{latitude:53,longitude:14}}}", CTest.class);
        assertTrue(cTest.area instanceof GeoRectangle);
    }

    @Test
    public void polygonFromJson() {
        CTest cTest = new Gson().fromJson("{area:{type:\"polygon\", vertices:[{latitude:52,longitude:13},{latitude:53,longitude:14},{latitude:52,longitude:13}]}}", CTest.class);
        assertTrue(cTest.area instanceof GeoPolygon);
    }

    @Test
    public void circleToJson() {
        CTest cTest = new CTest();
        cTest.area = new GeoCircle(GeoPoint.latLon(52, 13), 300);
        String json = new Gson().toJson(cTest);
        assertEquals("{\"area\":{\"type\":\"circle\",\"center\":{\"latitude\":52.0,\"longitude\":13.0},\"radius\":300.0}}", json);
    }

    @Test
    public void rectangleToJson() {
        CTest cTest = new CTest();
        cTest.area = new GeoRectangle(GeoPoint.latLon(52, 13), GeoPoint.latLon(53, 14));
        String json = new Gson().toJson(cTest);
        assertEquals("{\"area\":{\"type\":\"rectangle\",\"a\":{\"latitude\":52.0,\"longitude\":13.0},\"b\":{\"latitude\":53.0,\"longitude\":14.0}}}", json);
    }

    @Test
    public void polygonToJson() {
        CTest cTest = new CTest();
        cTest.area = new GeoPolygon(GeoPoint.latLon(52, 13), GeoPoint.latLon(53, 14));
        String json = new Gson().toJson(cTest);
        assertEquals("{\"area\":{\"type\":\"polygon\",\"vertices\":[{\"latitude\":52.0,\"longitude\":13.0},{\"latitude\":53.0,\"longitude\":14.0},{\"latitude\":52.0,\"longitude\":13.0}]}}", json);
    }

    static class CTest {

        @JsonAdapter(GeoAreaAdapterFactory.class)
        GeoArea area;
    }

}