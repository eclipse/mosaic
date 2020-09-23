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

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link CartesianCircle} represents an immutable pair of a {@link CartesianPoint}
 * center position and a radius in meters.
 */
public class CartesianCircle implements Circle<CartesianPoint>, CartesianArea {

    private static final long serialVersionUID = 1L;

    private final CartesianPoint center;
    private final double radius;

    /**
     * Construct a new {@link CartesianCircle}.
     *
     * @param center the center of this region
     * @param radius the radius of this region. Unit: [m].
     */
    public CartesianCircle(CartesianPoint center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public CartesianPoint getCenter() {
        return center;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public Bounds<CartesianPoint> getBounds() {
        return new CartesianRectangle(
                CartesianPoint.xy(center.getX() - radius, center.getY() - radius),
                CartesianPoint.xy(center.getX() + radius, center.getY() + radius)
        );
    }

    @Override
    public GeoCircle toGeo() {
        return new GeoCircle(getCenter().toGeo(), getRadius());
    }

    @Override
    public CartesianPolygon toPolygon() {
        return toPolygon(1);
    }

    public CartesianPolygon toPolygon(double degreeResolution) {
        final List<CartesianPoint> vertices = new ArrayList<>();
        int steps = (int) (360 / degreeResolution);
        for (int i = 0; i < steps; i++) {
            double radians = Math.toRadians(i * degreeResolution);
            vertices.add(CartesianPoint.xy(
                    getCenter().getX() + getRadius() * Math.sin(radians),
                    getCenter().getY() + getRadius() * Math.cos(radians)
            ));
        }
        return new CartesianPolygon(vertices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CartesianCircle other = (CartesianCircle) o;
        return Double.compare(radius, other.getRadius()) == 0
                && this.getCenter().equals(other.getCenter());
    }

    @Override
    public int hashCode() {
        long longHash = 6618299L;
        longHash = 31 * longHash + Double.doubleToLongBits(radius);
        longHash = 31 * longHash + getCenter().hashCode();
        return (int) (longHash ^ (longHash >>> 32));
    }
}
