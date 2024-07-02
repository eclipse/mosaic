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

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.geo.MutableGeoPoint;
import org.eclipse.mosaic.lib.geo.MutableUtmPoint;
import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.geo.UtmZone;
import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Vector3d;

/**
 * Converts WGS84 based coordinates (lat,lon) to cartesian coordinates (x,y)
 * using a WGS84 to UTM conversion algorithm. The conversion is based on
 * the algorithm written by Chuck Gantz (chuck.gantz@globalstar.com).
 */
public class Wgs84Projection extends GeoProjection {

    public static final double K_0 = 0.9996;
    private final ReferenceEllipsoid ellipsoid = ReferenceEllipsoid.WGS_84;

    private final GeoPoint geoOrigin;
    private final UtmPoint utmOrigin;

    private boolean failIfOutsideWorld = false;
    private boolean useZoneOfUtmOrigin = false;

    /**
     * Initializes the projection based on the {@link GeoPoint}
     * which is used as the playground center when converting
     * to local coordinates.
     *
     * @param origin the playground center
     */
    public Wgs84Projection(GeoPoint origin) {
        this.geoOrigin = origin;
        this.utmOrigin = geographicToUtm(origin);
    }

    /**
     * Initializes the projection based on the {@link UtmPoint}
     * which is used as the playground center when converting
     * to local coordinates.
     *
     * @param origin the playground center
     */
    public Wgs84Projection(UtmPoint origin) {
        this.geoOrigin = utmToGeographic(origin);
        this.utmOrigin = origin;
    }

    /**
     * This is a legacy constructor, still used internally.
     *
     * @deprecated Legacy constructor.
     */
    @Deprecated
    public Wgs84Projection(GeoPoint playgroundCenter, CartesianPoint cartesianOffset) {
        final UtmZone zone = UtmZone.from(playgroundCenter);
        this.utmOrigin = new MutableUtmPoint(
                -cartesianOffset.getX(),
                -cartesianOffset.getY(),
                -cartesianOffset.getZ(),
                zone
        );
        this.geoOrigin = utmToGeographic(utmOrigin);
        setGeoCalculator(new UtmGeoCalculator(this));
    }

    /**
     * Enables the option to fail if the conversion from UTM leads to a coordinate outside of the world.
     *
     * @return this projection
     */
    public Wgs84Projection failIfOutsideWorld() {
        this.failIfOutsideWorld = true;
        return this;
    }

    /**
     * Enables the option to use the zone of the UTM origin point when converting to UTM Point.
     *
     * @return this projection
     */
    public Wgs84Projection useZoneOfUtmOrigin() {
        this.useZoneOfUtmOrigin = true;
        return this;
    }

    @Override
    public Vector3d geographicToVector(GeoPoint geographic, Vector3d result) {
        getGeoCalculator().distanceBetween(geoOrigin, geographic, result);
        return result;
    }

    @Override
    public MutableGeoPoint vectorToGeographic(Vector3d vector3d, MutableGeoPoint result) {
        getGeoCalculator().pointFromDirection(geoOrigin, vector3d, result);
        return result;
    }

    @Override
    public MutableCartesianPoint geographicToCartesian(GeoPoint geographic, MutableCartesianPoint result) {
        return geographicToVector(geographic, new Vector3d()).toCartesian(result);
    }

    @Override
    public MutableGeoPoint cartesianToGeographic(CartesianPoint cartesian, MutableGeoPoint result) {
        return vectorToGeographic(cartesian.toVector3d(), result);
    }

    @Override
    public Vector3d utmToVector(UtmPoint utm, Vector3d result) {
        return geographicToVector(utmToGeographic(utm), result);
    }

    @Override
    public MutableUtmPoint vectorToUtm(Vector3d vector, MutableUtmPoint result) {
        return geographicToUtm(vectorToGeographic(vector), result);
    }

