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

import org.eclipse.mosaic.lib.math.Vector3d;

import java.util.List;
import java.util.stream.Stream;

public class BoundingBox {

    public final Vector3d min = new Vector3d();
    public final Vector3d max = new Vector3d();

    public final Vector3d size = new Vector3d();
    public final Vector3d center = new Vector3d();

    private boolean isEmpty = true;

    public boolean isEmpty() {
        return isEmpty;
    }

    public void clear() {
        min.set(0.0);
        max.set(0.0);
        size.set(0.0);
        center.set(0.0);
        isEmpty = true;
    }

    private void updateSizeAndCenter() {
        size.set(max).subtract(min);
        size.multiply(0.5, center).add(min);
    }

    private void addPoint(Vector3d point) {
        if (isEmpty) {
            min.set(point);
            max.set(point);
            isEmpty = false;
        } else {
            if (point.x < min.x) {
                min.x = point.x;
            }
            if (point.y < min.y) {
                min.y = point.y;
            }
            if (point.z < min.z) {
                min.z = point.z;
            }
            if (point.x > max.x) {
                max.x = point.x;
            }
            if (point.y > max.y) {
                max.y = point.y;
            }
            if (point.z > max.z) {
                max.z = point.z;
            }
        }
    }

    public void add(Vector3d point) {
        addPoint(point);
        updateSizeAndCenter();
    }

    public void add(Vector3d... points) {
        for (Vector3d point : points) {
            addPoint(point);
        }
        updateSizeAndCenter();
    }

    public void add(List<? extends Vector3d> points) {
        for (Vector3d point : points) {
            addPoint(point);
        }
        updateSizeAndCenter();
    }

    public void add(Stream<? extends Vector3d> points) {
        points.forEach(this::addPoint);
        updateSizeAndCenter();
    }

    public void add(BoundingBox other) {
        addPoint(other.min);
        addPoint(other.max);
        updateSizeAndCenter();
    }

    public boolean contains(Vector3d pt) {
        return contains(pt.x, pt.y, pt.z);
    }

    public boolean contains(double x, double y, double z) {
        return x >= min.x && x <= max.x
                && y >= min.y && y <= max.y
                && z >= min.z && z <= max.z;
    }

    public double distanceToPoint(Vector3d pt) {
        return Math.sqrt(distanceSqrToPoint(pt.x, pt.y, pt.z));
    }

    public double distanceToPoint(double px, double py, double pz) {
        return Math.sqrt(distanceSqrToPoint(px, py, pz));
    }

    public double distanceSqrToPoint(Vector3d pt) {
        return distanceSqrToPoint(pt.x, pt.y, pt.z);
    }

    public double distanceSqrToPoint(double px, double py, double pz) {
        if (contains(px, py, pz)) {
            return 0;
        }

        double x = 0.0, y = 0.0, z = 0.0;

        double tmp = px - min.x;
        if (tmp < 0) {
            // px < minX
            x = tmp;
        } else {
            tmp = max.x - px;
            if (tmp < 0) {
                // px > maxX
                x = tmp;
            }
        }

        tmp = py - min.y;
        if (tmp < 0) {
            // py < minY
            y = tmp;
        } else {
            tmp = max.y - py;
            if (tmp < 0) {
                // py > maxY
                y = tmp;
            }
        }

        tmp = pz - min.z;
        if (tmp < 0) {
            // pz < minZ
            z = tmp;
        } else {
            tmp = max.z - pz;
            if (tmp < 0) {
                // pz > maxZ
                z = tmp;
            }
        }

        return x * x + y * y + z * z;
    }

    public double hitDistance(Ray r) {
        double sqr = hitDistanceSqr(r);
        if (sqr != Double.MAX_VALUE) {
            return Math.sqrt(sqr);
        }
        // no hit!
        return Double.MAX_VALUE;
    }

    public double hitDistanceSqr(Ray r) {
        double tmin, tmax, tymin, tymax, tzmin, tzmax, div;

        if (contains(r.origin)) {
            return 0.0;
        }

        div = 1.0 / r.direction.x;
        if (div >= 0.0) {
            tmin = (min.x - r.origin.x) * div;
            tmax = (max.x - r.origin.x) * div;
        } else {
            tmin = (max.x - r.origin.x) * div;
            tmax = (min.x - r.origin.x) * div;
        }

        div = 1.0 / r.direction.y;
        if (div >= 0.0) {
            tymin = (min.y - r.origin.y) * div;
            tymax = (max.y - r.origin.y) * div;
        } else {
            tymin = (max.y - r.origin.y) * div;
            tymax = (min.y - r.origin.y) * div;
        }

        if ((tmin > tymax) || (tymin > tmax)) {
            // no intersection
            return Double.MAX_VALUE;
        }
        if (tymin > tmin) {
            tmin = tymin;
        }
        if (tymax < tmax) {
            tmax = tymax;
        }

        div = 1.0 / r.direction.z;
        if (div >= 0.0) {
            tzmin = (min.z - r.origin.z) * div;
            tzmax = (max.z - r.origin.z) * div;
        } else {
            tzmin = (max.z - r.origin.z) * div;
            tzmax = (min.z - r.origin.z) * div;
        }

        if ((tmin > tzmax) || (tzmin > tmax)) {
            // no intersection
            return Double.MAX_VALUE;
        }
        if (tzmin > tmin) {
            tmin = tzmin;
        }

        if (tmin > 0) {
            // hit! calculate square distance between ray origin and hit point
            double comp = r.direction.x * tmin;
            double dist = comp * comp;
            comp = r.direction.y * tmin;
            dist += comp * comp;
            comp = r.direction.z * tmin;
            dist += comp * comp;
            return dist;
        } else {
            // no intersection
            return Double.MAX_VALUE;
        }
    }
}
