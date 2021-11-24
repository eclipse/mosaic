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

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Matrix4dTest {

    @Test
    public void transpose() {
        final Matrix4d mExpected = new Matrix4d().setIdentity();
        set(mExpected, "[1, 2, 3, 4], [8, 6, 7, 2], [-9, -4, 5, 1], [13, 0, 2 -1]");

        final Matrix4d mTransposedExpected = new Matrix4d();
        set(mTransposedExpected, "[1, 8, -9, 13], [2, 6, -4, 0], [3, 7, 5, 2], [4, 2, 1, -1]");

        Matrix4d m = new Matrix4d(mExpected);

        Matrix4d mTransposed = m.transpose(new Matrix4d());
        assertEquals(mTransposedExpected, mTransposed);
        assertEquals(mExpected, m); //m remains untouched

        Matrix4d mTransposedTransposed = mTransposed.transpose(new Matrix4d());
        assertEquals(mExpected, mTransposedTransposed); //double transpose results in original matrix

        m.transpose();
        assertEquals(mTransposedExpected, m); //m
    }

    @Test
    public void inverse() {
        final Matrix4d m = new Matrix4d();
        set(m, "[1, 2, 3, 4], [8, 6, 7, 2], [-9, -4, 5, 1], [13, 0, 2 -1]");

        final Matrix4d mInvExpected = new Matrix4d();
        set(mInvExpected, "[0.0356846473, -0.0240663900, -0.0182572614, 0.0763485477], [-0.0804979253, 0.1356846473, -0.0867219917, -0.1373443983], [-0.0663900415, 0.0912863071, 0.1037344398, 0.0207468880], [0.3311203320, -0.1302904564, -0.0298755187, 0.0340248963]");

        final Matrix4d mInverted = new Matrix4d();
        m.inverse(mInverted);
        assertEquals(mInvExpected, mInverted);

        final Matrix4d multiplied = m.multiply(mInverted, new Matrix4d());
        assertEquals(Matrix4d.identityMatrix(), multiplied);
    }

    private void set(Matrix4d m, String matrix) {
        Matcher matcher = Pattern.compile("([0-9\\-.]+)").matcher(matrix);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (matcher.find()) {
                    m.set(r, c, Double.parseDouble(matcher.group(1)));
                }
            }
        }
    }

    private static void assertEquals(Matrix4d expected, Matrix4d actual) {
        if (!expected.isFuzzyEqual(actual)) {
            fail(String.format("Matrix not fuzzy equal.%nExpected: %s%nActual:%s", expected, actual));
        }
    }

}
