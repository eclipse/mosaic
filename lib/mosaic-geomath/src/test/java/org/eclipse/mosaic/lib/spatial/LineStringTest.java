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
import java.util.concurrent.atomic.AtomicInteger;

public class LineStringTest {

    @Test
    public void singularTest() {
        LineString.Vecs ls = new LineString.Vecs();
        Assert.assertTrue(ls.isEmpty());
        Assert.assertTrue(ls.isSingular());

        ls.addPoint(new Vector3d(0, 0, 0));
        Assert.assertFalse(ls.isEmpty());
        Assert.assertTrue(ls.isSingular());

        ls.addPoint(new Vector3d(1, 0, 0));
        Assert.assertFalse(ls.isEmpty());
        Assert.assertFalse(ls.isSingular());
    }

    @Test
    public void lengthTest() {
        int n = 5;
        LineString.Vecs ls = makeLineStringX(n, 1.7);
        Assert.assertTrue(MathUtils.isFuzzyEqual(ls.getLength(), 1.7 * (n - 1)));
    }

    @Test
    public void directionTest() {
        Vector3d step = new Vector3d(1.4, 2.3, 3.2);
        LineString.Vecs ls = makeLineString(5, new Vector3d(), step);

        Vector3d dir = step.norm(new Vector3d());
        Assert.assertTrue(ls.getStartDirection(new Vector3d()).isFuzzyEqual(dir));
        Assert.assertTrue(ls.getEndDirection(new Vector3d()).isFuzzyEqual(dir));

        ls = makeLineStringX(3, 1.0);
        ls.extendStart(1.0);
        Assert.assertTrue(ls.first().isFuzzyEqual(new Vector3d(-1, 0, 0)));
        Assert.assertTrue(ls.last().isFuzzyEqual(new Vector3d(2, 0, 0)));
        ls.extendEnd(1.0);
        Assert.assertTrue(ls.first().isFuzzyEqual(new Vector3d(-1, 0, 0)));
        Assert.assertTrue(ls.last().isFuzzyEqual(new Vector3d(3, 0, 0)));
    }

