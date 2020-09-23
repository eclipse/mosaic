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

package org.eclipse.mosaic.lib.transform;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.MutableGeoPoint;
import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;

/**
 * Geo calculator based on Harvesine formulas.
 */
public class HarvesineGeoCalculator implements GeoCalculator {

    private final static Vector3d NORTH = VectorUtils.NORTH;
    private final static double EARTH_R = ReferenceEllipsoid.WGS_84.equatorialRadius;

    @Override
    public Vector3d distanceBetween(GeoPoint a, GeoPoint b, Vector3d result) {
        double latA = toRadians(a.getLatitude());
        double latB = toRadians(b.getLatitude());
        double lonA = toRadians(a.getLongitude());
        double lonB = toRadians(b.getLongitude());
        double tmp = pow(sin((latB - latA) / 2), 2) + cos(latA) * cos(latB) * pow(sin((lonB - lonA) / 2), 2);
        double distance = 2 * atan2(sqrt(tmp), sqrt(1 - tmp)) * EARTH_R;

        double azimuth = atan2(
                sin(lonB - lonA) * cos(latB),
                cos(latA) * sin(latB) - sin(latA) * cos(latB) * cos(latB - latA)
        );
        return result.set(
                distance * sin(azimuth),
                b.getAltitude() - a.getAltitude(),
                -distance * cos(azimuth)
        );
    }

    @Override
    public MutableGeoPoint pointFromDirection(GeoPoint origin, Vector3d direction, MutableGeoPoint result) {
        double lat1 = toRadians(origin.getLatitude());
        double lon1 = toRadians(origin.getLongitude());

        double distance = sqrt(direction.x * direction.x + direction.z * direction.z);
        double azimuth = atan2(direction.x, -direction.z) - atan2(NORTH.x, -NORTH.z);

        double lat2 = asin(
                sin(lat1) * cos(distance / EARTH_R) + cos(lat1) * sin(distance / EARTH_R) * cos(azimuth)
        );
        double lon2 = lon1 + atan2(
                sin(azimuth) * sin(distance / EARTH_R) * cos(lat1),
                cos(distance / EARTH_R) - sin(lat1) * sin(lat2)
        );

        double lat = toDegrees(lat2);
        double lon = (toDegrees(lon2) + 540) % 360 - 180;
        double latSafe = MathUtils.clamp(lat, -90.0, 90.0);
        double lonSafe = toDegrees(MathUtils.wrapAnglePiPi(toRadians(lon)));

        return result.set(latSafe, lonSafe, origin.getAltitude() + direction.y);
    }
}
