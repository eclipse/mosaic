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

import org.junit.Assert;
import org.junit.Test;

public class Vector3dTest {

    @Test
    public void createTest() {
        Vector3d v = new Vector3d();
        Assert.assertEquals(v.x, 0, 0);
        Assert.assertEquals(v.y, 0, 0);
        Assert.assertEquals(v.z, 0, 0);

        v = new Vector3d(1, 2, 3);
        Assert.assertEquals(v.x, 1, 0);
        Assert.assertEquals(v.y, 2, 0);
        Assert.assertEquals(v.z, 3, 0);

        v = new Vector3d(v);
        Assert.assertEquals(v.x, 1, 0);
        Assert.assertEquals(v.y, 2, 0);
        Assert.assertEquals(v.z, 3, 0);
    }

    @Test
    public void setTest() {
        Vector3d v = new Vector3d().set(1);
        Assert.assertEquals(v, new Vector3d(1, 1, 1));

        v = new Vector3d().set(1, 2, 3);
        Assert.assertEquals(v, new Vector3d(1, 2, 3));

        v = new Vector3d().set(new Vector3d(1, 2, 3));
        Assert.assertEquals(v, new Vector3d(1, 2, 3));
    }

    @Test
    public void multiplyTest() {
        Vector3d v = new Vector3d(1, 1, 1);
        Vector3d r = new Vector3d();
        v.multiply(2);
        Assert.assertEquals(v, new Vector3d(2, 2, 2));

        v = new Vector3d(1, 1, 1);
        v.multiply(2, r);
        Assert.assertEquals(v, new Vector3d(1, 1, 1));
        Assert.assertEquals(r, new Vector3d(2, 2, 2));
    }

    @Test
    public void scaleTest() {
        Vector3d s = new Vector3d(2, 3, 4);
        Vector3d v = new Vector3d(2, 2, 2);
        Vector3d r = new Vector3d();

        v.scale(s);
        Assert.assertEquals(v, new Vector3d(4, 6, 8));

        v = new Vector3d(2, 2, 2);
        v.scale(s, r);
        Assert.assertEquals(v, new Vector3d(2, 2, 2));
        Assert.assertEquals(r, new Vector3d(4, 6, 8));
    }

    @Test
    public void addTest() {
        Vector3d v = new Vector3d(1, 1, 1);
        Vector3d a = new Vector3d(2, 3, 4);
        Vector3d r = new Vector3d();

        v.add(a, r);
        Assert.assertEquals(v, new Vector3d(1, 1, 1));
        Assert.assertEquals(r, new Vector3d(3, 4, 5));

        v.add(a);
        Assert.assertEquals(v, new Vector3d(3, 4, 5));
    }

    @Test
    public void subtractTest() {
        Vector3d v = new Vector3d(5, 5, 5);
        Vector3d a = new Vector3d(2, 3, 4);
        Vector3d r = new Vector3d();

        v.subtract(a, r);
        Assert.assertEquals(v, new Vector3d(5, 5, 5));
        Assert.assertEquals(r, new Vector3d(3, 2, 1));

        v.subtract(a);
        Assert.assertEquals(v, new Vector3d(3, 2, 1));
    }

    @Test
    public void dotTest() {
        Assert.assertEquals(new Vector3d(1, 0, 0).dot(new Vector3d(1, 0, 0)), 1, 0);
        Assert.assertEquals(new Vector3d(1, 0, 0).dot(new Vector3d(-1, 0, 0)), -1, 0);
        Assert.assertEquals(new Vector3d(1, 0, 0).dot(new Vector3d(0, 1, 0)), 0, 0);

        Vector3d a = new Vector3d(17.3, 12.5, 8.9);
        Vector3d b = new Vector3d(5.3, 9.5, 2.8);
        Assert.assertTrue(MathUtils.isFuzzyEqual(a.dot(b), a.x * b.x + a.y * b.y + a.z * b.z));
    }

    @Test
    public void crossTest() {
        Vector3d a = new Vector3d(1, 0, 0);
        Vector3d b = new Vector3d(0, 0, -1);
        Vector3d r = a.cross(b, new Vector3d());

        Assert.assertEquals(a, new Vector3d(1, 0, 0));
        Assert.assertEquals(b, new Vector3d(0, 0, -1));
        Assert.assertEquals(r, new Vector3d(0, 1, 0));
    }

    @Test
    public void lengthTest() {
        Assert.assertEquals(new Vector3d(0, 0, 0).magnitude(), 0, MathUtils.EPSILON_D);
        Assert.assertEquals(new Vector3d(1, 0, 0).magnitude(), 1, MathUtils.EPSILON_D);
        Assert.assertEquals(new Vector3d(0, 1, 0).magnitude(), 1, MathUtils.EPSILON_D);
        Assert.assertEquals(new Vector3d(0, 0, 1).magnitude(), 1, MathUtils.EPSILON_D);
        Assert.assertEquals(new Vector3d(12, 13, 14).magnitude(), Math.sqrt(12*12 + 13*13 + 14*14), MathUtils.EPSILON_D);
    }

    @Test
    public void normTest() {
        Assert.assertEquals(new Vector3d(17, 0, 0).norm(), new Vector3d(1, 0, 0));
        Assert.assertEquals(new Vector3d(0, 17, 0).norm(), new Vector3d(0, 1, 0));
        Assert.assertEquals(new Vector3d(0, 0, 17).norm(), new Vector3d(0, 0, 1));
        Assert.assertEquals(new Vector3d(12, 51, 17).norm().magnitude(), 1, MathUtils.EPSILON_D);
        Assert.assertEquals(new Vector3d(0, 0, 0).norm(), new Vector3d(0, 0, 0));

        Vector3d v = new Vector3d(12, 51, 17);
        Vector3d n = v.norm(new Vector3d());
        Assert.assertEquals(v, new Vector3d(12, 51, 17));
        Assert.assertEquals(n.magnitude(), 1, MathUtils.EPSILON_D);
    }

    @Test
    public void distanceTest() {
        Assert.assertEquals(new Vector3d().distanceTo(new Vector3d(2, 0, 0)), 2, MathUtils.EPSILON_D);
        Assert.assertEquals(new Vector3d().distanceTo(new Vector3d(0, 3, 0)), 3, MathUtils.EPSILON_D);
        Assert.assertEquals(new Vector3d().distanceTo(new Vector3d(0, 0, 4)), 4, MathUtils.EPSILON_D);
    }

    @Test
    public void rotateTest() {
        Vector3d v = new Vector3d(1, 0, 0).rotateDeg(90, new Vector3d(0, 1, 0));
        Assert.assertTrue(v.isFuzzyEqual(new Vector3d(0, 0, -1)));
        v = new Vector3d(1, 0, 0).rotateDeg(90, new Vector3d(0, 0, 1));
        Assert.assertTrue(v.isFuzzyEqual(new Vector3d(0, 1, 0)));
        v = new Vector3d(0, 1, 0).rotateDeg(90, new Vector3d(1, 0, 0));
        Assert.assertTrue(v.isFuzzyEqual(new Vector3d(0, 0, 1)));

        Vector3d a = new Vector3d(1, 0, 0);
        Vector3d b = new Vector3d(1, 0, 0);
        for (int angle = 0; angle < 180; angle += 10) {
            b.set(a);
            b.rotate(Math.toRadians(angle), new Vector3d(0, 1, 0));
            Assert.assertEquals(Math.toDegrees(a.angle(b)), angle, MathUtils.EPSILON_D);
        }
    }
}
