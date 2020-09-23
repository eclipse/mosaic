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

import java.util.List;

public abstract class SpatialTree<T> {

    public abstract class Node {
        protected final int depth;
        protected final BoundingBox bounds = new BoundingBox();

        protected Node(int depth) {
            this.depth = depth;
        }

        public int getDepth() {
            return depth;
        }

        public BoundingBox getBounds() {
            return bounds;
        }

        public abstract int size();

        public abstract List<Node> getChildren();

        public abstract List<T> getItems();

        public boolean isLeaf() {
            return getChildren() == null || getChildren().isEmpty();
        }

        public boolean contains(T e) {
            boolean inBounds = bounds.contains(itemAdapter.getMinX(e), itemAdapter.getMinY(e), itemAdapter.getMinZ(e)) &&
                    bounds.contains(itemAdapter.getMaxX(e), itemAdapter.getMaxY(e), itemAdapter.getMaxZ(e));
            if (!inBounds) {
                return false;
            }

            if (isLeaf()) {
                return getItems().contains(e);
            } else {
                for (Node nd : getChildren()) {
                    if (nd.contains(e)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    protected final SpatialItemAdapter<T> itemAdapter;

    protected SpatialTree(SpatialItemAdapter<T> itemAdapter) {
        this.itemAdapter = itemAdapter;
    }

    public abstract Node getRoot();

    public SpatialItemAdapter<T> getItemAdapter() {
        return itemAdapter;
    }
}
