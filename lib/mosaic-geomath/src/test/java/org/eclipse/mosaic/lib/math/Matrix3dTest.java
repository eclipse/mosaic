/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Matrix3dTest {

    @Test
    public void addMatrix() {
        final Matrix3d a = new Matrix3d();
        set(a, "[5, -3, 11], [5, 6, 22], [8, 19, 0]");

        final Matrix3d b = new Matrix3d();
        set(b, "[12, 3, -4], [5, 16, 9], [8, -3, 4]");

        final Matrix3d expected = new Matrix3d();
        set(expected, "[17, 0, 7], [10, 22, 31], [16, 16, 4]");

        Matrix3d actual = a.add(b, new Matrix3d());
        assertEquals(expected, actual);
    }

    @Test
    public void subtractMatrix() {
        final Matrix3d a = new Matrix3d();
        set(a, "[5, -3, 11], [5, 6, 22], [8, 19, 0]");

        final Matrix3d b = new Matrix3d();
        set(b, "[12, 3, -4], [5, 16, 9], [8, -3, 4]");

        final Matrix3d expected = new Matrix3d();
        set(expected, "[-7, -6, 15], [0, -10, 13], [0, 22, -4]");

        Matrix3d actual = a.subtract(b, new Matrix3d());
        assertEquals(expected, actual);
    }

    @Test
    public void multiplyMatrix() {
        final Matrix3d a = new Matrix3d();
        set(a, "[5, -3, 11], [5, 6, 22], [8, 19, 0]");

        final Matrix3d b = new Matrix3d();
        set(b, "[12, 3, -4], [5, 16, 9], [8, -3, 4]");

        final Matrix3d expected = new Matrix3d();
        set(expected, "[133, -66, -3], [266, 45, 122], [191, 328, 139]");

        Matrix3d actual = a.multiply(b, new Matrix3d());
        assertEquals(expected, actual);
    }

    @Test
    public void multiplyVector() {
        final Matrix3d a = new Matrix3d();
        set(a, "[2, 5, 7], [8, 9, 10], [1, 2, 3]");

        final Vector3d v = new Vector3d(2,4,5);

        final Vector3d expected = new Vector3d(59, 102, 25);

        Vector3d actual = a.multiply(v, new Vector3d());
        assertEquals(expected, actual);
    }

    @Test
    public void transpose() {
        final Matrix3d mExpected = new Matrix3d().setIdentity();
        set(mExpected, "[1, 2, 3], [8, 6, 7], [-9, -4, 5]");

        final Matrix3d mTransposedExpected = new Matrix3d();
        set(mTransposedExpected, "[1, 8, -9], [2, 6, -4], [3, 7, 5]");

        Matrix3d m = new Matrix3d(mExpected);

        Matrix3d mTransposed = m.transpose(new Matrix3d());
        assertEquals(mTransposed, mTransposedExpected);
        assertEquals(m, mExpected); //m remains untouched

        Matrix3d mTransposedTransposed = mTransposed.transpose(new Matrix3d());
        assertEquals(mExpected, mTransposedTransposed); //double transpose results in original matrix

        m.transpose();
        assertEquals(m, mTransposedExpected); //m
    }

    @Test
    public void inverse() {
        final Matrix3d m = new Matrix3d();
        set(m, "[1, 2, 3], [8, 6, 7], [-9, -4, 5]");

        final Matrix3d mInvExpected = new Matrix3d();
        set(mInvExpected, "[-0.707317073, 0.268292683, 0.048780488], [1.256097561, -0.390243902, -0.207317073], [-0.268292683, 0.170731707, 0.121951220]");

        final Matrix3d mInverted = new Matrix3d();
        m.inverse(mInverted);
        assertEquals(mInverted, mInvExpected);

        final Matrix3d multiplied = m.multiply(mInverted, new Matrix3d());
        assertEquals(Matrix3d.identityMatrix(), multiplied);
    }

    @Test
    public void getAsArray() {
        final Matrix3d m = new Matrix3d();
        set(m, "[1, 2, 3], [8, 6, 7], [-9, -4, 5]");

        float[] resultf = m.getAsFloatArray(new float[9], MatrixElementOrder.ROW_MAJOR);
        assertTrue(Arrays.equals(new float[] {1f, 2f, 3f, 8f, 6f, 7f, -9f, -4f, 5f}, resultf));

        resultf = m.getAsFloatArray(new float[9], MatrixElementOrder.COLUMN_MAJOR);
        assertTrue(Arrays.equals(new float[] {1f, 8f, -9f, 2f, 6f, -4f, 3f, 7f, 5f}, resultf));

        double[] resultd = m.getAsDoubleArray(new double[9], MatrixElementOrder.ROW_MAJOR);
        assertTrue(Arrays.equals(new double[] {1d, 2d, 3d, 8d, 6d, 7d, -9d, -4d, 5d}, resultd));

        resultd = m.getAsDoubleArray(new double[9], MatrixElementOrder.COLUMN_MAJOR);
        assertTrue(Arrays.equals(new double[] {1d, 8d, -9d, 2d, 6d, -4d, 3d, 7d, 5d}, resultd));
    }

    @Test
    public void setArray() {
        final Matrix3d expected = new Matrix3d();
        set(expected, "[1, 2, 3], [8, 6, 7], [-9, -4, 5]");

        Matrix3d m = new Matrix3d();
        m.set(new float[] {1f, 2f, 3f, 8f, 6f, 7f, -9f, -4f, 5f}, MatrixElementOrder.ROW_MAJOR);
        assertEquals(expected, m);

        m.set(new double[] {1d, 2d, 3d, 8d, 6d, 7d, -9d, -4d, 5d}, MatrixElementOrder.ROW_MAJOR);
        assertEquals(expected, m);

        m.set(new float[] {1f, 8f, -9f, 2f, 6f, -4f, 3f, 7f, 5f}, MatrixElementOrder.COLUMN_MAJOR);
        assertEquals(expected, m);

        m.set(new double[] {1d, 8d, -9d, 2d, 6d, -4d, 3d, 7d, 5d}, MatrixElementOrder.COLUMN_MAJOR);
        assertEquals(expected, m);
    }

    private void set(Matrix3d m, String matrix) {
        Matcher matcher = Pattern.compile("([0-9\\-.]+)").matcher(matrix);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (matcher.find()) {
                    m.set(r, c, Double.parseDouble(matcher.group(1)));
                }
            }
        }
    }

    private static void assertEquals(Matrix3d expected, Matrix3d actual) {
        if(!expected.isFuzzyEqual(actual)) {
            fail(String.format("Matrix not fuzzy equal.%nExpected: %s%nActual: %s", expected, actual));
        }
    }

    private static void assertEquals(Vector3d expected, Vector3d actual) {
        if(!expected.isFuzzyEqual(actual)) {
            fail(String.format("Vector3d not fuzzy equal.%nExpected: %s%nActual: %s", expected, actual));
        }
    }

}
