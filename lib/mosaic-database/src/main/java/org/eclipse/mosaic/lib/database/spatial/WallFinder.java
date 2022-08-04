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

package org.eclipse.mosaic.lib.database.spatial;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.building.Building;
import org.eclipse.mosaic.lib.database.building.Wall;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Edge;
import org.eclipse.mosaic.lib.spatial.KdTree;
import org.eclipse.mosaic.lib.spatial.SpatialItemAdapter;
import org.eclipse.mosaic.lib.spatial.SpatialTreeTraverser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This wall finder provides a quick spatial search to find all walls of buildings
 * which can be found in the requested circular area.
 */
public class WallFinder {

    private final KdTree<Edge<Vector3d>> wallTree;
    private final SpatialTreeTraverser.InRadius<Edge<Vector3d>> wallSearch;

    public WallFinder(Database db) {
        List<Edge<Vector3d>> walls = new ArrayList<>();
        for (Building building : db.getBuildings()) {
            for (Wall wall : building.getWalls()) {
                walls.add(new Edge<>(
                        wall.getFromCorner().getCartesianPosition().toVector3d(),
                        wall.getToCorner().getCartesianPosition().toVector3d()
                ));
            }
        }
        wallTree = new KdTree<>(new SpatialItemAdapter.EdgeAdapter<>(), walls);
        wallSearch = new SpatialTreeTraverser.InRadius<>();
    }

    /**
     * Finds and returns all walls in the vicinity of the given center point, which are
     * within the given range.
     *
     * @param center the center point to search for walls
     * @param range the maximum distance from the center points to search for walls
     *
     * @return a list of edges (pair of {@link Vector3d}) which represent the found walls
     */
    public Collection<Edge<Vector3d>> getWallsInRadius(Vector3d center, double range) {
        wallSearch.setup(center, range);
        wallSearch.traverse(wallTree);
        return wallSearch.getResult();
    }
}
