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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

public class Matrix3d implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    /**
     * Array holding value of the matrix. Values are stored column-wise, that is, the first 3 values
     * represent the first column, the second 3 values the second column, and so on.<br>
     * Do NOT use this array directly, unless it is from crucial importance (e.g. during physics simulation)
     */
    public final double[] m = new double[9];

    /**
     * Creates a new 3x3 matrix, with all values being zero.
     */
    public Matrix3d() {
        setZero();
    }

    /**
     * Creates a new 3x3 matrix, with the values being copied from the given matrix.
     */
    public Matrix3d(Matrix3d copyFrom) {
        set(copyFrom);
    }

    /**
     * Creates a new identity matrix in 3x3 format.
     */
    public static Matrix3d identityMatrix() {
        return new Matrix3d().setIdentity();
    }

    /**
     * Sets all values to match identity matrix.
     */
    public Matrix3d setIdentity() {
        for (int i = 0; i < 9; i++) {
            m[i] = i % 4 == 0 ? 1 : 0;
        }
        return this;
    }

    /**
     * Sets all values to zero.
     */
    public Matrix3d setZero() {
        for (int i = 0; i < 9; i++) {
            m[i] = 0;
        }
        return this;
    }

    /**
     * Copies the values from the given matrix to this matrix.
     */
    public Matrix3d set(Matrix3d m) {
        System.arraycopy(m.m, 0, this.m, 0, 9);
        return this;
    }

    /**
     * Get a specific value from the matrix by row and column index
     */
    public double get(int row, int col) {
        return m[col * 3 + row];
    }

    /**
     * Sets a specific value in the matrix by row and column index
     */
    public Matrix3d set(int row, int col, double value) {
        m[col * 3 + row] = value;
        return this;
    }


    /**
     * Adds a matrix to this matrix.
     */
    public Matrix3d add(Matrix3d mat) {
        return add(mat, this);
    }

    /**
     * Adds a matrix to this matrix and writes the result into the result matrix.
     */
    public Matrix3d add(Matrix3d mat, Matrix3d result) {
        for (int i = 0; i < 16; i++) {
            result.m[i] += mat.m[i];
        }
        return result;
    }

    /**
     * Multiplies this matrix with another matrix and writes the result into the result matrix.
     */
    public Matrix3d multiply(Matrix3d mat, Matrix3d result) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double x = 0;
                for (int k = 0; k < 3; k++) {
                    x += m[j + k * 3] * mat.m[i * 3 + k];
                }
                result.m[i * 3 + j] = x;
            }
        }
        return result;
    }

    /**
     * Multiplies this matrix with a 3-dimensional vector.
     */
    public Vector3d multiply(Vector3d v) {
        double x = v.x * m[0] + v.y * m[1] + v.z * m[2];
        double y = v.x * m[3] + v.y * m[4] + v.z * m[5];
        double z = v.x * m[6] + v.y * m[7] + v.z * m[8];
        v.set(x, y, z);
        return v;
    }

    /**
     * Multiplies this matrix with a 3-dimensional vector and writes the result into the given vector.
     */
    public Vector3d multiply(Vector3d v, Vector3d result) {
        result.x = v.x * m[0] + v.y * m[1] + v.z * m[2];
        result.y = v.x * m[3] + v.y * m[4] + v.z * m[5];
        result.z = v.x * m[6] + v.y * m[7] + v.z * m[8];
        return result;
    }

    /**
     * Transposes this matrix.
     */
    public Matrix3d transpose() {
        return transpose(this);
    }

    /**
     * Writes a transposed version of this matrix into the result matrix.
     */
    public Matrix3d transpose(Matrix3d result) {
        result.set(this);
        for (int r = 0; r < 3; r++) {
            for (int c = r + 1; c < 3; c++) {
                result.swap(c * 3 + r, r * 3 + c);
            }
        }
        return result;
    }

    private void swap(int i, int j) {
        double d = m[i];
        m[i] = m[j];
        m[j] = d;
    }

    /**
     * Inverts this matrix.
     *
     * @return <code>true</code>, if the inverse could be created.
     */
    public boolean invert() {
        return inverse(this);
    }

    /**
     * Writes a inverted version of this matrix into the result matrix.
     *
     * @return <code>true</code>, if the inverse could be created.
     */
    public boolean inverse(Matrix3d result) {
        // Invert a 3 x 3 matrix

        double m00 = m[0];
        double m01 = m[3];
        double m02 = m[6];
        double m10 = m[1];
        double m11 = m[4];
        double m12 = m[7];
        double m20 = m[2];
        double m21 = m[5];
        double m22 = m[8];

        double det = m00 * (m11 * m22 - m21 * m12) -
                m01 * (m10 * m22 - m12 * m20) +
                m02 * (m10 * m21 - m11 * m20);

        if (det == 0.0f) {
            return false;
        }

        double invdet = 1 / det;

        double r00 = (m11 * m22 - m21 * m12) * invdet;
        double r01 = (m02 * m21 - m01 * m22) * invdet;
        double r02 = (m01 * m12 - m02 * m11) * invdet;
        double r10 = (m12 * m20 - m10 * m22) * invdet;
        double r11 = (m00 * m22 - m02 * m20) * invdet;
        double r12 = (m10 * m02 - m00 * m12) * invdet;
        double r20 = (m10 * m21 - m20 * m11) * invdet;
        double r21 = (m20 * m01 - m00 * m21) * invdet;
        double r22 = (m00 * m11 - m10 * m01) * invdet;

        result.m[0] = r00;
        result.m[3] = r01;
        result.m[6] = r02;
        result.m[1] = r10;
        result.m[4] = r11;
        result.m[7] = r12;
        result.m[2] = r20;
        result.m[5] = r21;
        result.m[8] = r22;

        return true;
    }

    /**
     * Creates copy of this matrix.
     */
    public Matrix3d copy() {
        return new Matrix3d(this);
    }

    public boolean isFuzzyEqual(Matrix3d other) {
        for (int i = 0; i < m.length; i++) {
            if (!MathUtils.isFuzzyEqual(m[i], other.m[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Matrix3d other = (Matrix3d) o;
        return Arrays.equals(m, other.m);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(m);
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder(this.getClass().getSimpleName()).append("([");
        for (int r = 0; r < 3; r++) {
            out.append("[");
            for (int c = 0; c < 3; c++) {
                out.append(FORMAT.format(get(r, c)));
                if (c < 2) {
                    out.append(", ");
                }
            }
            out.append("]");
            if (r < 2) {
                out.append(", ");
            }
        }
        return out.append("])").toString();
    }
}