    @Test
    public void reverseTest() {
        LineString.Vecs ls = makeLineString(4, new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
        LineString.Vecs reverse = makeLineString(4, new Vector3d(3, 0, 0), new Vector3d(-1, 0, 0));

        Assert.assertTrue(ls.getReversed(new LineString.Vecs()).isFuzzyEqual(reverse));
    }

    @Test
    public void pointQueryTest() {
        LineString.Vecs ls = makeLineStringX(3, 1.0);

        Assert.assertTrue(ls.getPointAtPosition(0.5, new Vector3d())
                .isFuzzyEqual(new Vector3d(0.5, 0, 0)));
        Assert.assertTrue(MathUtils.isFuzzyEqual(ls.getPosition(new Vector3d(0.5, 1, 0)), 0.5));
        Assert.assertTrue(MathUtils.isFuzzyEqual(ls.getDistance(new Vector3d(0.5, 1, 0)), 1));

        Assert.assertTrue(ls.getClosestPointOnPath(new Vector3d(0.5, 1, 0), new Vector3d())
                .isFuzzyEqual(new Vector3d(0.5, 0, 0)));

        Assert.assertTrue(ls.getClosestPointOnPath(new Vector3d(-10, 1, 0), new Vector3d())
                .isFuzzyEqual(new Vector3d(0, 0, 0)));

        Assert.assertTrue(ls.getClosestPointOnPath(new Vector3d(10, 1, 0), new Vector3d())
                .isFuzzyEqual(new Vector3d(2, 0, 0)));
    }

    @Test
    public void lowerQueryTest() {
        LineString.Vecs ls = makeLineStringX(3, 1.0);

        Assert.assertEquals(1, ls.getLowerIndex(1.5));
        Assert.assertEquals(1, ls.getLowerIndex(new Vector3d(1.5, 1, 0)));
        Assert.assertSame(ls.get(1), ls.getLowerPoint(new Vector3d(1.5, 1, 0)));
    }

    @Test
    public void intersectionTest() {
        LineString.Vecs ls = makeLineString(4, new Vector3d(-2, 0, 0), new Vector3d(1, 0, 0));
        Vector3d intersectionPt = new Vector3d();

        LineString.Vecs notIntersecting = makeLineString(4, new Vector3d(-2, 0, 1), new Vector3d(1, 0, 0));
        Assert.assertFalse(ls.isIntersectingXZ(notIntersecting));
        Assert.assertFalse(ls.getIntersectionPointXZ(notIntersecting, intersectionPt));

        LineString.Vecs edgeIntersecting = makeLineString(4, new Vector3d(0.5, 0, 1.5), new Vector3d(0, 0, -1));
        Assert.assertTrue(ls.isIntersectingXZ(edgeIntersecting));
        Assert.assertTrue(ls.getIntersectionPointXZ(edgeIntersecting, intersectionPt));
        Assert.assertTrue(intersectionPt.isFuzzyEqual(new Vector3d(0.5, 0, 0)));

        LineString.Vecs pointIntersecting = makeLineString(4, new Vector3d(0, 0, 1), new Vector3d(0, 0, -1));
        Assert.assertTrue(ls.isIntersectingXZ(pointIntersecting));
        Assert.assertTrue(ls.getIntersectionPointXZ(pointIntersecting, intersectionPt));
        Assert.assertTrue(intersectionPt.isFuzzyEqual(new Vector3d(0, 0, 0)));
    }

    @Test
    public void subPathTest() {
        LineString.Vecs ls = makeLineStringX(5, 1.0);

        LineString.Vecs sub = new LineString.Vecs();
        ls.getSubPath(1.0, 3.0, sub);
        Assert.assertTrue(sub.isFuzzyEqual(makeLineString(3, new Vector3d(1, 0, 0), new Vector3d(1, 0, 0))));

        ls.getSubPath(1.5, 3.5, sub);
        LineString.Vecs expected = new LineString.Vecs();
        expected.add(new Vector3d(1.5, 0, 0));
        expected.add(new Vector3d(2, 0, 0));
        expected.add(new Vector3d(3, 0, 0));
        expected.add(new Vector3d(3.5, 0, 0));
        Assert.assertTrue(sub.isFuzzyEqual(expected));

        // reverse order
        ls.getSubPath(3.0, 1.0, sub);
        Assert.assertTrue(sub.isFuzzyEqual(makeLineString(3, new Vector3d(3, 0, 0), new Vector3d(-1, 0, 0))));

        ls.getSubPath(3.5, 1.5, sub);
        expected.clear();
        expected.add(new Vector3d(3.5, 0, 0));
        expected.add(new Vector3d(3, 0, 0));
        expected.add(new Vector3d(2, 0, 0));
        expected.add(new Vector3d(1.5, 0, 0));
        Assert.assertTrue(sub.isFuzzyEqual(expected));
    }

    @Test
    public void walkTest() {
        LineString.Vecs ls = makeLineStringX(3, 1.0);
        List<Vector3d> visited = new ArrayList<>();
        AtomicInteger ipos = new AtomicInteger();
        ls.walk((pt, pos) -> {
            visited.add(pt);
            Assert.assertTrue(MathUtils.isFuzzyEqual(ipos.getAndIncrement(), pos));
        });

        Assert.assertEquals(ls.size(), visited.size());
        for (int i = 0; i < ls.size(); i++) {
            Assert.assertSame(ls.get(i), visited.get(i));
        }
    }

    private LineString.Vecs makeLineStringX(int nPts, double step) {
        return makeLineString(nPts, new Vector3d(), new Vector3d(step, 0, 0));
    }

    private LineString.Vecs makeLineString(int nPts, Vector3d start, Vector3d step) {
        LineString.Vecs ls = new LineString.Vecs();
        Vector3d pt = new Vector3d(start);
        for (int i = 0; i < nPts; i++) {
            ls.addPoint(new Vector3d(pt));
            pt.add(step);
        }
        return ls;
    }
}
