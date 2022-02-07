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

import org.eclipse.mosaic.lib.misc.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * {@link Grid} represents a possible DataStructure to store vehicles based on a map
 * efficiently.
 */
public class Grid<T> {

    private final SpatialItemAdapter<T> adapter;
    private final double cellWidth;
    private final double cellHeight;
    private final int colAmount;
    private final int rowAmount;
    private final double minX;
    private final double maxX;
    private final double minZ;
    private final double maxZ;

    private final List<List<GridCell>> grid;
    private final Map<T, Tuple<Integer, Integer>> items = new HashMap<>();

    public Grid(final SpatialItemAdapter<T> adapter, double cellWidth, double cellHeight, final BoundingBox boundingBox) {
        this(adapter, cellWidth, cellHeight, boundingBox.min.x, boundingBox.max.x, boundingBox.min.z, boundingBox.max.z);
    }

    public Grid(final SpatialItemAdapter<T> adapter, double cellWidth, double cellHeight,
                double minX, double maxX, double minZ, double maxZ) {
        this.adapter = adapter;

        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        colAmount = (int) Math.ceil((maxX - minX) / cellWidth);
        rowAmount = (int) Math.ceil((maxZ - minZ) / cellHeight);

        grid = new ArrayList<>();
        for (int col = 0; col < colAmount; col++) {
            grid.add(new ArrayList<>());
            for (int row = 0; row < rowAmount; row++) {
                grid.get(col).add(new GridCell());
            }
        }
    }

    /**
     * Adds or updates an item in the grid.
     *
     * @param item the item to be added
     * @return true if the item is in the bounding area of the grid, else false
     */
    public boolean addOrUpdateItem(T item) {
        if (isInBounds(adapter.getMinX(item), adapter.getMinY(item), adapter.getMinZ(item))) {
            if (items.containsKey(item)) { // update -> remove from old grid cell
                getGridCellEntries(items.get(item).getA(), items.get(item).getB()).remove(item);
            }
            Tuple<Integer, Integer> coords = toGridCoordinate(adapter.getMinX(item), adapter.getMinY(item), adapter.getMinZ(item));
            getGridCellEntries(coords.getA(), coords.getB()).add(item);
            items.put(item, coords);
            return true;
        }
        return false;
    }

    public void removeItem(T item) {
        Tuple<Integer, Integer> coords = items.remove(item);
        if (coords != null) {
            getGridCellEntries(coords.getA(), coords.getB()).remove(item);
        }
    }

    private Tuple<Integer, Integer> toGridCoordinate(double x, double y, double z) {
        // also looking at special case where item is directly on the max borders
        int col = x == maxX ? colAmount - 1 : (int) ((x - minX) / cellWidth);
        int row = z == maxZ ? rowAmount - 1 : (int) ((z - minZ) / cellHeight);
        return new Tuple<>(col, row);
    }

    private boolean isInBounds(double x, double y, double z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public List<T> getItemsInBoundingArea(BoundingBox boundingBox, Predicate<T> filter) {
        Tuple<Integer, Integer> minCoords =
                toGridCoordinate(Math.max(boundingBox.min.x, minX), boundingBox.min.y, Math.max(boundingBox.min.z, minZ));
        Tuple<Integer, Integer> maxCoords =
                toGridCoordinate(Math.min(boundingBox.max.x, maxX), boundingBox.max.y, Math.min(boundingBox.max.z, maxZ));

        List<T> itemsInBoundingBox = new ArrayList<>();

        for (int col = minCoords.getA(); col < maxCoords.getA(); col++) {
            for (int row = minCoords.getB(); row < maxCoords.getB(); row++) {
                for (T entry : getGridCellEntries(col, row)) {
                    if (filter == null || filter.test(entry)) {
                        itemsInBoundingBox.add(entry);
                    }
                }
            }
        }
        return itemsInBoundingBox;
    }

    private Set<T> getGridCellEntries(int col, int row) {
        return grid.get(col).get(row).entries;
    }

    class GridCell {
        private final Set<T> entries;

        GridCell() {
            entries = new HashSet<>();
        }

        boolean add(T item) {
            return entries.add(item);
        }

        boolean remove(T item) {
            return entries.remove(item);
        }
    }
}