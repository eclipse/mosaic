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

public class Matrix4d implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    /**
     * Array holding value of the matrix. Values are stored row-wise, that is, the first 4 values
     * represent the first row, the second 4 values the second row, and so on.<br>
     * Do NOT use this array directly, unless it is from crucial importance (e.g. during physics simulation)
     */
    public final double[] m = new double[16];

    /**
     * Creates a new 4x4 matrix, with all values being zero.
     */
    public Matrix4d() {
        setZero();
    }

    /**
     * Creates a new 4x4 matrix, with the values being copied from the given matrix.
     */
    public Matrix4d(Matrix4d copyFrom) {
        set(copyFrom);
    }

    /**
     * Creates a new identity matrix in 4x4 format.
     */
    public static Matrix4d identityMatrix() {
        return new Matrix4d().setIdentity();
    }

    /**
     * Sets all values to match identity matrix.
     */
    public Matrix4d setIdentity() {
        for (int i = 0; i < 16; i++) {
            m[i] = i % 5 == 0 ? 1 : 0;
        }
        return this;
    }

    /**
     * Sets all values to zero.
     */
    public Matrix4d setZero() {
        for (int i = 0; i < 16; i++) {
            m[i] = 0;
        }
        return this;
    }

    /**
     * Copies the values from the given matrix to this matrix.
     */
    public Matrix4d set(Matrix4d mat) {
        System.arraycopy(mat.m, 0, m, 0, 16);
        return this;
    }

    /**
     * Get a specific value from the matrix by row and column index
     */
    public double get(int row, int col) {
        return m[row * 4 + col];
    }

    /**
     * Sets a specific value in the matrix by row and column index
     */
    public Matrix4d set(int row, int col, double value) {
        m[row * 4 + col] = value;
        return this;
    }

    /**
     * Adds a matrix to this matrix.
     */
    public Matrix4d add(Matrix4d mat) {
        return add(mat, this);
    }

    /**
     * Adds a matrix to this matrix and writes the result into the result matrix.
     */
    public Matrix4d add(Matrix4d mat, Matrix4d result) {
        for (int i = 0; i < 16; i++) {
            result.m[i] += mat.m[i];
        }
        return result;
    }

    /**
     * Multiplies this matrix with another matrix and writes the result into the result matrix.
     */
    public Matrix4d multiply(Matrix4d mat, Matrix4d result) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double x = 0;
                for (int k = 0; k < 4; k++) {
                    x += m[j + k * 4] * mat.m[i * 4 + k];
                }
                result.m[i * 4 + j] = x;
            }
        }
        return result;
    }

    /**
     * Transposes this matrix.
     */
    public Matrix4d transpose() {
        return transpose(this);
    }

    /**
     * Writes a transposed version of this matrix into the result matrix.
     */
    public Matrix4d transpose(Matrix4d result) {
        result.set(this);
        for (int m = 0; m < 4; m++) {
            for (int n = m + 1; n < 4; n++) {
                result.swap(m * 4 + n, n * 4 + m);
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
    public boolean inverse(Matrix4d result) {
        // Invert a 4 x 4 matrix using Cramer's Rule

        // transpose matrix
        final double src0 = m[0];
        final double src4 = m[1];
        final double src8 = m[2];
        final double src12 = m[3];

        final double src1 = m[4];
        final double src5 = m[5];
        final double src9 = m[6];
        final double src13 = m[7];

        final double src2 = m[8];
        final double src6 = m[9];
        final double src10 = m[10];
        final double src14 = m[11];

        final double src3 = m[12];
        final double src7 = m[13];
        final double src11 = m[14];
        final double src15 = m[15];

        // calculate pairs for first 8 elements (cofactors)
        final double atmp0 = src10 * src15;
        final double atmp1 = src11 * src14;
        final double atmp2 = src9 * src15;
        final double atmp3 = src11 * src13;
        final double atmp4 = src9 * src14;
        final double atmp5 = src10 * src13;
        final double atmp6 = src8 * src15;
        final double atmp7 = src11 * src12;
        final double atmp8 = src8 * src14;
        final double atmp9 = src10 * src12;
        final double atmp10 = src8 * src13;
        final double atmp11 = src9 * src12;

        // calculate first 8 elements (cofactors)
        final double dst0 = (atmp0 * src5 + atmp3 * src6 + atmp4 * src7)
                - (atmp1 * src5 + atmp2 * src6 + atmp5 * src7);
        final double dst1 = (atmp1 * src4 + atmp6 * src6 + atmp9 * src7)
                - (atmp0 * src4 + atmp7 * src6 + atmp8 * src7);
        final double dst2 = (atmp2 * src4 + atmp7 * src5 + atmp10 * src7)
                - (atmp3 * src4 + atmp6 * src5 + atmp11 * src7);
        final double dst3 = (atmp5 * src4 + atmp8 * src5 + atmp11 * src6)
                - (atmp4 * src4 + atmp9 * src5 + atmp10 * src6);
        final double dst4 = (atmp1 * src1 + atmp2 * src2 + atmp5 * src3)
                - (atmp0 * src1 + atmp3 * src2 + atmp4 * src3);
        final double dst5 = (atmp0 * src0 + atmp7 * src2 + atmp8 * src3)
                - (atmp1 * src0 + atmp6 * src2 + atmp9 * src3);
        final double dst6 = (atmp3 * src0 + atmp6 * src1 + atmp11 * src3)
                - (atmp2 * src0 + atmp7 * src1 + atmp10 * src3);
        final double dst7 = (atmp4 * src0 + atmp9 * src1 + atmp10 * src2)
                - (atmp5 * src0 + atmp8 * src1 + atmp11 * src2);

        // calculate pairs for second 8 elements (cofactors)
        final double btmp0 = src2 * src7;
        final double btmp1 = src3 * src6;
        final double btmp2 = src1 * src7;
        final double btmp3 = src3 * src5;
        final double btmp4 = src1 * src6;
        final double btmp5 = src2 * src5;
        final double btmp6 = src0 * src7;
        final double btmp7 = src3 * src4;
        final double btmp8 = src0 * src6;
        final double btmp9 = src2 * src4;
        final double btmp10 = src0 * src5;
        final double btmp11 = src1 * src4;

        // calculate second 8 elements (cofactors)
        final double dst8 = (btmp0 * src13 + btmp3 * src14 + btmp4 * src15)
                - (btmp1 * src13 + btmp2 * src14 + btmp5 * src15);
        final double dst9 = (btmp1 * src12 + btmp6 * src14 + btmp9 * src15)
                - (btmp0 * src12 + btmp7 * src14 + btmp8 * src15);
        final double dst10 = (btmp2 * src12 + btmp7 * src13 + btmp10 * src15)
                - (btmp3 * src12 + btmp6 * src13 + btmp11 * src15);
        final double dst11 = (btmp5 * src12 + btmp8 * src13 + btmp11 * src14)
                - (btmp4 * src12 + btmp9 * src13 + btmp10 * src14);
        final double dst12 = (btmp2 * src10 + btmp5 * src11 + btmp1 * src9)
                - (btmp4 * src11 + btmp0 * src9 + btmp3 * src10);
        final double dst13 = (btmp8 * src11 + btmp0 * src8 + btmp7 * src10)
                - (btmp6 * src10 + btmp9 * src11 + btmp1 * src8);
        final double dst14 = (btmp6 * src9 + btmp11 * src11 + btmp3 * src8)
                - (btmp10 * src11 + btmp2 * src8 + btmp7 * src9);
        final double dst15 = (btmp10 * src10 + btmp4 * src8 + btmp9 * src9)
                - (btmp8 * src9 + btmp11 * src10 + btmp5 * src8);

        // calculate determinant
        final double det = src0 * dst0 + src1 * dst1 + src2 * dst2 + src3 * dst3;
        if (det == 0.0f) {
            return false;
        }

        // calculate matrix inverse
        final double invdet = 1.0f / det;
        result.m[0] = dst0 * invdet;
        result.m[1] = dst1 * invdet;
        result.m[2] = dst2 * invdet;
        result.m[3] = dst3 * invdet;

        result.m[4] = dst4 * invdet;
        result.m[5] = dst5 * invdet;
        result.m[6] = dst6 * invdet;
        result.m[7] = dst7 * invdet;

        result.m[8] = dst8 * invdet;
        result.m[9] = dst9 * invdet;
        result.m[10] = dst10 * invdet;
        result.m[11] = dst11 * invdet;

        result.m[12] = dst12 * invdet;
        result.m[13] = dst13 * invdet;
        result.m[14] = dst14 * invdet;
        result.m[15] = dst15 * invdet;

        return true;
    }

    /**
     * Creates copy of this matrix.
     */
    public Matrix4d copy() {
        return new Matrix4d(this);
    }

    public boolean isFuzzyEqual(Matrix4d other) {
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

        final Matrix4d other = (Matrix4d) o;
        return Arrays.equals(m, other.m);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(m);
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder(this.getClass().getSimpleName()).append("([");
        for (int r = 0; r < 4; r++) {
            out.append("[");
            for (int c = 0; c < 4; c++) {
                out.append(FORMAT.format(get(r, c)));
                if (c < 3) {
                    out.append(", ");
                }
            }
            out.append("]");
            if (r < 3) {
                out.append(", ");
            }
        }
        return out.append("])").toString();
    }

}