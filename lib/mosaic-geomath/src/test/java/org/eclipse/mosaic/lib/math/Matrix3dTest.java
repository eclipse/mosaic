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

import static org.junit.Assert.fail;

import org.eclipse.mosaic.lib.spatial.TransformationMatrix;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Matrix3dTest {

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
    public void transposeRotationMatrix() {
        final TransformationMatrix m = new TransformationMatrix();
        m.rotate(37, new Vector3d(0,1,0));


        Matrix3d rotation = m.getRotation();
        System.out.println(rotation);
        Matrix3d inverse = new Matrix3d();
        rotation.inverse(inverse);
        Matrix3d transposed = new Matrix3d();
        rotation.transpose(transposed);

        assertEquals(inverse, transposed);
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

}
