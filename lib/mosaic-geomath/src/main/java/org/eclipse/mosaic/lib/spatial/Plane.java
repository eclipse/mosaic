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

public class Plane {

    /**
     * Plane origin
     */
    public final Vector3d p = new Vector3d();

    /**
     * Plane normal vector
     */
    public final Vector3d n = new Vector3d(0, 1, 0);

    public Plane() {

    }

    public Plane(Vector3d p, Vector3d n) {
        this();
        this.p.set(p);
        this.n.set(n);
    }

    /**
     * Computes the intersection point of this plane and the specified ray. Returns false if there is no intersection
     * point (i.e. ray points away from the plane or plane and ray are parallel).
     */
    public boolean computeIntersectionPoint(Ray ray, Vector3d result) {
        double denom = n.dot(ray.direction);
        if (!MathUtils.isFuzzyZero(denom)) {
            double t = p.subtract(ray.origin, result).dot(n) / denom;
            result.set(ray.direction).multiply(t).add(ray.origin);
            return t >= 0;

        } else if (contains(ray.origin)) {
            // special case: parallel ray in plane
            result.set(ray.origin);
            return true;
        }
        return false;
    }

    public boolean isAbove(Vector3d point) {
        return point.subtract(p, new Vector3d()).dot(n) > 0;
    }

    public boolean contains(Vector3d point) {
        return MathUtils.isFuzzyZero(point.subtract(p, new Vector3d()).dot(n));
    }
}