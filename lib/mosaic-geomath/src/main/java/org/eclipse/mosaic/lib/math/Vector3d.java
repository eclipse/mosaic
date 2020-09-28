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

package org.eclipse.mosaic.lib.math;

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.geo.MutableGeoPoint;
import org.eclipse.mosaic.lib.transform.GeoProjection;

import java.io.Serializable;
import java.util.Locale;

public class Vector3d implements Serializable {

    private static final long serialVersionUID = 1L;

    public double x;
    public double y;
    public double z;

    public Vector3d() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d(Vector3d v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public Vector3d set(Vector3d v) {
        x = v.x;
        y = v.y;
        z = v.z;
        return this;
    }

    public Vector3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3d set(double f) {
        x = f;
        y = f;
        z = f;
        return this;
    }

    public Vector3d multiply(double f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public Vector3d multiply(double f, Vector3d result) {
        result.x = x * f;
        result.y = y * f;
        result.z = z * f;
        return result;
    }

    public Vector3d scale(Vector3d scale) {
        x *= scale.x;
        y *= scale.y;
        z *= scale.z;
        return this;
    }

    public Vector3d scale(Vector3d scale, Vector3d result) {
        result.x = x * scale.x;
        result.y = y * scale.y;
        result.z = z * scale.z;
        return result;
    }

    public Vector3d add(Vector3d v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    public Vector3d add(Vector3d v, Vector3d result) {
        result.x = x + v.x;
        result.y = y + v.y;
        result.z = z + v.z;
        return result;
    }

    public Vector3d subtract(Vector3d v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    public Vector3d subtract(Vector3d v, Vector3d result) {
        result.x = x - v.x;
        result.y = y - v.y;
        result.z = z - v.z;
        return result;
    }


    public double dot(Vector3d v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector3d cross(Vector3d v, Vector3d result) {
        result.x = y * v.z - z * v.y;
        result.y = z * v.x - x * v.z;
        result.z = x * v.y - y * v.x;
        return result;
    }

    /**
     * Calculates and returns the angle between the current and the given vector in radians.
     *
     * @param v
     * @return angle in radians
     */
    public double angle(Vector3d v) {
        double a = dot(v) / (magnitude() * v.magnitude());
        return Math.acos(a);
    }

    public Vector3d norm() {
        double mag = magnitude();
        if (!MathUtils.isFuzzyZero(mag)) {
            return multiply(1.0f / mag);
        } else {
            return this;
        }
    }

    public Vector3d norm(Vector3d result) {
        double mag = magnitude();
        if (!MathUtils.isFuzzyZero(mag)) {
            return multiply(1.0f / magnitude(), result);
        } else {
            return result.set(0);
        }
    }

    public double magnitude() {
        return Math.sqrt(magnitudeSqr());
    }

    public double magnitudeSqr() {
        return x * x + y * y + z * z;
    }

    public double distanceTo(Vector3d v) {
        return Math.sqrt(distanceSqrTo(v));
    }

    public double distanceSqrTo(Vector3d v) {
        double dx = (v.x - x);
        double dy = (v.y - y);
        double dz = (v.z - z);
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Rotates this vector by the specified angle (in degrees) around the given axis.
     *
     * @param angleDeg Rotation angle in degrees
     * @param axis     Rotation axis
     */
    public Vector3d rotateDeg(double angleDeg, Vector3d axis) {
        return rotate(Math.toRadians(angleDeg), axis.x, axis.y, axis.z);
    }

    /**
     * Rotates this vector by the specified angle (in radians) around the given axis.
     *
     * @param angle Rotation angle in radians
     * @param axis  Rotation axis
     */
    public Vector3d rotate(double angle, Vector3d axis) {
        return rotate(angle, axis.x, axis.y, axis.z);
    }

    /**
     * Rotates this vector by the specified angle (in radians) around the given axis.
     *
     * @param angle Rotation angle in radians
     * @param axisX Rotation axis x-component
     * @param axisY Rotation axis y-component
     * @param axisZ Rotation axis z-component
     */
    public Vector3d rotate(double angle, double axisX, double axisY, double axisZ) {
        double c = Math.cos(angle);
        double c1 = 1.0 - c;
        double s = Math.sin(angle);

        double tx = x * (axisX * axisX * c1 + c) + y * (axisX * axisY * c1 - axisZ * s) + z
                * (axisX * axisZ * c1 + axisY * s);
        double ty = x * (axisY * axisX * c1 + axisZ * s) + y * (axisY * axisY * c1 + c) + z
                * (axisY * axisZ * c1 - axisX * s);
        double tz = x * (axisX * axisZ * c1 - axisY * s) + y * (axisY * axisZ * c1 + axisX * s) + z
                * (axisZ * axisZ * c1 + c);
        x = tx;
        y = ty;
        z = tz;
        return this;
    }

    public void toArray(double[] result, int offset) {
        result[offset] = x;
        result[offset + 1] = y;
        result[offset + 2] = z;
    }

    public void toArray(float[] result, int offset) {
        result[offset] = (float) x;
        result[offset + 1] = (float) y;
        result[offset + 2] = (float) z;
    }

    public CartesianPoint toCartesian() {
        return toCartesian(new MutableCartesianPoint());
    }

    public MutableCartesianPoint toCartesian(MutableCartesianPoint result) {
        return result.set(x, -z, y);
    }

    public GeoPoint toGeo() {
        return GeoProjection.getInstance().vectorToGeographic(this);
    }

    public MutableGeoPoint toGeo(MutableGeoPoint result) {
        return GeoProjection.getInstance().vectorToGeographic(this, result);
    }

    public boolean isFuzzyEqual(Vector3d other) {
        return MathUtils.isFuzzyEqual(x, other.x) &&
                MathUtils.isFuzzyEqual(y, other.y) &&
                MathUtils.isFuzzyEqual(z, other.z);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vector3d) {
            Vector3d v = (Vector3d) o;
            return v.x == x && v.y == y && v.z == z;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = result * 31 + Double.hashCode(y);
        result = result * 31 + Double.hashCode(z);
        return result;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "(%7.2f, %7.2f, %7.2f)", x, y, z);
    }
}
