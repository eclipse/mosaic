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

import java.util.ArrayList;
import java.util.Collections;


/**
 * This class implements the QuickHull algorithm to compute the convex hull from a set of points.
 * FIXME: For now the algorithm is 2D and considers only the X- and Z-coordinates. Modified version
 * from http://read.pudn.com/downloads50/sourcecode/graph/172533/QuickHull.java__.htm
 */
public class QuickHull2d {
    private ArrayList<Line> hullEdges;
    private ArrayList<Vector3d> points;
    private ArrayList<Vector3d> hullPoints;

    private Line aq = new Line(new Vector3d(), new Vector3d());

    private static class Line {
        public Vector3d p1;
        public Vector3d p2;

        public Line(Vector3d p1, Vector3d p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        public double length() {
            return p1.distanceTo(p2);
        }

        public double angle() {
            return Math.atan2(p2.z - p1.z, p2.x - p1.x);
        }

        public void set(Vector3d p1, Vector3d p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    private QuickHull2d(ArrayList<Vector3d> points) {
        this.points = points;
    }

    private double leftTurn(Vector3d p, Vector3d q, Vector3d r) {
        return (p.x - q.x) * (r.z - q.z) - (p.z - q.z) * (r.x - q.x);
    }

    private double ccw(Vector3d p, Vector3d q, Vector3d r) {
        return leftTurn(p, q, r);
    }

    private double angle(Line l1, Line l2) {
        double result = l1.angle() - l2.angle();
        if (result < -Math.PI) {
            return result + Math.PI * 2.0f;
        } else if (result > Math.PI) {
            return result - Math.PI * 2.0f;
        } else {
            return result;
        }
    }

    private void quickHull(Line ab, int l, int r) {
        if (l <= r) {
            int i, s1, s2, pivot;
            double maxDist = 0.0;
            double dist;
            Vector3d P, q;
            Vector3d a = ab.p1;
            Vector3d b = ab.p2;
            Line aP, Pb;

            pivot = l;
            for (i = l; i <= r; i++) {
                q = points.get(i);
                aq.set(a, q);
                dist = Math.abs(aq.length() * Math.sin(angle(ab, aq)));
                if (dist >= maxDist) {
                    maxDist = dist;
                    pivot = i;
                }
            }

            Collections.swap(points, l, pivot);
            P = points.get(l);

            aP = new Line(a, P);
            Pb = new Line(P, b);

            hullEdges.add(aP);
            hullEdges.add(Pb);
            hullEdges.remove(ab);

            i = l + 1;
            s1 = l;
            s2 = r + 1;

            while (i < s2) {
                q = points.get(i);
                if (ccw(a, P, q) > 0.0) {
                    Collections.swap(points, ++s1, i++);
                } else if (ccw(P, b, q) > 0.0) {
                    Collections.swap(points, --s2, i);
                } else {
                    i++;
                }
            }

            quickHull(aP, l + 1, s1);
            quickHull(Pb, s2, r);
        }
    }

    private void computeConvexHull() {
        Vector3d a, b, q;
        double minX, maxX, x;
        int i, j, iLeft, iRight, iLower, iUpper;
        Line ab, ba;

        hullEdges = new ArrayList<>();

        // find the left and right extrema. These define the chord that separates the upper and
        // lower sets
        minX = maxX = points.get(0).x;
        iLeft = iRight = 0;
        for (i = 1; i < points.size(); i++) {
            q = points.get(i);
            x = q.x;
            if (x > maxX) {
                maxX = x;
                iRight = i;
            }
            if (x < minX) {
                minX = x;
                iLeft = i;
            }
        }

        // Partition the points into upper and lower sets
        Collections.swap(points, 0, iRight);

        if (iLeft == 0) {
            iLeft = iRight;
        }

        a = points.get(0);
        b = points.get(iLeft);

        ab = new Line(a, b);
        ba = new Line(b, a);

        iUpper = 0;
        iLower = points.size();
        i = 1;

        while (i < iLower) {
            q = points.get(i);
            if (ccw(b, a, q) < 0.0) {
                Collections.swap(points, ++iUpper, i++);
            } else if (ccw(b, a, q) > 0.0) {
                Collections.swap(points, --iLower, i);
            } else {
                i++;
            }
        }

        hullEdges.add(ab);
        hullEdges.add(ba);

        quickHull(ab, 1, iUpper);
        quickHull(ba, iLower, points.size() - 1);

        int hullSize = hullEdges.size();
        Line[] orderedEdges = new Line[hullSize];
        Line curEdge = hullEdges.get(0);
        Line nextEdge;
        orderedEdges[0] = curEdge;
        for (i = 1; i < hullSize; i++) {
            for (j = 1; j < hullSize; j++) {
                nextEdge = hullEdges.get(j);
                if (nextEdge.p1.equals(curEdge.p2)) {
                    orderedEdges[i] = nextEdge;
                    curEdge = nextEdge;
                    break;
                }
            }
        }

        hullPoints = new ArrayList<>();
        for (i = 0; i < hullSize; i++) {
            hullPoints.add(orderedEdges[i].p1);
        }
    }

    public static ArrayList<Vector3d> computeConvexHull(ArrayList<Vector3d> points) {
        QuickHull2d qh = new QuickHull2d(points);
        qh.computeConvexHull();
        return qh.hullPoints;
    }
}