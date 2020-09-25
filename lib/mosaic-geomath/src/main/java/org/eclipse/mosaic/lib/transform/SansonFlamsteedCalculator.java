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

package org.eclipse.mosaic.lib.transform;

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.MutableGeoPoint;
import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Vector3d;

public class SansonFlamsteedCalculator implements GeoCalculator {

    private static final double LAT_LONG_SCALE = ReferenceEllipsoid.WGS_84.circumference / 360.0; // meters per degree
    private static final double INV_LAT_LONG_SCALE = 1.0 / LAT_LONG_SCALE; // degrees per meter

    private final GeoPoint origin;

    public SansonFlamsteedCalculator(GeoPoint origin) {
        this.origin = origin;
    }

    @Override
    public Vector3d distanceBetween(GeoPoint a, GeoPoint b, Vector3d result) {
        double xFrom = convertWgs84LongToMeters(a.getLongitude(), a.getLatitude()) - convertWgs84LongToMeters(origin.getLongitude(), a.getLatitude());
        double yFrom = convertWgs84LatToMeters(a.getLatitude()) - convertWgs84LatToMeters(origin.getLatitude());

        double xTo = convertWgs84LongToMeters(b.getLongitude(), b.getLatitude()) - convertWgs84LongToMeters(origin.getLongitude(), b.getLatitude());
        double yTo = convertWgs84LatToMeters(b.getLatitude()) - convertWgs84LatToMeters(origin.getLatitude());

        return result.set(xTo - xFrom, b.getAltitude() - a.getAltitude(), yTo - yFrom);
    }

    private double convertWgs84LongToMeters(double lon, double lat) {
        return lon * LAT_LONG_SCALE * Math.cos(Math.toRadians(lat));
    }

    private double convertWgs84LatToMeters(double lat) {
        return -lat * LAT_LONG_SCALE;
    }

    @Override
    public MutableGeoPoint pointFromDirection(GeoPoint src, Vector3d direction, MutableGeoPoint result) {
        double xSrc = convertWgs84LongToMeters(src.getLongitude(), src.getLatitude()) - convertWgs84LongToMeters(origin.getLongitude(), src.getLatitude());
        double ySrc = convertWgs84LatToMeters(src.getLatitude()) - convertWgs84LatToMeters(origin.getLatitude());
        xSrc += direction.x;
        ySrc += direction.z; //FIXME -z?

        double resultLat = origin.getLatitude() - ySrc * INV_LAT_LONG_SCALE;
        double resultLon = origin.getLongitude();

        double cos = Math.cos(Math.toRadians(resultLat));
        if (cos > 0.0) {
            resultLon += xSrc * INV_LAT_LONG_SCALE / cos;
        }

        double latSafe = MathUtils.clamp(resultLat, -90.0, 90.0);
        double lonSafe = toDegrees(MathUtils.wrapAnglePiPi(toRadians(resultLon)));
        return result.set(latSafe, lonSafe, src.getAltitude() + direction.y);
    }
}
