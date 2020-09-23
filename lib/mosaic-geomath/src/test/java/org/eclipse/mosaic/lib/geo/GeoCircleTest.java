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
import org.junit.rules.TestRule;

public class GeoCircleTest {

    private static GeoPoint BERLIN = GeoPoint.latLon(52.5, 13.4);

    @Rule
    public TestRule geoProjectionRule = new GeoProjectionRule(BERLIN);

    @Test
    public void getBounds() {
        // SETUP
        GeoPoint center = GeoPoint.latLon(52.5, 13.7);
        GeoCircle circle = new GeoCircle(center, 1500);

        // RUN
        Bounds<GeoPoint> bounds = circle.getBounds();

        // ASSERT
        assertEquals(52.4865, bounds.getSideA(), 0.0001);
        assertEquals(13.7221, bounds.getSideB(), 0.0001);
        assertEquals(52.5135, bounds.getSideC(), 0.0001);
        assertEquals(13.6779, bounds.getSideD(), 0.0001);

        assertEquals(circle.getCenter(), bounds.getCenter());
        assertEquals(Math.pow(circle.getRadius() * 2, 2), bounds.getArea(), 0.1);
    }

}