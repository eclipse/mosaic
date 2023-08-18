/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects;

import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Edge;

import java.util.ArrayList;
import java.util.List;

/**
 * This bounding box represents
 */
public class PointBoundingBox implements SpatialObjectBoundingBox {

    /**
     * A one element list, defining the position of a {@link SpatialObject} as the only corner.
     */
    private final List<Vector3d> allCorners = new ArrayList<>();
    /**
     * An empty list as a no bounding box is spanned by a single point.
     */
    private final List<Edge<Vector3d>> allEdges = new ArrayList<>();

    public PointBoundingBox(Vector3d position) {
        allCorners.add(position);
    }

    @Override
    public List<Vector3d> getAllCorners() {
        return allCorners;
    }

    @Override
    public List<Edge<Vector3d>> getAllEdges() {
        return allEdges;
    }
}
