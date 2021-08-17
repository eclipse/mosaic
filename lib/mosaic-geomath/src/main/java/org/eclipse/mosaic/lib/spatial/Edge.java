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
import org.eclipse.mosaic.lib.math.VectorUtils;

import java.io.Serializable;

public class Edge<T extends Vector3d> implements Serializable {

    private final static long serialVersionUID = 1L;

    public T a;
    public T b;

    public Edge(T a, T b) {
        this.a = a;
        this.b = b;
    }

    public boolean isLeftOfEdge(Vector3d queryPt) {
        return VectorUtils.isLeftOfLine(queryPt, a, b.subtract(a, new Vector3d()));
    }

    public boolean isLeftOfEdge(Vector3d queryPt, Vector3d up) {
        return VectorUtils.isLeftOfLine(queryPt, a, b.subtract(a, new Vector3d()), up);
    }

    public double getDistanceToPoint(Vector3d point) {
        return getNearestPointOnEdge(point).distanceTo(point);
    }

    public double getDistanceToRay(Ray ray) {
        Vector3d nearOnRay = ray.getNearestPointOnRay(this);
        Vector3d nearOnEdge = getNearestPointOnEdge(ray);

        return nearOnRay.distanceTo(nearOnEdge);
    }

    public Vector3d getNearestPointOnEdge(Vector3d queryPt) {
        return getNearestPointOnEdge(queryPt, new Vector3d());
    }

    public Vector3d getNearestPointOnEdge(Vector3d point, Vector3d result) {
        b.subtract(a, result);
        double l = (point.dot(result) - a.dot(result)) / result.dot(result);
        if (l < 0) {
            result.set(a);
        } else if (l > 1) {
            result.set(b);
        } else {
            result.multiply(l).add(a);
        }
        return result;
    }

    public Vector3d getNearestPointOnEdge(Ray ray) {
        return getNearestPointOnEdge(ray, new Vector3d());
    }

    public Vector3d getNearestPointOnEdge(Ray ray, Vector3d result) {
        Vector3d tv1 = new Vector3d();
        Vector3d tv2 = new Vector3d();
        Vector3d tv3 = new Vector3d();

        b.subtract(a, tv1).norm();
        tv2.set(ray.direction).norm();

        double cos = tv1.dot(tv2);
        double n = 1 - cos * cos;
        if (MathUtils.isFuzzyZero(n)) {
            // edge and ray are parallel, choose edge point nearer to ray origin
            if (a.distanceSqrTo(ray.origin) < b.distanceSqrTo(ray.origin)) {
                return result.set(a);
            } else {
                return result.set(b);
            }
        }

        ray.origin.subtract(a, tv3);
        double ax = tv3.dot(tv1);
        double bx = tv3.dot(tv2);
        double len = (ax - bx * cos) / n;
        if (len > 0) {
            double d = a.distanceTo(b);
            tv1.multiply(Math.min(len, d));
            return result.set(a).add(tv1);
        } else {
            return result.set(a);
        }
    }

    public Vector3d getNearestPointOnEdge(Edge<?> otherEdge) {
        return getNearestPointOnEdge(otherEdge, new Vector3d());
    }

    public Vector3d getNearestPointOnEdge(Edge<?> otherEdge, Vector3d result) {
        Vector3d tv1 = new Vector3d();
        Vector3d tv2 = new Vector3d();
        Vector3d tv3 = new Vector3d();

        b.subtract(a, tv1).norm();
        otherEdge.b.subtract(otherEdge.a, tv2).norm();

        double cos = tv1.dot(tv2);
        double n = 1 - cos * cos;
        if (MathUtils.isFuzzyZero(n)) {
            // edges are parallel
            otherEdge.a.subtract(a, tv1);
            if (tv1.dot(tv2) > 0) {
                return result.set(a);
            } else {
                return result.set(b);
            }
        }

        result.set(otherEdge.a);
        a.subtract(otherEdge.a, tv3);
        double ax = tv3.dot(tv1);
        double bx = tv3.dot(tv2);
        double len = (ax - bx * cos) / n;
        if (len > 0) {
            double d = otherEdge.getLength();
            tv1.multiply(Math.min(len, d));
            result.add(tv1);
        }
        tv1.set(result);
        return getNearestPointOnEdge(tv1, result);
    }

    public double getLength() {
        return a.distanceTo(b);
    }
    
}
