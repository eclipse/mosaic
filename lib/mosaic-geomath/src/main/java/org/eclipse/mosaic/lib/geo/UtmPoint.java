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

import org.eclipse.mosaic.lib.gson.UtmPointAdapter;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(UtmPointAdapter.class)
public interface UtmPoint extends Point<UtmPoint> {

    double getNorthing();

    double getEasting();

    double getAltitude();

    UtmZone getZone();

    GeoPoint toGeo();

    static UtmPoint northEast(UtmZone zone, double northing, double easting) {
        return eastNorth(zone, easting, northing);
    }

    static UtmPoint northEast(UtmZone zone, double northing, double easting, double altitude) {
        return eastNorth(zone, easting, northing, altitude);
    }

    static UtmPoint eastNorth(UtmZone zone, double easting, double northing) {
        return eastNorth(zone, easting, northing, 0);
    }

    static UtmPoint eastNorth(UtmZone zone, double easting, double northing, double altitude) {
        return new MutableUtmPoint(easting, northing, altitude, zone);
    }
}
