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
import java.util.Collections;
import java.util.List;

/**
 * This bounding box can be used for objects that should be treated to have a single-point position and not span into any dimensions.
 */
public class PointBoundingBox implements SpatialObjectBoundingBox {

    /**
     * A singleton list, defining the position of a {@link SpatialObject} as the only corner.
     * This point is in global coordinates.
     */
    private final List<Vector3d> allCorners;
    /**
     * An empty list as a no bounding box is spanned by a single point.
     */
    private final List<Edge<Vector3d>> allEdges;

    public PointBoundingBox(Vector3d position) {
        allCorners = Collections.singletonList(position);
        allEdges = Collections.emptyList();
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
