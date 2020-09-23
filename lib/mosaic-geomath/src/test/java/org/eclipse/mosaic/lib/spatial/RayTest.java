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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.math.Vector3d;

import org.junit.Test;

public class RayTest {

    @Test
    public void nearForPoint() {
        Ray ray = new Ray();
        ray.origin.set(0, 0, 0);
        ray.direction.set(1, 0, 0);

        // points 'behind' ray origin, nearest point is ray origin
        Vector3d n = ray.getNearestPointOnRay(new Vector3d(-1, 1, 1));
        assertTrue(n.isFuzzyEqual(ray.origin));
        n = ray.getNearestPointOnRay(new Vector3d(-1, 0, 0));
        assertTrue(n.isFuzzyEqual(ray.origin));

        // points 'in front of' ray origin, nearest point is somewhere on ray
        Vector3d t = new Vector3d(1, 0, 0);
        n = ray.getNearestPointOnRay(new Vector3d(1, 1, 1));
        assertTrue(n.isFuzzyEqual(t));
        n = ray.getNearestPointOnRay(new Vector3d(1, 0, 0));
        assertTrue(n.isFuzzyEqual(t));
    }

    @Test
    public void nearForEdge() {
        Ray ray = new Ray();
        ray.origin.set(0, 0, 0);
        ray.direction.set(1, 0, 0);

        // edge 'behind' ray origin, nearest point is ray origin
        Edge<Vector3d> ed = new Edge<>(new Vector3d(-1, 1, 0), new Vector3d(-1, -1, 0));
        Vector3d n = ray.getNearestPointOnRay(ed);
        assertTrue(n.isFuzzyEqual(ray.origin));

        // edge 'in front of' ray origin, nearest point is somewhere on ray
        ed = new Edge<>(new Vector3d(1, 1, 1), new Vector3d(1, -1, 1));
        n = ray.getNearestPointOnRay(ed);
        assertTrue(n.isFuzzyEqual(new Vector3d(1, 0, 0)));
    }

    @Test
    public void intersect() {
        Ray ray = new Ray();
        ray.origin.set(2, 0, -6);
        ray.direction.set(1, 0, 0);

        // line
        Vector3d a = new Vector3d(5, 0, -8);
        Vector3d b = new Vector3d(3, 0, -5);

        // assert intersect
        assertTrue(ray.intersectsLineSegmentXZ(a, b));
        assertTrue(ray.intersectsLineSegmentXZ(b, a));

        ray.direction.set(-1, 0, 0);

        assertFalse(ray.intersectsLineSegmentXZ(a, b));
        assertFalse(ray.intersectsLineSegmentXZ(b, a));

    }

    @Test
    public void intersectOnStartOfLine() {
        Ray ray = new Ray();
        ray.origin.set(2, 0, -6);
        ray.direction.set(1, 0, 0);

        // line
        Vector3d a = new Vector3d(5, 0, -6);
        Vector3d b = new Vector3d(8, 0, -8);

        // assert intersect
        assertTrue(ray.intersectsLineSegmentXZ(a, b));
        // only true, if ray hits start of line, not end of line. otherwise polygon check would fail as hits would be counted twice
        assertFalse(ray.intersectsLineSegmentXZ(b, a));
    }


}