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

package org.eclipse.mosaic.lib.routing.graphhopper.algorithm;

import com.graphhopper.routing.Path;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;

/**
 * Extension of {@link Path} providing access to the shortest path tree entry ({@link SPTEntry}).
 */
class PlateauPath extends Path {

    PlateauPath(Graph graph, Weighting weighting) {
        super(graph, weighting);
    }

    SPTEntry getSptEntry() {
        return sptEntry;
    }
}
