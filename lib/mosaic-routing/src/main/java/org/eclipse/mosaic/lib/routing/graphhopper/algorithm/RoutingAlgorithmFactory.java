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

package org.eclipse.mosaic.lib.routing.graphhopper.algorithm;

import com.graphhopper.routing.AStarBidirection;
import com.graphhopper.routing.AlternativeRoute;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;

/**
 * Factory to instantiate the routing algorithm to be used.
 */
public interface RoutingAlgorithmFactory {

    RoutingAlgorithmFactory DEFAULT = (graph, weighting, hints) -> {
        if (hints.getInt(Parameters.Algorithms.AltRoute.MAX_PATHS, 1) > 1) {
            hints.putObject("alternative_route.max_share_factor", 0.5)
                    .putObject("alternative_route.max_weight_factor", 2)
                    .putObject("alternative_route.max_exploration_factor", 1.3);
            return new AlternativeRoute(graph, weighting, TraversalMode.EDGE_BASED, hints);
        } else {
            return new AStarBidirection(graph, weighting, TraversalMode.EDGE_BASED);
        }
    };

    RoutingAlgorithmFactory BELLMAN_FORD = BellmanFordRouting::new;

    /**
     * Creates a {@link RoutingAlgorithm} instance for calculating routes based
     * on the given {@link Graph} and {@link Weighting} function.
     *
     * @param graph     the {@link Graph} containing the network to find routes in
     * @param weighting the cost function
     * @return the {@link RoutingAlgorithm}
     */
    RoutingAlgorithm createAlgorithm(Graph graph, Weighting weighting, PMap hints);

}
