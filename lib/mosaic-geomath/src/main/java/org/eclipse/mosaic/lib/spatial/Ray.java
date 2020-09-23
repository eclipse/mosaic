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

import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Vector3d;

public class Ray {

    public final Vector3d origin = new Vector3d();

    public final Vector3d direction = new Vector3d();

    public Ray() {
        // explicit empty
    }

    public Ray(Vector3d origin, Vector3d direction) {
        this.origin.set(origin);
        this.direction.set(direction);
    }

    public void set(double oriX, double oriY, double oriZ, double dirX, double dirY, double dirZ) {
        origin.set(oriX, oriY, oriZ);
        direction.set(dirX, dirY, dirZ);
    }

    public Vector3d getNearestPointOnRay(Vector3d point) {
        return getNearestPointOnRay(point, new Vector3d());
    }

    public Vector3d getNearestPointOnRay(Vector3d point, Vector3d result) {
        double l = (point.dot(direction) - origin.dot(direction)) / direction.dot(direction);
        if (l < 0) {
            result.set(origin);
        } else {
            result.set(direction).multiply(l).add(origin);
        }
        return result;
    }

    public Vector3d getNearestPointOnRay(Edge<?> edge) {
        return getNearestPointOnRay(edge, new Vector3d());
    }

    public Vector3d getNearestPointOnRay(Edge<?> edge, Vector3d result) {
        Vector3d tmp = edge.getNearestPointOnEdge(this, new Vector3d());
        return getNearestPointOnRay(tmp, result);
    }

    public boolean intersectsLineSegmentXZ(Vector3d lineStart, Vector3d lineEnd) {
        Vector3d v1 = origin.subtract(lineStart, new Vector3d());
        Vector3d v2 = lineEnd.subtract(lineStart, new Vector3d());
        Vector3d v3 = new Vector3d(direction.z, 0, direction.x);

        double dot = v2.dot(v3);
        if (MathUtils.isFuzzyZero(dot)) {
            return false;
        }

        double t1 = (v2.x * v1.z - v1.x * v2.z) / dot;
        double t2 = v1.dot(v3) / dot;

        return t1 >= 0.0 && t2 >= 0.0 && t2 < 1.0;
    }
}