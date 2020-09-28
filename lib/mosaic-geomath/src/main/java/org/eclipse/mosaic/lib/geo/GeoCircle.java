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

import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.transform.GeoProjection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A {@link GeoCircle} represents an immutable pair of a {@link GeoPoint}
 * center position and a radius in meters.
 */
public class GeoCircle implements Circle<GeoPoint>, GeoArea {

    private static final long serialVersionUID = 1L;

    private final GeoPoint center;
    private final double radius;

    /**
     * Construct a new {@link GeoCircle}.
     *
     * @param center the center of this region
     * @param radius the radius of this region. Unit: [m].
     */
    public GeoCircle(GeoPoint center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public GeoPoint getCenter() {
        return center;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public Bounds<GeoPoint> getBounds() {
        final Vector3d tmpDirection = new Vector3d();

        GeoPoint northWest = GeoProjection.getInstance().getGeoCalculator().pointFromDirection(
                getCenter(), tmpDirection.set(-radius, 0, -radius), new MutableGeoPoint()
        );
        GeoPoint southEast = GeoProjection.getInstance().getGeoCalculator().pointFromDirection(
                getCenter(), tmpDirection.set(radius, 0, radius), new MutableGeoPoint()
        );
        return new GeoRectangle(northWest, southEast);
    }

    @Override
    public CartesianCircle toCartesian() {
        return new CartesianCircle(getCenter().toCartesian(), getRadius());
    }

    @Override
    public GeoPolygon toPolygon() {
        return toPolygon(1);
    }

    public GeoPolygon toPolygon(double degreeResolution) {
        final List<GeoPoint> vertices = new ArrayList<>();
        final Vector3d direction = new Vector3d();
        int steps = (int) (360 / degreeResolution);
        for (int i = 0; i < steps; i++) {
            double radians = Math.toRadians(i * degreeResolution);
            direction.set(radius * Math.sin(radians), 0, - getRadius() * Math.cos(radians));
            vertices.add(GeoProjection.getInstance().getGeoCalculator().pointFromDirection(
                    getCenter(), direction, new MutableGeoPoint()
            ));
        }
        return new GeoPolygon(vertices);
    }


    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "GeoCircle{center=%s,radius=%.2f}", this.center, this.radius);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoCircle other = (GeoCircle) o;
        return Double.compare(radius, other.getRadius()) == 0
                && this.getCenter().equals(other.getCenter());
    }

    @Override
    public int hashCode() {
        long longHash = 20816263L;
        longHash = 31 * longHash + Double.doubleToLongBits(radius);
        longHash = 31 * longHash + getCenter().hashCode();
        return (int) (longHash ^ (longHash >>> 32));
    }

}
