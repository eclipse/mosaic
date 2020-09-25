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

package org.eclipse.mosaic.lib.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.mosaic.lib.geo.CartesianPoint;

import com.google.gson.Gson;
import org.junit.Test;

public class CartesianPointAdapterTest {

    @Test
    public void fromJson() {
        CTest cTest = new Gson().fromJson("{position:{x:52,y:13},value:300}", CTest.class);
        assertEquals(CartesianPoint.xy(52, 13), cTest.position);
    }

    @Test
    public void fromJsonNull() {
        CTest cTest = new Gson().fromJson("{value:300}", CTest.class);
        assertNull(cTest.position);
    }

    @Test
    public void toJson() {
        CTest cTest = new CTest();
        cTest.position = CartesianPoint.xy(52, 13);

        String json = new Gson().toJson(cTest);
        assertEquals("{\"position\":{\"x\":52.0,\"y\":13.0},\"value\":0.0}", json);
    }

    @Test
    public void toJsonNull() {
        CTest cTest = new CTest();

        String json = new Gson().toJson(cTest);
        assertEquals("{\"value\":0.0}", json);
    }


    static class CTest {

        CartesianPoint position;
        double value;
    }
}