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
import org.eclipse.mosaic.lib.transform.GeoProjection;
import org.eclipse.mosaic.lib.transform.HarvesineGeoCalculator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class GeoUtilsTest {

    private static GeoPoint BERLIN = GeoPoint.latLon(52.5, 13.4);

    @Rule
    public TestRule geoProjectionRule = new GeoProjectionRule(BERLIN);

    @Before
    public void setup() {
        GeoProjection.getInstance().setGeoCalculator(new HarvesineGeoCalculator());
    }

    @Test
    public void getPointInBetween() {
        // SETUP
        GeoPoint pointA = GeoPoint.latLon(52.4, 12.2, 100);
        GeoPoint pointB = GeoPoint.latLon(50.1, 10.2, 200);

        // RUN
        GeoPoint pointBetween = GeoUtils.getPointBetween(pointA, pointB);

        // ASSERT
        double distancePointA = pointBetween.distanceTo(pointA);
        double distancePointB = pointBetween.distanceTo(pointB);
        double expectedDistance = pointA.distanceTo(pointB) / 2;

        assertEquals(expectedDistance, distancePointA, 0.2);
        assertEquals(expectedDistance, distancePointB, 0.2);

        assertEquals(51.2549, pointBetween.getLatitude(), 0.0001);
        assertEquals(11.1730, pointBetween.getLongitude(), 0.0001);
        assertEquals(150, pointBetween.getAltitude(), 0.0001);
    }

    @Test
    public void pointOnLine() {
        GeoPoint pointA = GeoPoint.latLon(52.4, 12.2);
        GeoPoint pointB = GeoPoint.latLon(50.1, 10.2);

        // this point is ~195m away from the line between pointA and pointB
        GeoPoint searchPoint = GeoPoint.latLon(51.616226, 11.487377);

        // RUN
        GeoPoint pointOnLine = GeoUtils.closestPointOnLine(searchPoint, pointA, pointB);

        // ASSERT
        assertEquals(51.6153, pointOnLine.getLatitude(), 0.0001);
        assertEquals(11.4898, pointOnLine.getLongitude(), 0.0001);
        assertEquals(195, searchPoint.distanceTo(pointOnLine), 1);
    }



    @Test
    public void testAzimuth() {

        double azimuth = GeoUtils.azimuth(
                GeoPoint.lonLat(13.3856, 52.5415),
                GeoPoint.lonLat(13.3556, 52.5212)
        );
        assertEquals(azimuth, 221.96, 0.1);

        azimuth = GeoUtils.azimuth(
                GeoPoint.lonLat(13.3556, 52.5212),
                GeoPoint.lonLat(13.3856, 52.5415)
        );
        assertEquals(azimuth, 41.94, 0.1);

        azimuth = GeoUtils.azimuth(
                GeoPoint.lonLat(13.3523, 52.5212),
                GeoPoint.lonLat(13.3856, 52.51215)
        );
        assertEquals(azimuth, 113.99, 0.1);
    }

    @Test
    public void testAzimuthCartesian() {
        double azimuth = GeoUtils.azimuth(
                CartesianPoint.xyz(46841.44,52221.76,  0),
                CartesianPoint.xyz(46814.99,52163.45, 0)
        );
        assertEquals(204, azimuth, 1);

        azimuth = GeoUtils.azimuth(
                CartesianPoint.xyz(48173.47,52648.06, 0),
                CartesianPoint.xyz(48133.34,52648.45,  0)
        );
        assertEquals(270, azimuth, 5);

        azimuth = GeoUtils.azimuth(
                CartesianPoint.xyz(45773.30,51238.11, 0),
                CartesianPoint.xyz(45851.46,51207.13,  0)
        );
        assertEquals(112, azimuth, 5);
    }
}
