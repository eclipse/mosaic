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

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.geo.MutableUtmPoint;
import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.geo.UtmZone;
import org.eclipse.mosaic.lib.math.Vector3d;

import org.junit.Test;

public class Wgs84ProjectionTest {

    @Test
    public void conversion_Europe_Berlin() {
        testUtmConversion(
                GeoPoint.latLon(52.520817, 13.409414),
                new MutableUtmPoint(392081, 5820156, 0, UtmZone.from("33u"))
        );
    }

    @Test
    public void conversion_Europe_Austria() {
        testUtmConversion(
                GeoPoint.latLon(47.0631844, 15.5854173),
                new MutableUtmPoint(544454, 5212352, 0, UtmZone.from("33t"))
        );
    }

    @Test
    public void conversion_Europe_Norway() {
        testUtmConversion(
                GeoPoint.latLon(60.124167, 6.74),
                new MutableUtmPoint(374432, 6667387, 0, UtmZone.from("32v"))
        );
    }

    @Test
    public void conversion_SouthAfrica() {
        testUtmConversion(
                GeoPoint.latLon(-34.3581, 18.472139),
                new MutableUtmPoint(267514, 6195242, 0, UtmZone.from("34h"))
        );
    }

    @Test
    public void conversion_NorthAmerica() {
        testUtmConversion(
                GeoPoint.latLon(39.628625, -79.319977),
                new MutableUtmPoint(644184, 4387888, 0, UtmZone.from("17s"))
        );
    }

    @Test
    public void conversion_SouthAmerica() {
        testUtmConversion(
                GeoPoint.latLon(-15.777562, -69.403485),
                new MutableUtmPoint(456783, 8255628, 0, UtmZone.from("19l"))
        );
    }

    @Test
    public void conversion_Australia() {
        testUtmConversion(
                GeoPoint.latLon(-25.344428, 131.036882),
                new MutableUtmPoint(704992, 7195353, 0, UtmZone.from("52j"))
        );
    }

    @Test
    public void conversion_China() {
        testUtmConversion(
                GeoPoint.latLon(39.999695, 116.326458),
                new MutableUtmPoint(442506, 4427941, 0, UtmZone.from("50s"))
        );
    }

    @Test
    public void conversion_Dubai() {
        testUtmConversion(
                GeoPoint.latLon(25.001058, 54.991490),
                new MutableUtmPoint(297297, 2766567, 0, UtmZone.from("40r"))
        );
    }

    @Test
    public void conversion_Africa_NorthernHemisphere() {
        testUtmConversion(
                GeoPoint.latLon(0.564641, 38.498895),
                new MutableUtmPoint(444242, 62412, 0, UtmZone.from("37n"))
        );
    }

    @Test
    public void conversion_Africa_SouthernHemisphere() {
        testUtmConversion(
                GeoPoint.latLon(-0.292282, 36.059930),
                new MutableUtmPoint(172703, 9967651, 0, UtmZone.from("37m"))
        );
    }

    @Test
    public void geo_utm_vector_conversion() {
        GeoPoint origin = GeoPoint.latLon(52.520817, 13.409414);
        GeoProjection transform = new Wgs84Projection(origin);

        for (int i = 0; i < 10; i++) {
            double x = Math.cos(i * Math.PI / 5) * 0.1;
            double y = Math.sin(i * Math.PI / 5) * 0.1;

            GeoPoint tstGeoPt = GeoPoint.latLon(origin.getLatitude() + y, origin.getLongitude() + x);

            UtmPoint geoToUtm = transform.geographicToUtm(tstGeoPt);
            Vector3d geoToVec = transform.geographicToVector(tstGeoPt);

            Vector3d utmToVec = transform.utmToVector(geoToUtm);
            GeoPoint utmToGeo = transform.utmToGeographic(geoToUtm);

            GeoPoint vecToGeo = transform.vectorToGeographic(geoToVec);
            UtmPoint vecToUtm = transform.vectorToUtm(geoToVec);

            assertEquals(geoToVec.x, utmToVec.x, 0.01);
            assertEquals(geoToVec.y, utmToVec.y, 0.01);
            assertEquals(geoToVec.z, utmToVec.z, 0.01);

            assertEquals(geoToUtm.getNorthing(), vecToUtm.getNorthing(), 0.01);
            assertEquals(geoToUtm.getEasting(), vecToUtm.getEasting(), 0.01);
            assertEquals(geoToUtm.getAltitude(), vecToUtm.getAltitude(), 0.01);

            assertEquals(utmToGeo.getLatitude(), vecToGeo.getLatitude(), 0.01);
            assertEquals(utmToGeo.getLongitude(), vecToGeo.getLongitude(), 0.01);
            assertEquals(utmToGeo.getAltitude(), vecToGeo.getAltitude(), 0.01);
        }

    }

