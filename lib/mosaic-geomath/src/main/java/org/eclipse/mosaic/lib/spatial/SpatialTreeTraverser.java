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

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.CartesianPolygon;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoPolygon;
import org.eclipse.mosaic.lib.geo.Point;
import org.eclipse.mosaic.lib.geo.Polygon;
import org.eclipse.mosaic.lib.math.Vector3d;

import java.util.ArrayList;
import java.util.List;

public abstract class SpatialTreeTraverser<T> {

    public void traverse(SpatialTree<T> tree) {
        traverseNode(tree.getRoot(), tree);
    }

    protected void traverseNode(SpatialTree<T>.Node node, SpatialTree<T> tree) {
        if (node.isLeaf()) {
            traverseLeaf(node, tree);
        } else {
            traverseChildren(node, tree);
        }
    }

    protected abstract void traverseChildren(SpatialTree<T>.Node node, SpatialTree<T> tree);

    protected abstract void traverseLeaf(SpatialTree<T>.Node node, SpatialTree<T> tree);

    public abstract static class CenterDistanceBased<T> extends SpatialTreeTraverser<T> {
        protected final Vector3d center = new Vector3d();

        protected void setCenter(Vector3d center) {
            this.center.set(center);
        }

        protected double getCenterDistanceSqr(T item, SpatialTree<T> tree) {
            SpatialItemAdapter<T> it = tree.getItemAdapter();
            double dx = it.getCenterX(item) - center.x;
            double dy = it.getCenterY(item) - center.y;
            double dz = it.getCenterZ(item) - center.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }

    public static class InRadius<T> extends CenterDistanceBased<T> {
        protected double radiusSqr = 0;
        protected final List<T> result = new ArrayList<>();

        public InRadius<T> setup(Vector3d center, double radius) {
            setCenter(center);
            radiusSqr = radius * radius;
            result.clear();
            return this;
        }

        public List<T> getResult() {
            return result;
        }

        @Override
        protected void traverseChildren(SpatialTree<T>.Node node, SpatialTree<T> tree) {
            List<SpatialTree<T>.Node> children = node.getChildren();
            for (int i = 0; i < children.size(); i++) {
                SpatialTree<T>.Node child = children.get(i);
                if (child.getBounds().distanceSqrToPoint(center) <= radiusSqr) {
                    traverseNode(child, tree);
                }
            }
        }

        @Override
        protected void traverseLeaf(SpatialTree<T>.Node node, SpatialTree<T> tree) {
            List<T> items = node.getItems();
            for (int i = 0; i < items.size(); i++) {
                T item = items.get(i);
                if (getCenterDistanceSqr(item, tree) <= radiusSqr) {
                    result.add(item);
                }
            }
        }
    }

    public static class Nearest<T> extends CenterDistanceBased<T> {
        protected T nearest = null;
        protected double distanceSqr = Double.POSITIVE_INFINITY;

        public Nearest<T> setup(Vector3d center) {
            setCenter(center);
            distanceSqr = Double.POSITIVE_INFINITY;
            nearest = null;
            return this;
        }

        public T getNearest() {
            return nearest;
        }

        public double getDistance() {
            return Math.sqrt(distanceSqr);
        }

        @Override
        protected void traverseChildren(SpatialTree<T>.Node node, SpatialTree<T> tree) {
            List<SpatialTree<T>.Node> children = node.getChildren();
            if (children.size() == 2) {
                // kd tree, use optimized traversal
                SpatialTree<T>.Node left = children.get(0);
                SpatialTree<T>.Node right = children.get(1);
                double dSqrLt = left.getBounds().distanceSqrToPoint(center);
                double dSqrRt = right.getBounds().distanceSqrToPoint(center);

                if (dSqrLt < dSqrRt) {
                    // traverse left first
                    traverseNode(left, tree);
                    if (dSqrRt < distanceSqr) {
                        traverseNode(right, tree);
                    }
                } else {
                    // traverse right first
                    traverseNode(right, tree);
                    if (dSqrLt < distanceSqr) {
                        traverseNode(left, tree);
                    }
                }

            } else {
                // general case: octree or something else
                for (int i = 0; i < children.size(); i++) {
                    double dSqr = children.get(i).getBounds().distanceSqrToPoint(center);
                    if (dSqr < distanceSqr) {
                        traverseNode(children.get(i), tree);
                    }
                }
            }
        }

        @Override
        protected void traverseLeaf(SpatialTree<T>.Node node, SpatialTree<T> tree) {
            List<T> items = node.getItems();
            for (int i = 0; i < items.size(); i++) {
                T item = items.get(i);
                double dSqr = getCenterDistanceSqr(item, tree);
                if (dSqr <= distanceSqr) {
                    nearest = item;
                    distanceSqr = dSqr;
                }
            }
        }
    }

    public abstract static class InPolygon<P extends Point<P>, T extends Polygon<P>> extends Nearest<T> {

        private P search;

        public void setup(P searchPoint) {
            setup(toVector3d(searchPoint));
            search = searchPoint;
        }

        protected abstract Vector3d toVector3d(P point);

        @Override
        protected void traverseLeaf(SpatialTree<T>.Node node, SpatialTree<T> tree) {
            List<T> items = node.getItems();
            for (int i = 0; i < items.size(); i++) {
                T item = items.get(i);
                double dSqr = getCenterDistanceSqr(item, tree);
                if (item.contains(search) && dSqr <= distanceSqr) {
                    nearest = item;
                    distanceSqr = dSqr;
                }
            }
        }
    }

    public static class InGeoPolygon<T extends GeoPolygon> extends InPolygon<GeoPoint, T> {

        @Override
        protected Vector3d toVector3d(GeoPoint point) {
            return new Vector3d(point.getLongitude(), point.getLatitude(), 0);
        }
    }

    public static class InCartesianPolygon<T extends CartesianPolygon> extends InPolygon<CartesianPoint, T> {

        @Override
        protected Vector3d toVector3d(CartesianPoint point) {
            return new Vector3d(point.getX(), point.getY(), 0);
        }
    }
}
