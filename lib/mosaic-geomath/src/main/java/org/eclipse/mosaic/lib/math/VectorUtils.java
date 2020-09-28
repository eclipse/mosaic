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

public class VectorUtils {

    public static final Vector3d NORTH = new Vector3d(0, 0, -1);
    public static final Vector3d SOUTH = new Vector3d(0, 0, 1);
    public static final Vector3d EAST = new Vector3d(1, 0, 0);
    public static final Vector3d WEST = new Vector3d(-1, 0, 0);

    public static final Vector3d UP = new Vector3d(0, 1, 0);

    /**
     * Returns heading in degrees for the given direction vector.
     *
     * @param directionVec Direction vector to get the heading for
     * @return Heading in degrees (0 = north, 90 = east, 180 = south, 270 = west)
     */
    public static double getHeadingFromDirection(Vector3d directionVec) {
        double heading = Math.toDegrees(Math.atan2(directionVec.x, -directionVec.z));
        return MathUtils.normalizeDegree(heading);
    }

    /**
     * Computes an direction vector from the given heading angle (in degrees)
     *
     * @param headingDeg angle in degrees (0 = north, 90 = east, 180 = south, 270 = west)
     * @param result     Vector3d to store the result in
     * @return result Vector3d
     */
    public static Vector3d getDirectionVectorFromHeading(double headingDeg, Vector3d result) {
        result.set(0, 0, -1);
        result.rotate(Math.toRadians(headingDeg), 0, -1, 0);
        return result;
    }

    public static boolean isLeftOfLine(Vector3d point, Vector3d linePoint, Vector3d lineDirection) {
        return isLeftOfLine(point, linePoint, lineDirection, UP);
    }

    public static boolean isLeftOfLine(Vector3d point, Vector3d linePoint, Vector3d lineDirection, Vector3d up) {
        Vector3d tv1 = new Vector3d();
        Vector3d tv2 = new Vector3d();

        up.cross(lineDirection, tv1);
        point.subtract(linePoint, tv2);

        return tv1.dot(tv2) > 0;
    }

    public static Vector3d nearestPointOnLine(Vector3d point, Vector3d linePt1, Vector3d linePt2, Vector3d result) {
        linePt2.subtract(linePt1, result);
        double l = (point.dot(result) - linePt1.dot(result)) / result.dot(result);
        result.multiply(l).add(linePt1);
        return result;
    }

    /**
     * Returns the intersection point of two lines in the XZ plane. Notice that, although 3D
     * coordinates are used, only the X and Z components are considered. The Y component of the
     * returned intersection point is always 0. The function considers lines of infinite length;
     * hence the intersection point does not necessarily lie between e11, e12 and e21, e22. If the
     * specified edges are parallel <code>false</code> is returned.
     *
     * @param e11    First point of first line
     * @param e12    Second point of first line
     * @param e21    First point of second line
     * @param e22    Second point of second line
     * @param result Is set to the intersection point of the two lines (of infinite length)
     *               specified by the given points. If the lines are parallel <code>result</code>
     *               remains unchanged
     * @return true if a intersection point was found, false if lines are parallel
     */
    public static boolean computeXZLineIntersectionPoint(Vector3d e11, Vector3d e12, Vector3d e21, Vector3d e22, Vector3d result) {
        // http://en.wikipedia.org/wiki/Line-line_intersection
        double x1 = e11.x;
        double y1 = e11.z;
        double x2 = e12.x;
        double y2 = e12.z;
        double x3 = e21.x;
        double y3 = e21.z;
        double x4 = e22.x;
        double y4 = e22.z;

        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (!MathUtils.isFuzzyZero(denom)) {
            // lines are not parallel
            double a = x1 * y2 - y1 * x2;
            double b = x3 * y4 - y3 * x4;
            double x = (a * (x3 - x4) - b * (x1 - x2)) / denom;
            double z = (a * (y3 - y4) - b * (y1 - y2)) / denom;
            result.set(x, 0, z);
            return true;
        }
        return false;
    }

    /**
     * Similar to {@link #computeXZLineIntersectionPoint(Vector3d, Vector3d, Vector3d, Vector3d, Vector3d)}
     * but also checks if the resulting intersection point lies between e11, e12 and e21, e22. Returns false
     * if the edges are parallel or the intersection point lies outside the edge borders.
     *
     * @param e11    First point of first edge
     * @param e12    Second point of first edge
     * @param e21    First point of second edge
     * @param e22    Second point of second edge
     * @param result Is set to the intersection point of the two edges (of infinite length)
     *               specified by the given points. If the edges do not intersect the result is undefined
     * @return true if a intersection point was found
     */
    public static boolean computeXZEdgeIntersectionPoint(Vector3d e11, Vector3d e12, Vector3d e21, Vector3d e22, Vector3d result) {
        if (!computeXZLineIntersectionPoint(e11, e12, e21, e22, result)) {
            return false;
        }
        // check if result point is on both edges (between start and end points)
        double dx0 = result.x - e11.x;
        double dz0 = result.z - e11.z;
        double dx1 = e12.x - e11.x;
        double dz1 = e12.z - e11.z;
        double dot = dx0 * dx1 + dz0 * dz1;
        double l0 = dx0 * dx0 + dz0 * dz0;
        double l1 = dx1 * dx1 + dz1 * dz1;
        if (dot < -MathUtils.EPSILON_D || l0 > l1 + MathUtils.EPSILON_D) {
            return false;
        }
        dx0 = result.x - e21.x;
        dz0 = result.z - e21.z;
        dx1 = e22.x - e21.x;
        dz1 = e22.z - e21.z;
        dot = dx0 * dx1 + dz0 * dz1;
        l0 = dx0 * dx0 + dz0 * dz0;
        l1 = dx1 * dx1 + dz1 * dz1;
        return !(dot < -MathUtils.EPSILON_D) && !(l0 > l1 + MathUtils.EPSILON_D);
    }

    public static double curvature(Vector3d vecA, Vector3d vecB, Vector3d vecC) {
        // length of triangle sides
        double a = vecC.distanceTo(vecB);
        double b = vecC.distanceTo(vecA);
        double c = vecB.distanceTo(vecA);

        // half of extent of the triangle
        double s = (a + b + c) / 2d;

        // area of triangle
        double area = Math.sqrt(s * (s - a) * (s - b) * (s - c));

        // radius of circumscribed circle of the triangle
        double r = (a * b * c) / (4 * area);
        double curvature = 1d / r;

        double position = -1d * Math.signum((vecC.x - vecA.x) * (vecB.z - vecA.z)
                - (vecC.z - vecA.z) * (vecB.x - vecA.x));

        return curvature * position;
    }

}
