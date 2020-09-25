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

import static junit.framework.TestCase.assertTrue;

import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.math.MeanErrorAggregator;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.misc.Tuple;
import org.eclipse.mosaic.lib.transform.GeoCalculator;
import org.eclipse.mosaic.lib.transform.GeoProjection;
import org.eclipse.mosaic.lib.transform.HarvesineGeoCalculator;
import org.eclipse.mosaic.lib.transform.SansonFlamsteedCalculator;
import org.eclipse.mosaic.lib.transform.SimpleGeoCalculator;
import org.eclipse.mosaic.lib.transform.UtmGeoCalculator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class GeoCalculationTest {

    private static GeoPoint BERLIN = GeoPoint.latLon(52.5, 13.4);

    @Rule
    public TestRule geoProjectionRule = new GeoProjectionRule(BERLIN);

    @Test
    public void spheroidGeoCalculator_longDistance() throws Exception {
        MeanErrorAggregator distanceError = new MeanErrorAggregator();
        MeanErrorAggregator azimuthError = new MeanErrorAggregator();
        MeanErrorAggregator roundTripError = new MeanErrorAggregator();

        GeoProjection.getInstance().setGeoCalculator(new HarvesineGeoCalculator());
        massTest(distanceError, azimuthError, roundTripError);

        assertTrue(distanceError.meanError() < 50d);
//        assertTrue(azimuthError.meanError() < 1d);
        assertTrue(roundTripError.meanError() < 50d);
    }

    @Test
    public void spheroidGeoCalculator_shortDistance() throws Exception {
        MeanErrorAggregator distanceError = new MeanErrorAggregator();
        MeanErrorAggregator azimuthError = new MeanErrorAggregator();
        MeanErrorAggregator roundTripError = new MeanErrorAggregator();

        GeoProjection.getInstance().setGeoCalculator(new HarvesineGeoCalculator());
        massTest(distanceError, azimuthError, roundTripError, "distance_test_values_short.csv");

        assertTrue(distanceError.meanError() < 1d);
//        assertTrue(azimuthError.meanError() < 1d);
        assertTrue(roundTripError.meanError() < 0.1d);
    }

    @Test
    public void simpleGeoCalculator_shortDistance() throws Exception {
        MeanErrorAggregator distanceError = new MeanErrorAggregator();
        MeanErrorAggregator azimuthError = new MeanErrorAggregator();
        MeanErrorAggregator roundTripError = new MeanErrorAggregator();

        GeoProjection.getInstance().setGeoCalculator(new SimpleGeoCalculator());
        massTest(distanceError, azimuthError, roundTripError, "distance_test_values_short.csv");

        assertTrue(distanceError.meanError() < 1d);
//        assertTrue(azimuthError.meanError() < 1d);
        assertTrue(roundTripError.meanError() < 0.1d);
    }

    @Test
    public void utmGeoCalculator_shortDistance() throws Exception {
        MeanErrorAggregator distanceError = new MeanErrorAggregator();
        MeanErrorAggregator azimuthError = new MeanErrorAggregator();
        MeanErrorAggregator roundTripError = new MeanErrorAggregator();

        GeoProjection.getInstance().setGeoCalculator(new UtmGeoCalculator());
        massTest(distanceError, azimuthError, roundTripError, "distance_test_values_short.csv");

        assertTrue(distanceError.meanError() < 1d);
//        assertTrue(azimuthError.meanError() < 1d);
        assertTrue(roundTripError.meanError() < 0.5d);
    }

    @Test
    public void utmGeoCalculator_shortDistance_Berlin() throws Exception {
        MeanErrorAggregator distanceError = new MeanErrorAggregator();
        MeanErrorAggregator azimuthError = new MeanErrorAggregator();
        MeanErrorAggregator roundTripError = new MeanErrorAggregator();

        GeoProjection.getInstance().setGeoCalculator(new UtmGeoCalculator());
        massTest(distanceError, azimuthError, roundTripError, "distance_test_values_short_berlin.csv");

        assertTrue(distanceError.meanError() < 1d);
//        assertTrue(azimuthError.meanError() < 1d);
        assertTrue(roundTripError.meanError() < 0.1d);
    }

    @Test
    public void sansonFlamsteedGeoCalculator_shortDistance_Berlin() throws Exception {
        MeanErrorAggregator distanceError = new MeanErrorAggregator();
        MeanErrorAggregator azimuthError = new MeanErrorAggregator();
        MeanErrorAggregator roundTripError = new MeanErrorAggregator();

        GeoProjection.getInstance().setGeoCalculator(new SansonFlamsteedCalculator(BERLIN));
        massTest(distanceError, azimuthError, roundTripError, "distance_test_values_short_berlin.csv");

        assertTrue(distanceError.meanError() < 1d);
//        assertTrue(azimuthError.meanError() < 1d);
        assertTrue(roundTripError.meanError() < 0.1d);
    }

    private void massTest(MeanErrorAggregator distanceError, MeanErrorAggregator azimuthError, MeanErrorAggregator roundTripError) throws IOException {
        massTest(distanceError, azimuthError, roundTripError, "distance_test_values.csv");
    }

    private void massTest(MeanErrorAggregator distanceError, MeanErrorAggregator azimuthError, MeanErrorAggregator roundTripError, String testFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + testFile)))) {
            reader.lines().forEach(line -> {
                        String[] items = line.split(";");
                        GeoPoint pointA = GeoPoint.latLon(Double.parseDouble(items[0]), Double.parseDouble(items[1]));
                        GeoPoint pointB = GeoPoint.latLon(Double.parseDouble(items[2]), Double.parseDouble(items[3]));
                        double expectedDistance = Double.parseDouble(items[4]);
                        double expectedAzimuth = (Double.parseDouble(items[5]) + 360) % 360;

                        double distance = GeoUtils.distanceBetween(pointA, pointB);
                        distanceError.add(distance, expectedDistance);

                        double azimuth = GeoUtils.azimuth(pointA, pointB);
                        azimuthError.add(azimuth, expectedAzimuth);

                        GeoPoint roundTrip = GeoUtils.getGeoPointFromDirection(pointA, azimuth, distance);
                        double distanceRoundTrip = GeoUtils.distanceBetween(pointB, roundTrip);
                        roundTripError.add(distanceRoundTrip, 0);
                    }
            );

        }

        System.out.println("Mean distance error: " + distanceError.meanError());
        System.out.println("Mean azimuth error: " + azimuthError.meanError());
        System.out.println("Mean roundtrip error: " + roundTripError.meanError());
    }

    @Test
    public void performanceTest() throws IOException {
        List<Tuple<GeoPoint, GeoPoint>> points = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/distance_test_values.csv")))) {
            reader.lines().forEach(line -> {
                        String[] items = line.split(";");
                        points.add(new Tuple<>(
                                GeoPoint.latLon(Double.parseDouble(items[0]), Double.parseDouble(items[1])),
                                GeoPoint.latLon(Double.parseDouble(items[2]), Double.parseDouble(items[3]))
                        ));
                    }
            );
        }

        for (GeoCalculator calculator : new GeoCalculator[]{
                new SimpleGeoCalculator(),
                new HarvesineGeoCalculator(),
                new UtmGeoCalculator(),
                new SansonFlamsteedCalculator(BERLIN)
        }) {
            long start = System.nanoTime();
            for (Tuple<GeoPoint, GeoPoint> tuple : points) {
                calculator.distanceBetween(tuple.getA(), tuple.getB(), new Vector3d());
            }
            long duration = System.nanoTime() - start;
            System.out.println(calculator.getClass().getSimpleName() + ": " + duration / 1e6 + "ms");
        }

    }

}