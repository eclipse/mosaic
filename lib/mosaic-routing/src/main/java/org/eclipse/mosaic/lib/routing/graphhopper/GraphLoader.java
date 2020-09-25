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
 *
 * Contact: mosaic@fokus.fraunhofer.de
 */

package org.eclipse.mosaic.lib.routing.graphhopper;

import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;

/**
 * Encapsulates the import procedure of a MOSAIC scenario database into a GraphHopper readable GraphStorage.
 */
public interface GraphLoader {

    /**
     * Initializes the import process.
     *
     */
    void initialize(GraphHopperStorage graph, EncodingManager encodingManager, GraphhopperToDatabaseMapper mapper);

    /**
     * Creates a graph.
     */
    void loadGraph();

}
