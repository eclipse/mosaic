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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * {@link Grid} represents data structure to efficiently store spatial objects on the 2D X,Z plane using a fixed grid of cells.
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

    private final List<List<GridCell<T>>> grid;
    private final Map<T, CellIndex> items = new HashMap<>();

    private final CellIndex tmpIndexA = new CellIndex();
    private final CellIndex tmpIndexB = new CellIndex();

    public Grid(final SpatialItemAdapter<T> adapter, double cellWidth, double cellHeight, final BoundingBox gridBounds) {
        this(adapter, cellWidth, cellHeight, gridBounds.min.x, gridBounds.max.x, gridBounds.min.z, gridBounds.max.z);
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

        grid = new ArrayList<>(colAmount);
        for (int col = 0; col < colAmount; col++) {
            grid.add(new ArrayList<>(rowAmount));
            for (int row = 0; row < rowAmount; row++) {
                grid.get(col).add(new GridCell<>());
            }
        }
    }

    /**
     * Searches all objects within the given bounding area.
     *
     * @param area   the rectangle area for range search
     * @param filter a predicate to exclude certain objects from the result list
     * @return the list of results
     */
    public List<T> getItemsInBoundingArea(BoundingBox area, Predicate<T> filter) {
        return getItemsInBoundingArea(area, filter, new ArrayList<>());
    }

    /**
     * Searches all objects within the given bounding area.
     *
     * @param area   the rectangle area for range search
     * @param filter a predicate to exclude certain objects from the result list
     * @param result the list of results
     * @return the list of results
     */
    public List<T> getItemsInBoundingArea(BoundingBox area, Predicate<T> filter, List<T> result) {
        synchronized (tmpIndexA) {
            CellIndex minIndex = toCellIndex(Math.max(area.min.x, minX), Math.max(area.min.z, minZ), tmpIndexA);
            CellIndex maxIndex = toCellIndex(Math.min(area.max.x, maxX), Math.min(area.max.z, maxZ), tmpIndexB);

            for (int col = minIndex.col; col <= maxIndex.col; col++) {
                for (int row = minIndex.row; row <= maxIndex.row; row++) {
                    final GridCell<T> cell = getGridCell(col, row);
                    // iterating with classical for-loop is faster than foreach
                    for (int i = 0; i < cell.size(); i++) {
                        T item = cell.get(i);
                        if (area.contains(adapter.getCenterX(item), adapter.getCenterY(item), adapter.getCenterZ(item))
                                && (filter == null || filter.test(item))) {
                            result.add(item);
                        }
                    }
                }
            }
            return result;
        }
    }

    /**
     * Adds or updates an item in the grid.
     *
     * @param item the item to be added
     * @return true if the item has been added to the grid, false if it has already been present in the grid
     */
    public boolean addItem(T item) {
        synchronized (tmpIndexA) {
            CellIndex newCellIndex = toCellIndex(adapter.getCenterX(item), adapter.getCenterZ(item), new CellIndex());
            CellIndex oldCellIndex = items.put(item, newCellIndex);
            if (oldCellIndex != null) {
                getGridCell(oldCellIndex).remove(item);
                return false;
            }
            getGridCell(newCellIndex).add(item);
            return true;
        }
    }

    public void updateGrid() {
        synchronized (tmpIndexA) {
            items.forEach((item, currentIndex) -> {
                CellIndex newCellIndex = toCellIndex(adapter.getCenterX(item), adapter.getCenterZ(item), tmpIndexA);
                if (newCellIndex.isEqualTo(currentIndex)) {
                    // no index change -> do nothing
                    return;
                }
                getGridCell(currentIndex).remove(item);
                currentIndex.set(newCellIndex);
                getGridCell(currentIndex).add(item);
            });
        }
    }

    public void removeItem(T item) {
        synchronized (tmpIndexA) {
            CellIndex cellIndex = items.remove(item);
            if (cellIndex != null) {
                getGridCell(cellIndex).remove(item);
            }
        }
    }

    private CellIndex toCellIndex(double x, double z, CellIndex resultIndex) {
        // also looking at special case where item is directly on the max borders
        resultIndex.col = x < minX ? 0 : x >= maxX ? colAmount - 1 : (int) ((x - minX) / cellWidth);
        resultIndex.row = z < minZ ? 0 : z >= maxZ ? rowAmount - 1 : (int) ((z - minZ) / cellHeight);
        return resultIndex;
    }

    private GridCell<T> getGridCell(CellIndex cellIndex) {
        return getGridCell(cellIndex.col, cellIndex.row);
    }

    private GridCell<T> getGridCell(int col, int row) {
        return grid.get(col).get(row);
    }

    private static class GridCell<T> extends ArrayList<T> {
        // marker class for better readability
        // we use an arrayList since efficiently iterating over all elements is more important than remove/add
    }

    private static class CellIndex {
        private int row;
        private int col;

        private CellIndex set(CellIndex copyFrom) {
            this.row = copyFrom.row;
            this.col = copyFrom.col;
            return this;
        }

        public boolean isEqualTo(CellIndex other) {
            return other != null && this.row == other.row && this.col == other.col;
        }
    }
}