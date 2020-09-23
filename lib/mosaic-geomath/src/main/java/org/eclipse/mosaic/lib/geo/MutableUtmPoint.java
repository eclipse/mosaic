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

import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.transform.GeoProjection;

import java.util.Locale;

public class MutableUtmPoint implements UtmPoint {

    private static final long serialVersionUID = 1L;

    public double easting;
    public double northing;
    public double altitude;
    public UtmZone utmZone;

    public MutableUtmPoint() {
        set(0, 0, 0, null);
    }

    public MutableUtmPoint(UtmPoint other) {
        set(other.getEasting(), other.getNorthing(), other.getAltitude(), other.getZone());
    }

    public MutableUtmPoint(double easting, double northing, double altitude, String zoneEncoded) {
        set(easting, northing, altitude, UtmZone.from(zoneEncoded));
    }

    public MutableUtmPoint(double easting, double northing, double altitude, UtmZone zone) {
        set(easting, northing, altitude, zone);
    }

    public MutableUtmPoint set(UtmPoint other) {
        return this.set(other.getEasting(), other.getNorthing(), other.getAltitude(), other.getZone());
    }

    public MutableUtmPoint set(double easting, double northing, double altitude, UtmZone zone) {
        this.easting = easting;
        this.northing = northing;
        this.altitude = altitude;
        this.utmZone = zone;
        return this;
    }

    @Override
    public double getEasting() {
        return easting;
    }

    @Override
    public double getNorthing() {
        return northing;
    }

    @Override
    public double getAltitude() {
        return altitude;
    }

    @Override
    public UtmZone getZone() {
        return utmZone;
    }

    public boolean isInSameZone(MutableUtmPoint other) {
        return this.getZone() != null
                && other.getZone() != null
                && this.getZone().equals(other.getZone());
        // FIXME was: return zoneNumber == -1 || other.zoneNumber == -1 || (zoneNumber == other.zoneNumber && northernHemisphere == other.northernHemisphere);
    }

    @Override
    public double distanceTo(UtmPoint other) {
        if (other.getZone().equals(other.getZone())) {
            double de = (other.getEasting() - getEasting());
            double dn = (other.getNorthing() - getNorthing());
            return Math.sqrt(dn * dn + de * de);
        } else {
            return GeoUtils.distanceBetween(this.toGeo(), other.toGeo());
        }
    }

    @Override
    public Vector3d toVector3d(Vector3d result) {
        return GeoProjection.getInstance().utmToVector(this, result);
    }

    public GeoPoint toGeo() {
        return GeoProjection.getInstance().utmToGeographic(this);
    }

    public MutableGeoPoint toGeo(MutableGeoPoint result) {
        return GeoProjection.getInstance().utmToGeographic(this, result);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UtmPoint) {
            UtmPoint v = (UtmPoint) o;
            return v.getNorthing() == getNorthing()
                    && v.getEasting() == getEasting()
                    && v.getAltitude() == getAltitude();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        long h = Double.doubleToLongBits(northing) ^ Double.doubleToLongBits(easting)
                ^ Double.doubleToLongBits(altitude);
        return (int) ((h) ^ (~(h >> 32)));
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "UtmPoint{easting=%.2f,northing=%.2f,alt=%.2f}", this.easting, this.northing, this.altitude);
    }
}
