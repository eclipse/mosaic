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
import org.eclipse.mosaic.lib.geo.MutableUtmPoint;
import org.eclipse.mosaic.lib.math.Vector3d;

public class UtmGeoCalculator implements GeoCalculator {

    private final GeoCalculator fallbackModel = new HarvesineGeoCalculator();
    private final GeoProjection geoProjection;

    private final MutableUtmPoint utmA = new MutableUtmPoint();
    private final MutableUtmPoint utmB = new MutableUtmPoint();

    public UtmGeoCalculator() {
        this(GeoProjection.getInstance());
    }

    public UtmGeoCalculator(GeoProjection geoProjection) {
        this.geoProjection = geoProjection;
    }

    @Override
    public Vector3d distanceBetween(GeoPoint a, GeoPoint b, Vector3d result) {
        synchronized (utmA) {
            geoProjection.geographicToUtm(a, utmA);
            geoProjection.geographicToUtm(b, utmB);

            if (utmA.getZone().number == utmB.getZone().number && utmB.getZone().isNorthernHemisphere() == utmB.getZone().isNorthernHemisphere()) {
                double dX = utmB.getEasting() - utmA.getEasting();
                double dY = utmB.getNorthing() - utmA.getNorthing();
                double dZ = utmB.getAltitude() - utmA.getAltitude();
                result.set(dX, dZ, -dY);
            } else {
                fallbackModel.distanceBetween(a, b, result);
            }
        }
        return result;
    }

    @Override
    public MutableGeoPoint pointFromDirection(GeoPoint origin, Vector3d direction, MutableGeoPoint result) {
        synchronized (utmA) {
            geoProjection.geographicToUtm(origin, utmA);
            utmA.set(
                    utmA.getEasting() + direction.x,
                    utmA.getNorthing() - direction.z,
                    utmA.getAltitude() + direction.y,
                    utmA.getZone()
            );
            geoProjection.utmToGeographic(utmA, result);
        }
        return result;
    }
}
