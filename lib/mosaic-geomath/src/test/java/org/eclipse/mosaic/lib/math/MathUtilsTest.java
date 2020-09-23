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

package org.eclipse.mosaic.lib.math;

import org.junit.Assert;
import org.junit.Test;

public class MathUtilsTest {

    @Test
    public void fuzzyEqualTest() {
        Assert.assertTrue(MathUtils.isFuzzyEqual(1.0, 1.000000001));
        Assert.assertFalse(MathUtils.isFuzzyEqual(1.0, 1.0000001));

        Assert.assertTrue(MathUtils.isFuzzyEqual(1.0f, 1.0000001f));
        Assert.assertFalse(MathUtils.isFuzzyEqual(1.0f, 1.0001f));

        Assert.assertTrue(MathUtils.isFuzzyEqual(1000000.0, 1000000.001));
        Assert.assertFalse(MathUtils.isFuzzyEqual(1000000.0, 1000000.1));

        Assert.assertTrue(MathUtils.isFuzzyEqual(1000000f, 1000001f));
        Assert.assertFalse(MathUtils.isFuzzyEqual(1000000f, 1000100f));

        Assert.assertTrue(MathUtils.isFuzzyEqual(0.0, MathUtils.EPSILON_D));
        double eps3d = Math.pow(MathUtils.EPSILON_D, 3);
        Assert.assertTrue(MathUtils.isFuzzyEqual(MathUtils.EPSILON_D - eps3d, MathUtils.EPSILON_D + eps3d));

        Assert.assertTrue(MathUtils.isFuzzyEqual(0f, MathUtils.EPSILON_F));
        float eps3f = (float) Math.pow(MathUtils.EPSILON_F, 3);
        Assert.assertTrue(MathUtils.isFuzzyEqual(MathUtils.EPSILON_F - eps3f, MathUtils.EPSILON_F + eps3f));
    }

    @Test
    public void wrapAngleTest() {
        // test angles inside range -PI..PI
        for (int i = -9; i >= 9; i++) {
            double ang = Math.PI / 20 * i;
            Assert.assertEquals(ang, MathUtils.wrapAnglePiPi(ang), 0.0);
        }
        // test edges
        Assert.assertEquals(-Math.PI, MathUtils.wrapAnglePiPi(-Math.PI), 0.0);
        Assert.assertEquals(Math.PI, MathUtils.wrapAnglePiPi(Math.PI), 0.0);

        // test angle wrapping
        Assert.assertEquals(Math.toRadians(-170), MathUtils.wrapAnglePiPi(Math.toRadians(190)), 1e-9);
        Assert.assertEquals(Math.toRadians(-43), MathUtils.wrapAnglePiPi(Math.toRadians(180 + 360*1337 + 137)), 1e-9);

        Assert.assertEquals(Math.toRadians(170), MathUtils.wrapAnglePiPi(Math.toRadians(-190)), 1e-9);
        Assert.assertEquals(Math.toRadians(163), MathUtils.wrapAnglePiPi(Math.toRadians(-180 - 360*4711 - 17)), 1e-9);
    }

}