    private void testUtmConversion(GeoPoint wgs84, MutableUtmPoint utm) {
        GeoProjection transform = new Wgs84Projection(wgs84);

        UtmPoint actualUtm = transform.geographicToUtm(wgs84);

        assertEquals(utm.getEasting(), actualUtm.getEasting(), 1d);
        assertEquals(utm.getNorthing(), actualUtm.getNorthing(), 1d);
        assertEquals(utm.getAltitude(), actualUtm.getAltitude(), 1d);
        assertEquals(utm.getZone(), actualUtm.getZone());

        GeoPoint actualGeoPoint = transform.utmToGeographic(utm);

        assertEquals(wgs84.getLatitude(), actualGeoPoint.getLatitude(), 0.0001d);
        assertEquals(wgs84.getLongitude(), actualGeoPoint.getLongitude(), 0.0001d);
        assertEquals(wgs84.getAltitude(), actualGeoPoint.getAltitude(), 0.0001d);
    }

    @Test
    public void conversion_Europe_Austria_withOffset() {
        testCartesianConversion(
                GeoPoint.latLon(47.0631844, 15.5854173),
                new MutableCartesianPoint(57170, 18211, 0),
                new MutableCartesianPoint(-487283.63, -5194140.89, 0)
        );
    }

    private void testCartesianConversion(GeoPoint wgs84, MutableCartesianPoint cartesianPoint, MutableCartesianPoint cartesianOffset) {
        GeoProjection transform = new Wgs84Projection(wgs84, cartesianOffset);

        CartesianPoint actualCartesian = transform.geographicToCartesian(wgs84);

        assertEquals(cartesianPoint.getX(), actualCartesian.getX(), 1d);
        assertEquals(cartesianPoint.getY(), actualCartesian.getY(), 1d);
        assertEquals(cartesianPoint.getZ(), actualCartesian.getZ(), 1d);

        GeoPoint actualGeoPoint = transform.cartesianToGeographic(cartesianPoint);

        assertEquals(wgs84.getLatitude(), actualGeoPoint.getLatitude(), 0.0001d);
        assertEquals(wgs84.getLongitude(), actualGeoPoint.getLongitude(), 0.0001d);
        assertEquals(wgs84.getAltitude(), actualGeoPoint.getAltitude(), 0.0001d);
    }

    @Test
    public void conversion_largeAltitude() {
        GeoProjection transform = new Wgs84Projection(GeoPoint.latLon(52, 13));

        // Vector3d can contain arbitrary altitudes (e.g. camera position in a zoomed out visualizer)
        Vector3d largeAltitudeVector = new Vector3d(0.0, 1e5, 0.0);
        transform.vectorToGeographic(largeAltitudeVector);
        transform.vectorToUtm(largeAltitudeVector);
    }

    @Test
    public void conversion_largeVector() {
        GeoProjection transform = new Wgs84Projection(GeoPoint.latLon(52, 13));

        // Vector3d can contain arbitrary x/z locations (e.g. camera position in a zoomed out visualizer)
        // transforming such a vector might not yield meaningful coordinates but should not result in an exception
        // caused by invalid / out of range lat/lon values.
        Vector3d largeVector = new Vector3d(55408330, 0.00, 17049723);
        transform.vectorToGeographic(largeVector);
        transform.vectorToUtm(largeVector);
    }
}