/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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
import java.util.function.Predicate;

class QuadTreeTraversal {

    static <T> void getObjectsInRadius(QuadTree<T> tree, Vector3d center, double radius, List<T> result) {
        getObjectsInRadius(tree, center, radius, null, result);
    }

    static <T> void getObjectsInRadius(QuadTree<T> tree, Vector3d center, double radius, Predicate<T> filter, List<T> result) {
        result.clear();
        selectInRadius(tree.getRoot(), center, radius * radius, filter, result);
    }

    private static <T> void selectInRadius(QuadTree.TreeNode node, Vector3d center, double rSqr, Predicate<T> filter, List<T> result) {
        if (node.isLeaf()) {
            for (int i = 0; i < node.objects.size(); i++) {
                QuadTree<T>.ObjectAndNode oan = (QuadTree<T>.ObjectAndNode) node.objects.get(i);
                if (center.distanceSqrTo(oan.objectPos) < rSqr && (filter == null || filter.test(oan.object))) {
                    result.add(oan.object);
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                if (node.childNodes[i].distanceSqrToPoint(center) < rSqr) {
                    selectInRadius(node.childNodes[i], center, rSqr, filter, result);
                }
            }
        }
    }

    static <T> void getObjectsInBoundingArea(QuadTree<T> tree, BoundingBox area, List<T> result) {
        getObjectsInBoundingArea(tree, area, null, result);
    }

    static <T> void getObjectsInBoundingArea(QuadTree<T> tree, BoundingBox area, Predicate<T> filter, List<T> result) {
        result.clear();
        selectInArea(tree.getRoot(), area, filter, result);
    }

    private static <T> void selectInArea(QuadTree.TreeNode node, BoundingBox area, Predicate<T> filter, List<T> result) {
        if (node.isLeaf()) {
            for (int i = 0; i < node.objects.size(); i++) {
                QuadTree<T>.ObjectAndNode oan = (QuadTree<T>.ObjectAndNode) node.objects.get(i);
                if (area.contains(oan.objectPos) && (filter == null || filter.test(oan.object))) {
                    result.add(oan.object);
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                if (node.childNodes[i].intersects(area)) {
                    selectInArea(node.childNodes[i], area, filter, result);
                }
            }
        }
    }

    static <T> T getNearestObject(QuadTree<T> tree, Vector3d center) {
        return getNearestObject(tree, center, null);
    }

    static <T> T getNearestObject(QuadTree<T> tree, Vector3d center, Predicate<T> filter) {
        QuadTree<T>.ObjectAndNode r = selectNearest(tree.getRoot(), center, Double.MAX_VALUE, filter);
        return r != null ? r.object : null;
    }

    private static <T> QuadTree<T>.ObjectAndNode selectNearest(QuadTree.TreeNode node, Vector3d center, double dSqr, Predicate<T> filter) {
        QuadTree<T>.ObjectAndNode o = null;
        if (node.isLeaf()) {
            for (int i = 0; i < node.objects.size(); i++) {
                QuadTree<T>.ObjectAndNode oan = (QuadTree<T>.ObjectAndNode) node.objects.get(i);
                double d = center.distanceSqrTo(oan.objectPos);
                if (d < dSqr && (filter == null || filter.test((T) oan.object))) {
                    o = oan;
                    dSqr = d;
                }
            }
        } else {
            // traverse child nodes ordered by distance to center (nearest node first)
            double prevChildDist = -1;
            int prevI = -1;
            for (int i = 0; i < 4; i++) {
                // select nearest child node, that is further away than previously selected child node
                int bestI = 0;
                double minD = Double.MAX_VALUE;
                for (int j = 0; j < 4; j++) {
                    double childDist = node.childNodes[i].distanceSqrToPoint(center);
                    if (childDist < minD && (childDist > prevChildDist || (childDist == prevChildDist && j > prevI))) {
                        minD = childDist;
                        bestI = j;
                    }
                }
                prevChildDist = minD;
                prevI = bestI;

                if (minD < dSqr) {
                    QuadTree<T>.ObjectAndNode r = selectNearest(node.childNodes[bestI], center, dSqr, filter);
                    if (r != null) {
                        // found vehicle in child node that is nearer tha dSqr
                        dSqr = r.objectPos.distanceSqrTo(center);
                        o = r;
                    }
                } else {
                    // remaining child nodes are further away than currently selected vehicle
                    break;
                }
            }
        }
        return o;
    }

}
