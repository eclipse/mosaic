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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.geo.UtmZone;

import com.google.gson.Gson;
import org.junit.Test;

public class UtmZoneAdapterTest {

    @Test
    public void fromJson() {
        CTest cTest = new Gson().fromJson("{position:{northing:52,easting:13,zone:\"33n\"}}", CTest.class);
        assertEquals(UtmPoint.northEast(UtmZone.from("33n"), 52, 13), cTest.position);
    }

    @Test
    public void fromJsonNull() {
        CTest cTest = new Gson().fromJson("{}", CTest.class);
        assertNull(cTest.position);
    }

    @Test
    public void toJson() {
        CTest cTest = new CTest();
        cTest.position = UtmPoint.northEast(UtmZone.from("33n"), 52, 13);

        String json = new Gson().toJson(cTest);
        assertEquals("{\"position\":{\"easting\":13.0,\"northing\":52.0,\"zone\":\"33n\"}}", json);
    }

    @Test
    public void toJsonNull() {
        CTest cTest = new CTest();

        String json = new Gson().toJson(cTest);
        assertEquals("{}", json);
    }


    static class CTest {
        UtmPoint position;
    }
}