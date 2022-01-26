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

import org.eclipse.mosaic.lib.geo.CartesianRectangle;

import java.util.List;

/**
 * {@link Grid} represents a possible DataStructure to store vehicles based on a map
 * efficiently.
 * TODO: needs to be extended
 */
public class Grid<T> {
    private CartesianRectangle bounds;
    private double cellWidth;
    private double cellHeight;

    public Grid(double cellWidth, double cellHeight, CartesianRectangle bounds) {
        this.bounds = bounds;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        double xMax = Math.max(bounds.getA().getX(), bounds.getB().getX());
        double xMin = Math.min(bounds.getA().getX(), bounds.getB().getX());
        double yMax = Math.max(bounds.getA().getY(), bounds.getB().getY());
        double yMin = Math.min(bounds.getA().getY(), bounds.getB().getY());
        long colAmount = (long) Math.ceil((xMax - xMin) / cellWidth);
        long rowAmount = (long) Math.ceil((yMax - yMin) / cellHeight);
    }

    class GridCell {
        List<T> entries;
    }
}