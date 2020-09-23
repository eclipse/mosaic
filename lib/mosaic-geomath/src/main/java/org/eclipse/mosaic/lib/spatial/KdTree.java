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

import org.eclipse.mosaic.lib.math.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KdTree<T> extends SpatialTree<T> {
    private final KdNode root;

    public KdTree(SpatialItemAdapter<T> itemAdapter, List<T> items) {
        this(itemAdapter, items, 20);
    }

    public KdTree(SpatialItemAdapter<T> itemAdapter, List<T> items, int bucketSize) {
        super(itemAdapter);
        root = new KdNode(new ArrayList<>(items), bucketSize, 0);
    }

    @Override
    public Node getRoot() {
        return root;
    }

    private class KdNode extends Node {
        private List<Node> children = null;
        private List<T> items = null;

        KdNode(List<T> items, int bucketSz, int depth) {
            super(depth);

            Vector3d v = new Vector3d();
            bounds.add(items.stream().map(it -> itemAdapter.getMin(it, v)));
            bounds.add(items.stream().map(it -> itemAdapter.getMax(it, v)));

            if (items.size() <= bucketSz) {
                // leaf node
                this.items = items;
            } else {
                // intermediate node
                split(items);
                List<T> left = new ArrayList<>();
                List<T> right = new ArrayList<>();
                for (int i = 0; i < items.size() / 2; i++) {
                    left.add(items.get(i));
                }
                for (int i = items.size() / 2; i < items.size(); i++) {
                    right.add(items.get(i));
                }
                children = new ArrayList<>();
                children.add(new KdNode(left, bucketSz, depth+1));
                children.add(new KdNode(right, bucketSz, depth+1));
            }
        }

        private void split(List<T> items) {
            if (bounds.size.x > bounds.size.y && bounds.size.x > bounds.size.z) {
                items.sort(xCmp);
            } else if (bounds.size.y > bounds.size.x && bounds.size.y > bounds.size.z) {
                items.sort(yCmp);
            } else if (bounds.size.z > bounds.size.x && bounds.size.z > bounds.size.y) {
                items.sort(zCmp);
            }
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public List<Node> getChildren() {
            return children;
        }

        @Override
        public List<T> getItems() {
            return items;
        }
    }

    private final Comparator<T> xCmp = Comparator.comparingDouble(itemAdapter::getCenterX);
    private final Comparator<T> yCmp = Comparator.comparingDouble(itemAdapter::getCenterY);
    private final Comparator<T> zCmp = Comparator.comparingDouble(itemAdapter::getCenterZ);
}
