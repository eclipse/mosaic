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

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;
import org.eclipse.mosaic.lib.misc.Tuple;
import org.eclipse.mosaic.lib.transform.GeoCalculator;
import org.eclipse.mosaic.lib.transform.GeoProjection;
import org.eclipse.mosaic.lib.transform.SimpleGeoCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class GeoUtils {

    private static final GeoCalculator DEFAULT_GEO_CALCULATOR = new SimpleGeoCalculator();

    private static GeoCalculator getGeoCalculator() {
        // if no GeoProjection is initialized, the default GeoCalculator is used.
        if (GeoProjection.isInitialized()) {
            return GeoProjection.getInstance().getGeoCalculator();
        }
        return DEFAULT_GEO_CALCULATOR;
    }

    public static double distanceBetween(final GeoPoint a, final GeoPoint b) {
        Vector3d result = distanceBetween(a, b, new Vector3d());
        return Math.sqrt(result.x * result.x + result.z * result.z);
    }

    public static Vector3d distanceBetween(final GeoPoint a, final GeoPoint b, Vector3d result) {
        getGeoCalculator().distanceBetween(a, b, result);
        return result;
    }

    /**
     * Calculates and returns the angle (azimuth) between two points as seen from {@code pointA}. The
     * azimuth value starts with {@code 0} degrees referring to North, and increases clockwise up to {@code 360} degrees.
     *
     * @param a the base point
     * @param b the point to calculate the azimuth based on {@code pointA}
     * @return the azimuth value in [degrees] from 0 (North) clockwise to 360
     */
    public static double azimuth(final GeoPoint a, final GeoPoint b) {
        final Vector3d result = new Vector3d();
        distanceBetween(a, b, result);
        return VectorUtils.getHeadingFromDirection(result);
    }

    /**
     * Calculates and returns the angle (azimuth) between two points as seen from {@code pointA}. The
     * azimuth value starts with {@code 0} degrees referring to North, and increases clockwise up to {@code 360} degrees.
     *
     * @param a the base point
     * @param b the point to calculate the azimuth based on {@code pointA}
     * @return the azimuth value in [degrees] from 0 (North) clockwise to 360
     */
    public static double azimuth(CartesianPoint a, CartesianPoint b) {
        return (360 + Math.toDegrees(Math.atan2((b.getX() - a.getX()), (b.getY() - a.getY())))) % 360;
    }

    private final static double LINE_MATCHING_RESOLUTION_IN_METERS = 50;

    /**
     * Searches for the {@link MutableGeoPoint} on a line between two given points {@code linePointA} and {@code linePointB}
     * which is closest to the given point {@code point}.
     *
     * @param point      the point which is used to search for the closest point on the given line
     * @param linePointA the start of the line on which the resulting point is located
     * @param linePointB the end of the line on which the resulting point is located
     * @return the {@link MutableGeoPoint} on the line between {@code linePointA} and {@code linePointB} which is closest to {@code point}
     */
    public static GeoPoint closestPointOnLine(final GeoPoint point, final GeoPoint linePointA, final GeoPoint linePointB) {
        final double azimuth = azimuth(linePointA, linePointB);
        final double orthodromicDistance = distanceBetween(linePointA, linePointB);
        final int parts = (int) Math.max(20, Math.ceil(orthodromicDistance / LINE_MATCHING_RESOLUTION_IN_METERS));
        final double orthodromicDistancePart = orthodromicDistance / parts;

        final Collection<Tuple<Double, GeoPoint>> closestDistances = new ArrayList<>(parts);

        for (int i = 0; i < parts; ++i) {
            final double offset = orthodromicDistancePart * i;
            final GeoPoint offsetGeographicPoint = getGeoPointFromDirection(
                    linePointA,
                    azimuth,
                    offset
            );

            closestDistances.add(new Tuple<>(
                    distanceBetween(offsetGeographicPoint, point),
                    offsetGeographicPoint
            ));
        }

        return closestDistances.stream().min(Comparator.comparingDouble(Tuple::getA)).get().getB();
    }

    /**
     * Calculates a geographic coordinate based on an origin point and a
     * direction with length.
     *
     * @param origin   The geographic position to start from
     * @param azimuth  The azimuth in decimal degrees from -180° to 180°
     * @param distance The orthodromic distance in the same units as the
     *                 ellipsoid axis (meters by default)
     */
    public static GeoPoint getGeoPointFromDirection(final GeoPoint origin, double azimuth, double distance) {
        Vector3d direction = new Vector3d(
                distance * Math.sin(Math.toRadians(azimuth)),
                0,
                -distance * Math.cos(Math.toRadians(azimuth))
        );
        return getGeoPointFromDirection(origin, direction);
    }

    public static GeoPoint getGeoPointFromDirection(final GeoPoint origin, Vector3d direction) {
        MutableGeoPoint result = new MutableGeoPoint(0, 0, 0);
        getGeoCalculator().pointFromDirection(origin, direction, result);
        return result;
    }

    /**
     * Returns a random {@link MutableGeoPoint} within the radius of the given point.
     *
     * @param random the random number generator to generate random points
     * @param origin Initial geographic position
     * @param radius Radius around the geographic position, unit: [m]
     * @return Random geographic position within the radius of the given point
     */
    public static GeoPoint getRandomGeoPoint(RandomNumberGenerator random, final GeoPoint origin, double radius) {
        double randomDistance = random.nextDouble(1, radius);
        double randomAzimuth = random.nextDouble(0, 360) - 180;
        return getGeoPointFromDirection(origin, randomAzimuth, randomDistance);
    }

    /**
     * Returns the exact point which between the two given points. The orthodromic distance
     * from the resulting point to {@code pointA} or {@code pointB} is equal to
     * the half of the distance between {@code pointA} and {@code pointB}.
     *
     * @param pointA the first point
     * @param pointB the second point
     * @return the point in center of both points
     */
    public static GeoPoint getPointBetween(final GeoPoint pointA, final GeoPoint pointB) {
        double distance = distanceBetween(pointA, pointB);
        double azimuth = azimuth(pointA, pointB);
        GeoPoint centerPoint = getGeoPointFromDirection(pointA, azimuth, distance / 2);
        return GeoPoint.lonLat(
                centerPoint.getLongitude(), centerPoint.getLatitude(),
                (pointA.getAltitude() + pointB.getAltitude()) / 2
        );
    }

    /**
     * Returns the Bing quad key tile with the specified zoom level that contains the given {@link GeoPoint}.
     */
    public static long getQuadKey(final GeoPoint geoPoint, int zoom) {
        long x = (long) Math.floor((geoPoint.getLongitude() + 180) / 360 * (1 << zoom));
        long y = (long) Math.floor((1 - Math.log(Math.tan(Math.toRadians(geoPoint.getLatitude()))
                + 1 / Math.cos(Math.toRadians(geoPoint.getLatitude()))) / Math.PI) / 2 * (1 << zoom));

        long quadKey = 0;
        for (int i = 0; i <= zoom; i++) {
            quadKey = quadKey | ((y & (1 << i)) << (i + 1)) | ((x & (1 << i)) << i);
        }
        return quadKey;
    }
}