    /**
     * Converts lat/long to UTM coords. Equations from USGS Bulletin 1532
     * East Longitudes are positive, West longitudes are negative.
     * North latitudes are positive, South latitudes are negative
     * Lat and Long are in decimal degrees
     * Written by Chuck Gantz- chuck.gantz@globalstar.com
     */
    @SuppressWarnings("checkstyle:localvariablename")
    @Override
    public MutableUtmPoint geographicToUtm(GeoPoint geoPoint, MutableUtmPoint result) {
        double latRad = Math.toRadians(geoPoint.getLatitude());
        // Make sure the longitude is between -180.00 .. 179.9
        double longTemp = (geoPoint.getLongitude() + 180) - (int) ((geoPoint.getLongitude() + 180) / 360) * 360 - 180;
        double longRad = Math.toRadians(longTemp);
        int zoneNumber = extractZoneNumber(geoPoint, longTemp);

        double longOrigin = (zoneNumber - 1) * 6 - 180 + 3;  //+3 puts origin in middle of zone
        double longOriginRad = Math.toRadians(longOrigin);

        double eccSquared = ellipsoid.eccentricitySquared;
        double eccToTheFourth = eccSquared * eccSquared;
        double eccToTheSixth = eccSquared * eccSquared * eccSquared;
        double eccPrimeSquared = (eccSquared) / (1 - eccSquared);

        double a = ellipsoid.equatorialRadius;
        double N = a / Math.sqrt(1 - eccSquared * Math.sin(latRad) * Math.sin(latRad));
        double T = Math.tan(latRad) * Math.tan(latRad);
        double C = eccPrimeSquared * Math.cos(latRad) * Math.cos(latRad);
        double A = Math.cos(latRad) * (longRad - longOriginRad);

        double M = a * ((1 - eccSquared / 4 - 3 * eccToTheFourth / 64 - 5 * eccToTheSixth / 256) * latRad
                - (3 * eccSquared / 8 + 3 * eccToTheFourth / 32 + 45 * eccToTheSixth / 1024) * Math.sin(2 * latRad)
                + (15 * eccToTheFourth / 256 + 45 * eccToTheSixth / 1024) * Math.sin(4 * latRad)
                - (35 * eccToTheSixth / 3072) * Math.sin(6 * latRad));

        double resultEasting = K_0 * N * (A + (1 - T + C) * A * A * A / 6
                + (5 - 18 * T + T * T + 72 * C - 58 * eccPrimeSquared) * A * A * A * A * A / 120)
                + 500000.0;

        double resultNorthing = K_0 * (M + N * Math.tan(latRad) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24
                + (61 - 58 * T + T * T + 600 * C - 330 * eccPrimeSquared) * A * A * A * A * A * A / 720));
        if (geoPoint.getLatitude() < 0) {
            resultNorthing += 10000000.0; //10000000 meter offset for southern hemisphere
        }
        final UtmZone zone = UtmZone.from(zoneNumber, UtmZone.getLetter(zoneNumber, geoPoint.getLatitude()));
        return result.set(resultEasting, resultNorthing, geoPoint.getAltitude(), zone);
    }

    private int extractZoneNumber(GeoPoint geoPoint, double longTemp) {
        int zoneNumber;

        if (useZoneOfUtmOrigin && utmOrigin != null) {
            zoneNumber = utmOrigin.getZone().number;
        } else {
            zoneNumber = (int) ((longTemp + 180) / 6) + 1;

            if (geoPoint.getLatitude() >= 56.0 && geoPoint.getLatitude() < 64.0 && longTemp >= 3.0 && longTemp < 12.0) {
                zoneNumber = 32;
            }

            // Special zones for Svalbard
            if (geoPoint.getLatitude() >= 72.0 && geoPoint.getLatitude() < 84.0) {
                if (longTemp >= 0.0 && longTemp < 9.0) {
                    zoneNumber = 31;
                } else if (longTemp >= 9.0 && longTemp < 21.0) {
                    zoneNumber = 33;
                } else if (longTemp >= 21.0 && longTemp < 33.0) {
                    zoneNumber = 35;
                } else if (longTemp >= 33.0 && longTemp < 42.0) {
                    zoneNumber = 37;
                }
            }
        }
        return zoneNumber;
    }

    @SuppressWarnings("checkstyle:localvariablename")
    @Override
    public MutableGeoPoint utmToGeographic(UtmPoint utmPoint, MutableGeoPoint result) {
        // remove 500,000 meter offset for longitude
        double x = utmPoint.getEasting() - 500000.0;
        double y = utmPoint.getNorthing();

        if (!utmPoint.getZone().isNorthernHemisphere()) {
            y -= 10000000.0;
        }

        double a = ellipsoid.equatorialRadius;
        double longOrigin = (utmPoint.getZone().getNumber() - 1) * 6 - 180 + 3;  //+3 puts origin in middle of zone

        double eccSquared = ellipsoid.eccentricitySquared;
        double e1 = (1 - Math.sqrt(1 - eccSquared)) / (1 + Math.sqrt(1 - eccSquared));
        double eccPrimeSquared = (eccSquared) / (1 - eccSquared);

        double M = y / K_0;
        double mu = M / (a * (1 - eccSquared / 4 - 3 * eccSquared * eccSquared / 64 - 5 * eccSquared * eccSquared * eccSquared / 256));

        double phi1Rad = mu + (3 * e1 / 2 - 27 * e1 * e1 * e1 / 32) * Math.sin(2 * mu)
                + (21 * e1 * e1 / 16 - 55 * e1 * e1 * e1 * e1 / 32) * Math.sin(4 * mu)
                + (151 * e1 * e1 * e1 / 96) * Math.sin(6 * mu);

        double a1 = 1 - eccSquared * Math.sin(phi1Rad) * Math.sin(phi1Rad);

        double N1 = a / Math.sqrt(a1);
        double T1 = Math.tan(phi1Rad) * Math.tan(phi1Rad);
        double C1 = eccPrimeSquared * Math.cos(phi1Rad) * Math.cos(phi1Rad);
        double R1 = a * (1 - eccSquared) / Math.pow(a1, 1.5);
        double D = x / (N1 * K_0);

        double lat = phi1Rad - (N1 * Math.tan(phi1Rad) / R1)
                * (D * D / 2 - (5 + 3 * T1 + 10 * C1 - 4 * C1 * C1 - 9 * eccPrimeSquared) * D * D * D * D / 24
                + (61 + 90 * T1 + 298 * C1 + 45 * T1 * T1 - 252 * eccPrimeSquared - 3 * C1 * C1) * D * D * D * D * D * D / 720);
        double resultLatitude = Math.toDegrees(lat);

        double lng = (D - (1 + 2 * T1 + C1) * D * D * D / 6 + (5 - 2 * C1 + 28 * T1 - 3 * C1 * C1 + 8 * eccPrimeSquared + 24 * T1 * T1)
                * D * D * D * D * D / 120) / Math.cos(phi1Rad);
        double resultLongitude = longOrigin + Math.toDegrees(lng);

        if (!failIfOutsideWorld) {
            resultLatitude = MathUtils.clamp(resultLatitude, -90.0, 90.0);
            resultLongitude = toDegrees(MathUtils.wrapAnglePiPi(toRadians(resultLongitude)));
        }

        return result.set(resultLatitude, resultLongitude, utmPoint.getAltitude());
    }
}
