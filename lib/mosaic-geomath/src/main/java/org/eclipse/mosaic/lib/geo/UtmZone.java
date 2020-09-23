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

import java.io.Serializable;

/**
 * This class provide a zone for the Universal Transverse Mercator coordinate
 * system.
 *
 * @see <a href="https://commons.wikimedia.org/wiki/File:Utm-zones.jpg">https://commons.wikimedia.org/wiki/File:Utm-zones.jpg</a>
 */
public final class UtmZone implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static char[] UTM_LETTERS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };


    private static final int ZONE_MIN_VALUE = 1;
    private static final int ZONE_MAX_VALUE = 60;

    /* 60 zones, each 6° of longitude in width. */
    private static final int ZONE_WIDTH = 360 / ZONE_MAX_VALUE;
    private static final int ZONE_HEIGHT_SHARING = 4;

    private final static UtmZone[][] ZONE_CACHE = new UtmZone[ZONE_MAX_VALUE][26];

    public final int number;
    public final char letter;

    private UtmZone(final int number, final char letter) {
        if (number < ZONE_MIN_VALUE) {
            throw new IllegalArgumentException("zone not in range (" + number + "<" + ZONE_MIN_VALUE + ")");
        }
        if (number > ZONE_MAX_VALUE) {
            throw new IllegalArgumentException("zone not in range (" + number + ">" + ZONE_MAX_VALUE + ")");
        }
        this.number = number;
        this.letter = letter;
    }

    public static UtmZone from(String zoneEncoded) {
        try {
            return from(
                    Integer.parseInt(zoneEncoded.substring(0, zoneEncoded.length() - 1)),
                    zoneEncoded.toLowerCase().charAt(zoneEncoded.length() - 1)
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid UTM zone: " + zoneEncoded);
        }
    }

    /**
     * Construct a utmZone based on the standardized zone descriptions (number & letter).
     *
     * @param zoneNumber 1 <= x <= 60
     * @param letter     The name of the UTM zone
     */
    public static UtmZone from(int zoneNumber, char letter) {
        int zoneIndex = zoneNumber - ZONE_MIN_VALUE;
        if (zoneIndex >= ZONE_CACHE.length) {
            throw new IllegalArgumentException("Invalid zone number " + zoneNumber);
        }
        int letterIndex = letter - 'a';
        if (letterIndex >= ZONE_CACHE[0].length) {
            throw new IllegalArgumentException("Invalid zone letter " + zoneNumber);
        }

        UtmZone zoneInCache = ZONE_CACHE[zoneIndex][letterIndex];
        if (zoneInCache == null) {
            zoneInCache = new UtmZone(zoneNumber, letter);
            ZONE_CACHE[zoneIndex][letterIndex] = zoneInCache;
        }
        return zoneInCache;
    }

    public static UtmZone from(GeoPoint geoPoint) {
        int tmpNumber = (int) (Math.floor((geoPoint.getLongitude() + 180.0) / ZONE_WIDTH) + 1);

        /*
         * Exceptions: On the southwest coast of Norway, grid zone 32V (9° of longitude in width) is extended further west, and grid zone 31V (3°
         * of longitude in width) is correspondingly shrunk to cover only open water. Also, in the region around Svalbard, the four grid zones 31X
         * (9° of longitude in width), 33X (12° of longitude in width), 35X (12° of longitude in width), and 37X (9° of longitude in width) are
         * extended to cover what would otherwise have been covered by the seven grid zones 31X to 37X. The three grid zones 32X, 34X and 36X are not
         * used. Universal Transverse Mercator coordinate system. (2012, December 11). In Wikipedia, The Free Encyclopedia. Retrieved 13:14,
         * January 20, 2013, from http://en.wikipedia.org/w/index.php?title=Universal_Transverse_Mercator_coordinate_system&oldid=527595935
         */
        if (geoPoint.getLatitude() >= 56.0 && geoPoint.getLatitude() < 64.0 && geoPoint.getLongitude() >= 3.0 && geoPoint.getLongitude() < 12.0) {
            tmpNumber = 32;
        } else if (geoPoint.getLatitude() >= 72.0 && geoPoint.getLatitude() < 84.0) {
            final double[] deformationLongitudeBounds = {0.0, 9.0, 21.0, 33.0, 42.0};
            for (int i = 0; i < deformationLongitudeBounds.length - 1; ++i) {
                if (geoPoint.getLongitude() >= deformationLongitudeBounds[i] && geoPoint.getLongitude() < deformationLongitudeBounds[i + 1]) {
                    tmpNumber = 31 + i * 2;
                    break;
                }
            }
        }
        return from(tmpNumber, getLetter(tmpNumber, geoPoint.getLatitude()));
    }

    public static char getLetter(int zoneNumber, double latitude) {
        if (latitude >= -80 && latitude <= 84) {
            return getLetter((int) Math.floor((latitude + 80) / 8) + ZONE_HEIGHT_SHARING / 2);
        } else if (latitude < -80) {
            return zoneNumber < 30 ? 'a' : 'b';
        } else if (latitude > 84) {
            return zoneNumber < 30 ? 'y' : 'z';
        }
        throw new IllegalArgumentException("Invalid latitude");
    }

    public final int getNumber() {
        return number;
    }

    public char getLetter() {
        return letter;
    }

    public boolean isNorthernHemisphere() {
        return getLetter() >= 'n';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UtmZone utmZone = (UtmZone) o;
        return number == utmZone.number && letter == utmZone.letter;
    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + (int) letter;
        return result;
    }

    @Override
    public String toString() {
        return "UTMZone [number=" + number + ", letter=" + letter + "]";
    }

    public static char getLetter(final int letter) {
        if (letter >= UTM_LETTERS.length) {
            throw new IllegalArgumentException("The integer " + letter + " is not a valid letter.");
        }
        return UTM_LETTERS[letter];
    }


}
