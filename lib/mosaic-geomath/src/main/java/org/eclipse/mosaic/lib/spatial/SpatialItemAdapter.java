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

import org.eclipse.mosaic.lib.geo.Area;
import org.eclipse.mosaic.lib.math.Vector3d;

public interface SpatialItemAdapter<T> {
    double getMinX(T item);

    double getMinY(T item);

    double getMinZ(T item);

    default double getMaxX(T item) {
        return getMinX(item);
    }

    default double getMaxY(T item) {
        return getMinY(item);
    }

    default double getMaxZ(T item) {
        return getMinZ(item);
    }

    default double getCenterX(T item) {
        return (getMinX(item) + getMaxX(item)) * 0.5;
    }

    default double getCenterY(T item) {
        return (getMinY(item) + getMaxY(item)) * 0.5;
    }

    default double getCenterZ(T item) {
        return (getMinZ(item) + getMaxZ(item)) * 0.5;
    }

    default Vector3d getMin(T item, Vector3d result) {
        return result.set(getMinX(item), getMinY(item), getMinZ(item));
    }

    default Vector3d getMax(T item, Vector3d result) {
        return result.set(getMaxX(item), getMaxY(item), getMaxZ(item));
    }

    default Vector3d getCenter(T item, Vector3d result) {
        return result.set(getCenterX(item), getCenterY(item), getCenterZ(item));
    }

    default void setNode(T item, SpatialTree<T>.Node node) {
    }

    class PointAdapter<T extends Vector3d> implements SpatialItemAdapter<T> {
        @Override
        public double getMinX(T item) {
            return item.x;
        }

        @Override
        public double getMinY(T item) {
            return item.y;
        }

        @Override
        public double getMinZ(T item) {
            return item.z;
        }
    }

    class EdgeAdapter<T extends Edge<?>> implements SpatialItemAdapter<T> {
        @Override
        public double getMinX(T item) {
            return Math.min(item.a.x, item.b.x);
        }

        @Override
        public double getMinY(T item) {
            return Math.min(item.a.y, item.b.y);
        }

        @Override
        public double getMinZ(T item) {
            return Math.min(item.a.z, item.b.z);
        }

        @Override
        public double getMaxX(T item) {
            return Math.max(item.a.x, item.b.x);
        }

        @Override
        public double getMaxY(T item) {
            return Math.max(item.a.y, item.b.y);
        }

        @Override
        public double getMaxZ(T item) {
            return Math.max(item.a.z, item.b.z);
        }

        @Override
        public double getCenterX(T item) {
            return (item.a.x + item.b.x) * 0.5;
        }

        @Override
        public double getCenterY(T item) {
            return (item.a.y + item.b.y) * 0.5;
        }

        @Override
        public double getCenterZ(T item) {
            return (item.a.z + item.b.z) * 0.5;
        }
    }

    class AreaAdapter<T extends Area<?>> implements SpatialItemAdapter<T> {

        @Override
        public double getMinX(T item) {
            return item.getBounds().getSideD();
        }

        @Override
        public double getMinY(T item) {
            return item.getBounds().getSideA();
        }

        @Override
        public double getMinZ(T item) {
            return 0;
        }

        @Override
        public double getMaxX(T item) {
            return item.getBounds().getSideB();
        }

        @Override
        public double getMaxY(T item) {
            return item.getBounds().getSideC();
        }

        @Override
        public double getMaxZ(T item) {
            return 0;
        }
    }
}
