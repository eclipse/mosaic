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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Edge;

import java.util.Collection;

/**
 * Marks the perception module to be able to return a collection of walls in the vicinity of
 * the ego vehicle. We use this extra interface to hide this method from the API
 * the application developer sees.
 */
public interface WallProvider {

    /**
     * @return a list of walls in the surroundings of the ego vehicle (all walls within the viewing range)
     */
    Collection<Edge<Vector3d>> getSurroundingWalls();
}
