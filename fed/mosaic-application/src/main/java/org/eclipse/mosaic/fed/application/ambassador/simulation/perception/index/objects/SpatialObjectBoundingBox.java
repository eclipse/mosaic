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

import java.util.List;

public interface SpatialObjectBoundingBox {

    /**
     * Returns all corners spanning the 2D bounding box of a {@link SpatialObject}.
     */
    List<Vector3d> getAllCorners();

    /**
     * Returns all sides spanning the 2D bounding box of a {@link SpatialObject}.
     */
    List<Edge<Vector3d>> getAllEdges();
}
