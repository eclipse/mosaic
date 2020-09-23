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

public class MutableCartesianPoint implements CartesianPoint {

    private static final long serialVersionUID = 1L;

    public double x;
    public double y;
    public double z;


    public MutableCartesianPoint() {
        this(0, 0, 0);
    }

    public MutableCartesianPoint(double x, double y, double z) {
        set(x, y, z);
    }

    public MutableCartesianPoint set(CartesianPoint other) {
        set(other.getX(), other.getY(), other.getZ());
        return this;
    }

    public MutableCartesianPoint set(double x, double y, double z) {
        if (Double.isNaN(x)) {
            throw new IllegalArgumentException("Invalid x coordinate");
        }
        if (Double.isNaN(y)) {
            throw new IllegalArgumentException("Invalid y coordinate");
        }
        if (Double.isNaN(z)) {
            throw new IllegalArgumentException("Invalid z coordinate");
        }

        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public double distanceTo(CartesianPoint other) {
        double dx = (other.getX() - getX());
        double dy = (other.getY() - getY());
        double dz = (other.getZ() - getZ());
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public Vector3d toVector3d(Vector3d result) {
        return result.set(getX(), getZ(), -getY());
    }

    @Override
    public GeoPoint toGeo() {
        return GeoProjection.getInstance().cartesianToGeographic(this);
    }

    public MutableGeoPoint toGeo(MutableGeoPoint result) {
        return GeoProjection.getInstance().cartesianToGeographic(this, result);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CartesianPoint) {
            CartesianPoint c = (CartesianPoint) o;
            return c.getX() == getX()
                    && c.getY() == getY()
                    && c.getZ() == getZ();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        long h = Double.doubleToLongBits(getX()) ^ Double.doubleToLongBits(getY())
                ^ Double.doubleToLongBits(getZ());
        return (int) ((h) ^ (~(h >> 32)));
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "CartesianPoint{x=%.2f,y=%.2f,z=%.2f}", this.getX(), this.getY(), this.getZ());
    }
}