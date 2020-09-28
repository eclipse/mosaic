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

package org.eclipse.mosaic.lib.spatial;

import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Vector3d;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxTest {

    @Test
    public void creationTest() {
        int minX = -3;
        int maxX = 5;

        int minY = -4;
        int maxY = 7;

        int minZ = -1;
        int maxZ = 2;

        List<Vector3d> points = new ArrayList<>();
        for (int i = minX; i <= maxX; i++) {
            points.add(new Vector3d(i, 0, 0));
        }
        for (int i = minY; i <= maxY; i++) {
            points.add(new Vector3d(0, i, 0));
        }
        for (int i = minZ; i <= maxZ; i++) {
            points.add(new Vector3d(0, 0, i));
        }

        // create from list
        BoundingBox aabb = new BoundingBox();
        Assert.assertTrue(aabb.isEmpty());
        aabb.add(points);
        Assert.assertFalse(aabb.isEmpty());
        Assert.assertTrue(aabb.min.isFuzzyEqual(new Vector3d(minX, minY, minZ)));
        Assert.assertTrue(aabb.max.isFuzzyEqual(new Vector3d(maxX, maxY, maxZ)));

        // create from stream
        aabb = new BoundingBox();
        aabb.add(points.stream());
        Assert.assertFalse(aabb.isEmpty());
        Assert.assertTrue(aabb.min.isFuzzyEqual(new Vector3d(minX, minY, minZ)));
        Assert.assertTrue(aabb.max.isFuzzyEqual(new Vector3d(maxX, maxY, maxZ)));

        // create from other bounding box
        BoundingBox aabbCopy = new BoundingBox();
        aabbCopy.add(aabb);
        Assert.assertFalse(aabb.isEmpty());
        Assert.assertTrue(aabbCopy.min.isFuzzyEqual(new Vector3d(minX, minY, minZ)));
        Assert.assertTrue(aabbCopy.max.isFuzzyEqual(new Vector3d(maxX, maxY, maxZ)));
    }

    @Test
    public void sizeTest() {
        BoundingBox aabb = new BoundingBox();
        Vector3d min = new Vector3d(1, 2, 3);
        Vector3d max = new Vector3d(4, 5, 6);
        aabb.add(min);
        aabb.add(max);

        Vector3d expectedCenter = new Vector3d(min).add(max).multiply(0.5);
        Assert.assertTrue(aabb.center.isFuzzyEqual(expectedCenter));

        Vector3d expectedSize = new Vector3d(max).subtract(min);
        Assert.assertTrue(aabb.size.isFuzzyEqual(expectedSize));
    }

    @Test
    public void containsTest() {
        BoundingBox aabb = new BoundingBox();
        aabb.add(new Vector3d(1, 2, 3));
        aabb.add(new Vector3d(4, 5, 6));

        Assert.assertTrue(aabb.contains(aabb.min));
        Assert.assertTrue(aabb.contains(aabb.max));
        Assert.assertTrue(aabb.contains(aabb.center));

        double e = 0.00000001;

        Assert.assertFalse(aabb.contains(new Vector3d(1 - e, 2, 3)));
        Assert.assertFalse(aabb.contains(new Vector3d(1, 2 - e, 3)));
        Assert.assertFalse(aabb.contains(new Vector3d(1, 2, 3 - e)));

        Assert.assertFalse(aabb.contains(new Vector3d(4 + e, 5, 6)));
        Assert.assertFalse(aabb.contains(new Vector3d(4, 5 + e, 6)));
        Assert.assertFalse(aabb.contains(new Vector3d(4, 5, 6 + e)));
    }

    @Test
    public void distanceTest() {
        BoundingBox aabb = new BoundingBox();
        aabb.add(new Vector3d(0, 0, 0));
        aabb.add(new Vector3d(1, 1, 1));

        Vector3d q = new Vector3d(3, 0, 0);
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceToPoint(q), 2));
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceSqrToPoint(q), 4));
        q = new Vector3d(0, 4, 0);
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceToPoint(q), 3));
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceSqrToPoint(q), 9));
        q = new Vector3d(0, 0, 5);
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceToPoint(q), 4));
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceSqrToPoint(q), 16));

        q = new Vector3d(-2, 0, 0);
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceToPoint(q), 2));
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceSqrToPoint(q), 4));
        q = new Vector3d(0, -3, 0);
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceToPoint(q), 3));
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceSqrToPoint(q), 9));
        q = new Vector3d(0, 0, -4);
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceToPoint(q), 4));
        Assert.assertTrue(MathUtils.isFuzzyEqual(aabb.distanceSqrToPoint(q), 16));
    }

    @Test
    public void rayHitTest() {
        BoundingBox aabb = new BoundingBox();
        aabb.add(new Vector3d(-1, -1, -1));
        aabb.add(new Vector3d(1, 1, 1));

        // origin inside box
        Ray hitRay = new Ray(new Vector3d(), new Vector3d(1, 0, 0));
        Assert.assertEquals(0.0, aabb.hitDistanceSqr(hitRay), 0.0);

        hitRay = new Ray(new Vector3d(-10, 0, 0), new Vector3d(1, 0, 0));
        Assert.assertEquals(9.0, aabb.hitDistance(hitRay), 0.00001);
        hitRay = new Ray(new Vector3d(10, 0, 0), new Vector3d(-1, 0, 0));
        Assert.assertEquals(9.0, aabb.hitDistance(hitRay), 0.00001);

        hitRay = new Ray(new Vector3d(0, -10, 0), new Vector3d(0, 1, 0));
        Assert.assertEquals(9.0, aabb.hitDistance(hitRay), 0.00001);
        hitRay = new Ray(new Vector3d(0, 10, 0), new Vector3d(0, -1, 0));
        Assert.assertEquals(9.0, aabb.hitDistance(hitRay), 0.00001);

        hitRay = new Ray(new Vector3d(0, 0, -10), new Vector3d(0, 0, 1));
        Assert.assertEquals(9.0, aabb.hitDistance(hitRay), 0.00001);
        hitRay = new Ray(new Vector3d(0, 0, 10), new Vector3d(0, 0, -1));
        Assert.assertEquals(9.0, aabb.hitDistance(hitRay), 0.00001);

        hitRay = new Ray(new Vector3d(0, 0, 10), new Vector3d(0, 1, 0));
        Assert.assertEquals(Double.MAX_VALUE, aabb.hitDistance(hitRay), 0.00001);
        Assert.assertEquals(Double.MAX_VALUE, aabb.hitDistanceSqr(hitRay), 0.00001);
    }
}
