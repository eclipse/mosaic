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

public class CartesianRectangle implements Rectangle<CartesianPoint>, Bounds<CartesianPoint>, CartesianArea {

    private static final long serialVersionUID = 1L;

    private final CartesianPoint a;
    private final CartesianPoint b;

    /**
     * Construct a new {@link GeoRectangle}.
     *
     * @param pointA the first point.
     * @param pointB the second point.
     * @throws IllegalArgumentException if both elements equals.
     */
    public CartesianRectangle(CartesianPoint pointA, CartesianPoint pointB) {
        this.a = pointA;
        this.b = pointB;
    }

    @Override
    public CartesianPoint getA() {
        return a;
    }

    @Override
    public CartesianPoint getB() {
        return b;
    }

    @Override
    public double getSideA() {
        return Math.min(a.getY(), b.getY());
    }

    @Override
    public double getSideB() {
        return Math.max(a.getX(), b.getX());
    }

    @Override
    public double getSideC() {
        return Math.max(a.getY(), b.getY());
    }

    @Override
    public double getSideD() {
        return Math.min(a.getX(), b.getX());
    }

    @Override
    public boolean contains(final CartesianPoint point) {
        return point.getX() >= getSideD() && point.getX() <= getSideB()
                && point.getY() >= getSideA() && point.getY() <= getSideC();
    }

    @Override
    public double getArea() {
        return (getSideC() - getSideA()) * (getSideB() - getSideD());
    }

    @Override
    public Bounds<CartesianPoint> getBounds() {
        return new CartesianRectangle(getA(), getB());
    }

    /**
     * Calculates the center of gravity of this rectangle.
     *
     * @return CartesianPoint
     */
    @Override
    public CartesianPoint getCenter() {
        double x = getA().getX() + ((getB().getX() - getA().getX()) / 2);
        double y = getA().getY() + ((getB().getY() - getA().getY()) / 2);
        return CartesianPoint.xy(x, y);
    }

    @Override
    public CartesianPolygon toPolygon() {
        return new CartesianPolygon(
                CartesianPoint.xy(getA().getX(), getA().getY()),
                CartesianPoint.xy(getA().getX(), getB().getY()),
                CartesianPoint.xy(getB().getX(), getB().getY()),
                CartesianPoint.xy(getB().getX(), getA().getY())
        );
    }

    public GeoRectangle toGeo() {
        return new GeoRectangle(getA().toGeo(), getB().toGeo());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CartesianRectangle other = (CartesianRectangle) o;
        return this.getA().equals(other.getA())
                && this.getB().equals(other.getB());
    }

    @Override
    public int hashCode() {
        long longHash = 10881662L;
        longHash = 31 * longHash + getA().hashCode();
        longHash = 31 * longHash + getB().hashCode();
        return (int) (longHash ^ (longHash >>> 32));
    }

}
