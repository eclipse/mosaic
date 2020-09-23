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

import org.eclipse.mosaic.lib.transform.ReferenceEllipsoid;

import java.util.Locale;

/**
 * Represents an immutable pair of two different {@link GeoPoint}s.
 * Together they form a rectangular area.
 */
public class GeoRectangle implements Rectangle<GeoPoint>, Bounds<GeoPoint>, GeoArea {

    private static final long serialVersionUID = 1L;

    private final GeoPoint a;
    private final GeoPoint b;

    /**
     * Construct a new {@link GeoRectangle}.
     *
     * @param pointA the first point.
     * @param pointB the second point.
     * @throws IllegalArgumentException if both elements equals.
     */
    public GeoRectangle(GeoPoint pointA, GeoPoint pointB) {
        this.a = pointA;
        this.b = pointB;
    }

    @Override
    public GeoPoint getA() {
        return a;
    }

    @Override
    public GeoPoint getB() {
        return b;
    }

    @Override
    public double getSideA() {
        return Math.min(a.getLatitude(), b.getLatitude());
    }

    @Override
    public double getSideB() {
        return Math.max(a.getLongitude(), b.getLongitude());
    }

    @Override
    public double getSideC() {
        return Math.max(a.getLatitude(), b.getLatitude());
    }

    @Override
    public double getSideD() {
        return Math.min(a.getLongitude(), b.getLongitude());
    }

    @Override
    public Bounds<GeoPoint> getBounds() {
        return new GeoRectangle(this.getA(), this.getB());
    }

    /**
     * Checks whether the point is located in the rectangle area.
     *
     * @param point the point to check.
     * @return true if the point located in rectangle area.
     */
    @Override
    public boolean contains(final GeoPoint point) {
        return point.getLongitude() >= getSideD() && point.getLongitude() <= getSideB()
                && point.getLatitude() >= getSideA() && point.getLatitude() <= getSideC();
    }

    @Override
    public double getArea() {
        double radius = ReferenceEllipsoid.WGS_84.equatorialRadius;
        double lat1 = getSideA();
        double lon1 = getSideB();
        double lat2 = getSideC();
        double lon2 = getSideD();

        return Math.PI * radius * radius
                * Math.abs(Math.sin(Math.toRadians(lat1)) - Math.sin(Math.toRadians(lat2)))
                * Math.abs(lon1 - lon2) / 180;
    }

    @Override
    public GeoPolygon toPolygon() {
        return new GeoPolygon(
                GeoPoint.latLon(getA().getLatitude(), getA().getLongitude()),
                GeoPoint.latLon(getA().getLatitude(), getB().getLongitude()),
                GeoPoint.latLon(getB().getLatitude(), getB().getLongitude()),
                GeoPoint.latLon(getB().getLatitude(), getA().getLongitude())
        );
    }

    /**
     * Calculates the center of gravity of this rectangle.
     *
     * @return GeoPoint
     */
    @Override
    public GeoPoint getCenter() {
        double lat = getA().getLatitude() + ((getB().getLatitude() - getA().getLatitude()) / 2);
        double lon = getA().getLongitude() + ((getB().getLongitude() - getA().getLongitude()) / 2);
        return GeoPoint.latLon(lat, lon);
    }

    public CartesianRectangle toCartesian() {
        return new CartesianRectangle(getA().toCartesian(), getB().toCartesian());
    }


    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "GeoRectangle{a=%s,b=%s}", this.a, this.b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoRectangle other = (GeoRectangle) o;
        return this.getA().equals(other.getA())
                && this.getB().equals(other.getB());
    }

    @Override
    public int hashCode() {
        long longHash = 8817722001L;
        longHash = 31 * longHash + getA().hashCode();
        longHash = 31 * longHash + getB().hashCode();
        return (int) (longHash ^ (longHash >>> 32));
    }

}
