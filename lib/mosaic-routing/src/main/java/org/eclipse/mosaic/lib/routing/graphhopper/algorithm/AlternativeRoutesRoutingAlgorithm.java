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
import com.graphhopper.routing.RoutingAlgorithm;

import java.util.List;

/**
 * Mark an routing algorithm which is able to generate alternative routes.
 */
public interface AlternativeRoutesRoutingAlgorithm extends RoutingAlgorithm {

    /**
     * Defines the number of additional alternative routes to calculate.
     *
     * @param alternatives the number of alternative routes.
     */
    void setRequestAlternatives(int alternatives);

    /**
     * Returns all alternative paths excluding the best path.
     *
     * @return all alternative paths
     */
    List<Path> getAlternativePaths();

}
