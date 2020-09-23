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

import org.eclipse.mosaic.lib.gson.GeoPointAdapter;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(GeoPointAdapter.class)
public interface GeoPoint extends Point<GeoPoint> {

    GeoPoint ORIGO = new MutableGeoPoint(0, 0, 0);

    double getLatitude();

    double getLongitude();

    double getAltitude();

    CartesianPoint toCartesian();

    UtmPoint toUtm();

    /**
     * Creates a {@link MutableGeoPoint} from latitude and longitude.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @return a GeoPoint
     * @throws IllegalArgumentException if a coordinate is invalid or {@link Double#NaN}.
     */
    static GeoPoint latLon(double latitude, double longitude) {
        return new MutableGeoPoint(latitude, longitude, 0);
    }

    /**
     * Creates a {@link MutableGeoPoint} from latitude and longitude.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @param altitude  the altitude in [m]
     * @return a GeoPoint
     * @throws IllegalArgumentException if a coordinate is invalid or {@link Double#NaN}.
     */
    static GeoPoint latLon(double latitude, double longitude, double altitude) {
        return new MutableGeoPoint(latitude, longitude, altitude);
    }

    /**
     * Creates a {@link MutableGeoPoint} from latitude and longitude.
     *
     * @param longitude the longitude
     * @param latitude  the latitude
     * @return a GeoPoint
     * @throws IllegalArgumentException if a coordinate is invalid or {@link Double#NaN}.
     */
    static GeoPoint lonLat(double longitude, double latitude) {
        return latLon(latitude, longitude);
    }

    /**
     * Creates a {@link MutableGeoPoint} from latitude and longitude.
     *
     * @param longitude the longitude
     * @param latitude  the latitude
     * @param altitude  the altitude in [m]
     * @return a GeoPoint
     * @throws IllegalArgumentException if a coordinate is invalid or {@link Double#NaN}.
     */
    static GeoPoint lonLat(double longitude, double latitude, double altitude) {
        return latLon(latitude, longitude, altitude);
    }


}
