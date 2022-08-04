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

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.MutableGeoPoint;
import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Vector3d;

public class SimpleGeoCalculator implements GeoCalculator {

    @Override
    public Vector3d distanceBetween(GeoPoint a, GeoPoint b, Vector3d result) {
        double radius = Math.cos(Math.toRadians(a.getLatitude())) * ReferenceEllipsoid.WGS_84.equatorialRadius;
        double scaleY = (Math.PI * 2 * ReferenceEllipsoid.WGS_84.equatorialRadius) / 360;
        double scaleX = (Math.PI * 2 * radius) / 360;
        double dY = (b.getLatitude() - a.getLatitude()) * scaleY;
        double dX = (b.getLongitude() - a.getLongitude()) * scaleX;
        double dZ = b.getAltitude() - a.getAltitude();
        return result.set(dX, dZ, -dY);
    }

    @Override
    public MutableGeoPoint pointFromDirection(GeoPoint src, Vector3d direction, MutableGeoPoint result) {
        double radius = Math.cos(Math.toRadians(src.getLatitude())) * ReferenceEllipsoid.WGS_84.equatorialRadius;
        double scaleY = (Math.PI * 2 * ReferenceEllipsoid.WGS_84.equatorialRadius) / 360;
        double scaleX = (Math.PI * 2 * radius) / 360;

        double lat = src.getLatitude() - direction.z / scaleY;
        double lon = src.getLongitude() + direction.x / scaleX;

        // ensure valid lat/lon ranges
        double latSafe = MathUtils.clamp(lat, -90.0, 90.0);
        double lonSafe = Math.toDegrees(MathUtils.wrapAnglePiPi(Math.toRadians(lon)));
        return result.set(latSafe, lonSafe, src.getAltitude() + direction.y);
    }

}
