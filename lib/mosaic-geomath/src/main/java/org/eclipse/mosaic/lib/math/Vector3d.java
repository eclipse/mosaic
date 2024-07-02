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

    /**
     * Constructor for {@link Vector3d}, which initializes it as V_0.
     * <pre>
     *     |0|
     * v = |0|
     *     |0|
     * </pre>
     */
    public Vector3d() {
        x = 0;
        y = 0;
        z = 0;
    }

    /**
     * Constructor for {@link Vector3d}, initializing it with the given coordinates.
     * <pre>
     *     |x|
     * v = |y|
     *     |z|
     * </pre>
     *
     * @param x X-Coordinate
     * @param y Y-Coordinate
     * @param z Z-Coordinate
     */
    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Constructor for {@link Vector3d},
     * initializing it with a copy of the coordinates from another given {@link Vector3d}.
     * <pre>
     *  Vector3d(v_j):
     *         |x_j|
     *     v = |y_j|
     *         |z_j|
     * </pre>
     *
     * @param v {@link Vector3d} to be copied
     **/
    public Vector3d(Vector3d v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    /**
     * Sets the coordinates of the {@link Vector3d} to the given value,
     * setting it to a copy of the coordinates from another given {@link Vector3d}.
     *
     * @param v {@link Vector3d} to be copied
     * @return the vector on which {@code set} has been called
     */
    public Vector3d set(Vector3d v) {
        x = v.x;
        y = v.y;
        z = v.z;
        return this;
    }

    /**
     * Sets the coordinates of the {@link Vector3d} to the given values.
     *
     * @param x X-Coordinate
     * @param y Y-Coordinate
     * @param z Z-Coordinate
     * @return the vector on which {@code set} has been called
     */
    public Vector3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Sets all entries of the {@link Vector3d} to the given value.
     *
     * @param f given value
     * @return the vector on which {@code set} has been called
     */
    public Vector3d set(double f) {
        x = f;
        y = f;
        z = f;
        return this;
    }

    /**
     * Multiplies a scalar to the {@link Vector3d}.
     * <pre>
     *  v_i.multiply(f):
     *      x_i = x_i * f
     *      y_i = y_i * f
     *      z_i = z_i * f
     * </pre>
     *
     * @param f scalar of type double
     * @return the vector on which {@code multiply} has been called
     */
    public Vector3d multiply(double f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    /**
     * Multiplies a scalar to a {@link Vector3d},
     * storing the multiplication-result into a given vector.
     * <pre>
     *  v_i.multiply(f, v_r):
     *      x_r = x_i * f
     *      y_r = y_i * f
     *      z_r = z_i * f
     * </pre>
     *
     * @param f      scalar of type double
     * @param result the {@link Vector3d} where the multiplication result should be saved to
     * @return the result-vector
     */
    public Vector3d multiply(double f, Vector3d result) {
        result.x = x * f;
        result.y = y * f;
        result.z = z * f;
        return result;
    }

    /**
     * Coordinate-wise multiplication of two {@link Vector3d vectors}.
     * <pre>
     *  v_i.scale(v_j):
     *      x_i = x_i * x_j
     *      y_i = y_i * y_j
     *      z_i = z_i * z_j
     * </pre>
     *
     * @param scale the {@link Vector3d} to be scaled with
     * @return the vector on which {@code scale} has been called
     */
    public Vector3d scale(Vector3d scale) {
        x *= scale.x;
        y *= scale.y;
        z *= scale.z;
        return this;
    }

    /**
     * Coordinate-wise multiplication of two {@link Vector3d vectors},
     * storing the scale-result into a given vector.
     * <pre>
     *  v_i.scale(v_j, v_r):
     *      x_r = x_i * x_j
     *      y_r = y_i * y_j
     *      z_r = z_i * z_j
     * </pre>
     *
     * @param scale  the {@link Vector3d} to be scaled with
     * @param result the {@link Vector3d} where the scale-result should be saved to
     * @return the result-vector
     */
    public Vector3d scale(Vector3d scale, Vector3d result) {
        result.x = x * scale.x;
        result.y = y * scale.y;
        result.z = z * scale.z;
        return result;
    }

    /**
     * Coordinate-wise addition of two {@link Vector3d vectors}.
     * <pre>
     *  v_i.add(v_j):
     *      x_i = x_i + x_j
     *      y_i = y_i + y_j
     *      z_i = z_i + z_j
     * </pre>
     *
     * @param v the {@link Vector3d} to be added
     * @return the vector on which {@code add} has been called
     */
    public Vector3d add(Vector3d v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    /**
     * Coordinate-wise addition of two {@link Vector3d vectors},
     * storing the addition-result into a given vector.
     * <pre>
     *  v_i.add(v_j, v_r):
     *      x_r = x_i + x_j
     *      y_r = y_i + y_j
     *      z_r = z_i + z_j
     * </pre>
     *
     * @param v      the {@link Vector3d} to be added
     * @param result the {@link Vector3d} where the addition-result should be saved to
     * @return the result-vector
     */
    public Vector3d add(Vector3d v, Vector3d result) {
        result.x = x + v.x;
        result.y = y + v.y;
        result.z = z + v.z;
        return result;
    }

    /**
     * Coordinate-wise subtraction of two {@link Vector3d vectors}.
     * <pre>
     *  v_i.subtract(v_j):
     *      x_i = x_i - x_j
     *      y_i = y_i - y_j
     *      z_i = z_i - z_j
     * </pre>
     *
     * @param v the {@link Vector3d} to be subtracted
     * @return the vector on which {@code subtract} has been called
     */
    public Vector3d subtract(Vector3d v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    /**
     * Coordinate-wise subtraction of two {@link Vector3d vectors},
     * storing the subtraction-result into a given vector.
     * <pre>
     *  v_i.subtract(v_j, v_r):
     *      x_r = x_i - x_j
     *      y_r = y_i - y_j
     *      z_r = z_i - z_j
     * </pre>
     *
     * @param v      the {@link Vector3d} to be subtracted
     * @param result the {@link Vector3d} where the subtraction-result should be saved to
     * @return the result-vector
     */
    public Vector3d subtract(Vector3d v, Vector3d result) {
        result.x = x - v.x;
        result.y = y - v.y;
        result.z = z - v.z;
        return result;
    }


    /**
     * The dot product of two {@link Vector3d} resulting in a scalar.
     * <pre>
     *  v_i.dot(v_j): = x_i * x_j + y_i * y_j + z_i * z_j
     * </pre>
     *
     * @param v the {@link Vector3d} to be used for the dot product
     * @return the resulting scalar
     */
    public double dot(Vector3d v) {
        return x * v.x + y * v.y + z * v.z;
    }

    /**
     * The cross-product of two {@link Vector3d},
     * storing the subtraction-result into a given vector.
     * <pre>
     *  v_i.cross(v_j, v_r):
     *      x_r = y_i * z_j - z_i * y_j
     *      y_r = z_i * x_j - x_i * z_j
     *      z_r = x_i * y_j - y_i * x_j
     * </pre>
     *
     * @param v      the {@link Vector3d} to be used for the cross-product
     * @param result the {@link Vector3d} where the cross-product should be saved to
     * @return the resulting {@link Vector3d}
     */
    public Vector3d cross(Vector3d v, Vector3d result) {
        result.x = y * v.z - z * v.y;
        result.y = z * v.x - x * v.z;
        result.z = x * v.y - y * v.x;
        return result;
    }

    /**
     * Calculates and returns the angle between the current and the given vector in radians.
     *
     * @param v the given vector
     * @return angle in radians
     */
    public double angle(Vector3d v) {
        double a = dot(v) / (magnitude() * v.magnitude());
        return Math.acos(a);
    }

    /**
     * Normalizes the {@link Vector3d} by dividing it by its {@link #magnitude()}.
     *
     * @return the normalized {@link Vector3d}
     */
    public Vector3d norm() {
        double mag = magnitude();
        if (!MathUtils.isFuzzyZero(mag)) {
            return multiply(1.0f / mag);
        } else {
            return this;
        }
    }

    /**
     * Normalizes the {@link Vector3d} by dividing it by its {@link #magnitude()},
     * storing the result into a given vector.
     *
     * @param result the {@link Vector3d} where the normalization should be saved to
     * @return the normalized {@link Vector3d}
     */
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

    /**
     * Method to determine the Euclidean distance between the two points represented by two {@link Vector3d Vector3ds},
     * resulting in a scalar value.
     * <pre>
     * v_i.distanceTo(v_j) = sqrt((x_j - x_i)^2 + (y_j - y_i)^2 + (z_j - z_i)^2)
     * </pre>
     *
     * @param v the {@link Vector3d} to which the distance should be determined
     * @return the resulting Euclidean distance
     */
    public double distanceTo(Vector3d v) {
        return Math.sqrt(distanceSqrTo(v));
    }

    /**
     * Method to determine the sqquared Euclidean distance between the two points represented by two {@link Vector3d Vector3ds},
     * resulting in a scalar value.
     * <pre>
     * v_i.distanceSqrTo(v_j) = (x_j - x_i)^2 + (y_j - y_i)^2 + (z_j - z_i)^2
     * </pre>
     *
     * @param v the {@link Vector3d} to which the squared distance should be determined
     * @return the resulting squared Euclidean distance
     */
    public double distanceSqrTo(Vector3d v) {
        double dx = (v.x - x);
        double dy = (v.y - y);
        double dz = (v.z - z);
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Rotates this {@link Vector3d} by the specified angle (in degrees) around the given axis.
     *
     * @param angleDeg Rotation angle in degrees
     * @param axis     Rotation axis
     * @return the rotated {@link Vector3d}
     */
    public Vector3d rotateDeg(double angleDeg, Vector3d axis) {
        return rotate(Math.toRadians(angleDeg), axis.x, axis.y, axis.z);
    }

    /**
     * Rotates this {@link Vector3d} by the specified angle (in radians) around the given axis.
     *
     * @param angle Rotation angle in radians
     * @param axis  Rotation axis
     * @return the rotated {@link Vector3d}
     */
    public Vector3d rotate(double angle, Vector3d axis) {
        return rotate(angle, axis.x, axis.y, axis.z);
    }

    /**
     * Rotates this {@link Vector3d} by the specified angle (in radians) around the given axis.
     *
     * @param angle Rotation angle in radians
     * @param axisX Rotation axis x-component
     * @param axisY Rotation axis y-component
     * @param axisZ Rotation axis z-component
     * @return the rotated {@link Vector3d}
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

    /**
     * Writes the {@link Vector3d} to an array using doubles,
     * given an offset as start-index.
     *
     * @param result the array where the result should be saved to
     * @param offset the start-index for writing
     */
    public void toArray(double[] result, int offset) {
        result[offset] = x;
        result[offset + 1] = y;
        result[offset + 2] = z;
    }

    /**
     * Writes the {@link Vector3d} to an array using floats,
     * given an offset as start-index.
     *
     * @param result the array where the result should be saved to
     * @param offset the start-index for writing
     */
    public void toArray(float[] result, int offset) {
        result[offset] = (float) x;
        result[offset + 1] = (float) y;
        result[offset + 2] = (float) z;
    }

    /**
     * Conversion of {@link Vector3d} to a {@link CartesianPoint} within MOSAIC's coordinate system.
     *
     * @return the resulting {@link CartesianPoint}
     */
    public CartesianPoint toCartesian() {
        return toCartesian(new MutableCartesianPoint());
    }

    /**
     * Conversion of {@link Vector3d} to a {@link CartesianPoint} within MOSAIC's coordinate system,
     * storing the results in a given {@link MutableCartesianPoint}.
     *
     * @param result a {@link MutableCartesianPoint} where the conversion will be saved to
     * @return the resulting {@link CartesianPoint}
     */
    public MutableCartesianPoint toCartesian(MutableCartesianPoint result) {
        return result.set(x, -z, y);
    }

    /**
     * Conversion of {@link Vector3d} to a {@link GeoPoint} using the underlying {@link GeoProjection}.
     *
     * @return the resulting {@link GeoPoint}
     */
    public GeoPoint toGeo() {
        return GeoProjection.getInstance().vectorToGeographic(this);
    }

    /**
     * Projection of {@link Vector3d} to a {@link GeoPoint} using the underlying {@link GeoProjection},
     * storing the result in a given {@link MutableGeoPoint}.
     *
     * @param result the {@link MutableGeoPoint} where the projection will be saved to
     * @return the resulting {@link MutableGeoPoint}
     */
    public MutableGeoPoint toGeo(MutableGeoPoint result) {
        return GeoProjection.getInstance().vectorToGeographic(this, result);
    }

    /**
     * Equal-check for two {@link Vector3d Vector3ds} using {@link MathUtils#isFuzzyEqual(double, double)}.
     *
     * @param other the {@link Vector3d} to be compared
     * @return {@code true} if all coordinates of both {@link Vector3d Vector3ds} are fuzzy-equal, else {@link false}
     */
    public boolean isFuzzyEqual(Vector3d other) {
        return MathUtils.isFuzzyEqual(x, other.x)
                && MathUtils.isFuzzyEqual(y, other.y)
                && MathUtils.isFuzzyEqual(z, other.z);
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
