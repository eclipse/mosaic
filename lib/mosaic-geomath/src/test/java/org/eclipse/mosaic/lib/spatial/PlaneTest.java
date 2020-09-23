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

package org.eclipse.mosaic.lib.spatial;

import org.eclipse.mosaic.lib.math.Vector3d;

import org.junit.Assert;
import org.junit.Test;

public class PlaneTest {

    @Test
    public void pointTest() {
        Plane plane = new Plane(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0));
        Assert.assertTrue(plane.contains(plane.p));
        Assert.assertTrue(plane.contains(new Vector3d(17, 0, 12)));

        Vector3d above = new Vector3d(17, 1, 12);
        Vector3d below = new Vector3d(17, -1, 12);
        Assert.assertTrue(plane.isAbove(above));
        Assert.assertFalse(plane.contains(above));
        Assert.assertFalse(plane.isAbove(below));
        Assert.assertFalse(plane.contains(below));
    }

    @Test
    public void intersectTest() {
        Plane plane = new Plane(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0));
        Vector3d intersectPt = new Vector3d();

        Ray hit = new Ray(new Vector3d(2, 1, 4), new Vector3d(0, -1, 0));
        Assert.assertTrue(plane.computeIntersectionPoint(hit, intersectPt));
        Assert.assertTrue(intersectPt.isFuzzyEqual(new Vector3d(2, 0, 4)));

        Ray noHit = new Ray(new Vector3d(2, 1, 4), new Vector3d(0, 1, 0));
        Assert.assertFalse(plane.computeIntersectionPoint(noHit, intersectPt));
    }

    @Test
    public void intersectTestParallel() {
        Plane plane = new Plane(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0));
        Vector3d intersectPt = new Vector3d();

        Ray parallelAbove = new Ray(new Vector3d(0, 1, 0), new Vector3d(1, 0, 0));
        Assert.assertFalse(plane.computeIntersectionPoint(parallelAbove, intersectPt));

        Ray parallelInside = new Ray(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
        Assert.assertTrue(plane.computeIntersectionPoint(parallelInside, intersectPt));
        Assert.assertTrue(intersectPt.isFuzzyEqual(parallelInside.origin));
    }
}
