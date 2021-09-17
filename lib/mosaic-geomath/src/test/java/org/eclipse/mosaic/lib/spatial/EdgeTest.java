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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Assert;
import org.junit.Test;

public class EdgeTest {

    @Test
    public void isLeftOfEdgeTest() {
        Edge<Vector3d> ed = new Edge<>(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
        // default up dir: y-axis
        Assert.assertTrue(ed.isLeftOfEdge(new Vector3d(-10, 0, -1)));
        Assert.assertTrue(ed.isLeftOfEdge(new Vector3d(0.5, 0, -1)));
        Assert.assertTrue(ed.isLeftOfEdge(new Vector3d(10, 0, -1)));
        Assert.assertFalse(ed.isLeftOfEdge(new Vector3d(-10, 0, 1)));
        Assert.assertFalse(ed.isLeftOfEdge(new Vector3d(0.5, 0, 1)));
        Assert.assertFalse(ed.isLeftOfEdge(new Vector3d(10, 0, 1)));

        // custom up dir: z-axis
        Assert.assertTrue(ed.isLeftOfEdge(new Vector3d(0.5, 1, 0), new Vector3d(0, 0, 1)));
        Assert.assertFalse(ed.isLeftOfEdge(new Vector3d(0.5, -1, 0), new Vector3d(0, 0, 1)));
    }

    @Test
    public void distanceToEdgeTest() {
        Edge<Vector3d> ed = new Edge<>(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));

        // nearest point is in middle of edge
        Vector3d q = new Vector3d(0.5, 1, 0);
        Assert.assertTrue(MathUtils.isFuzzyEqual(ed.getDistanceToPoint(q), 1));

        // nearest point is start of edge
        q = new Vector3d(-2, 0, 0);
        Assert.assertTrue(MathUtils.isFuzzyEqual(ed.getDistanceToPoint(q), 2));

        // nearest point is end of edge
        q = new Vector3d(3, 0, 0);
        Assert.assertTrue(MathUtils.isFuzzyEqual(ed.getDistanceToPoint(q), 2));
    }

    @Test
    public void nearestForPoint() {
        Edge<Vector3d> ed = new Edge<>(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));

        // nearest point is in middle of edge
        Vector3d n = ed.getNearestPointOnEdge(new Vector3d(0.5, 1, 0));
        Assert.assertTrue(n.isFuzzyEqual(new Vector3d(0.5, 0.0, 0.0)));

        // nearest point is start of edge
        n = ed.getNearestPointOnEdge(new Vector3d(-1, 1, 0));
        Assert.assertTrue(n.isFuzzyEqual(ed.a));

        // nearest point is end of edge
        n = ed.getNearestPointOnEdge(new Vector3d(5, 1, 0));
        Assert.assertTrue(n.isFuzzyEqual(ed.b));
    }

    @Test
    public void nearestForRay() {
        Edge<Vector3d> ed = new Edge<>(new Vector3d(0, 0, 0), new Vector3d(2, 0, 0));

        Ray ray = new Ray();
        ray.direction.set(0, 1, 0);
        ray.origin.set(0, -1, 1);

        // nearest point is in middle of edge
        ray.origin.x = 1;
        Vector3d n = ed.getNearestPointOnEdge(ray);
        Assert.assertTrue(n.isFuzzyEqual(new Vector3d(1, 0, 0)));

        // nearest point is start of edge
        ray.origin.x = -1;
        n = ed.getNearestPointOnEdge(ray);
        Assert.assertTrue(n.isFuzzyEqual(ed.a));

        // nearest point is end of edge
        ray.origin.x = 3;
        n = ed.getNearestPointOnEdge(ray);
        Assert.assertTrue(n.isFuzzyEqual(ed.b));
    }

    @Test
    public void nearestForEdge() {
        Edge<Vector3d> ed = new Edge<>(new Vector3d(0, 0, 0), new Vector3d(2, 0, 0));

        Edge<Vector3d> otherEd = new Edge<>(new Vector3d(), new Vector3d());

        // nearest point is in middle of edge
        otherEd.a.set(1, -1, 1);
        otherEd.b.set(1, 1, 1);
        Vector3d n = ed.getNearestPointOnEdge(otherEd);
        Assert.assertTrue(n.isFuzzyEqual(new Vector3d(1, 0, 0)));

        // nearest point is start of edge
        otherEd.a.set(-1, -1, 1);
        otherEd.b.set(-1, 1, 1);
        n = ed.getNearestPointOnEdge(otherEd);
        Assert.assertTrue(n.isFuzzyEqual(ed.a));

        // nearest point is end of edge
        otherEd.a.set(3, -1, 1);
        otherEd.b.set(3, 1, 1);
        n = ed.getNearestPointOnEdge(otherEd);
        Assert.assertTrue(n.isFuzzyEqual(ed.b));
    }

    @Test
    public void distanceToRay() {
        Edge<Vector3d> ed = new Edge<>(new Vector3d(0, 0, 0), new Vector3d(2, 0, 0));

        Ray ray = new Ray();
        ray.direction.set(0, 1, 0);
        ray.origin.set(0, -1, 1);

        // nearest point is in middle of edge
        ray.origin.x = 1;
        Assert.assertEquals(ed.getDistanceToRay(ray), 1.0, MathUtils.EPSILON_D);

        // nearest point is start of edge
        ray.origin.x = -1;
        Assert.assertEquals(ed.getDistanceToRay(ray), Math.sqrt(2), MathUtils.EPSILON_D);

        // nearest point is end of edge
        ray.origin.x = 3;
        Assert.assertEquals(ed.getDistanceToRay(ray), Math.sqrt(2), MathUtils.EPSILON_D);
    }

    @Test
    public void lengthTest() {
        Edge<Vector3d> ed = new Edge<>(new Vector3d(0, 0, 0), new Vector3d(1, 2, 3));
        Assert.assertTrue(MathUtils.isFuzzyEqual(ed.getLength(), Math.sqrt(1 + 4 + 9)));
    }

}
