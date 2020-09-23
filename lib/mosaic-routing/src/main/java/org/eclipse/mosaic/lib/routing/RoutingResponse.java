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

package org.eclipse.mosaic.lib.routing;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the response for a routing request.
 * Gathers information in one object.
 */
public class RoutingResponse {

    private final CandidateRoute bestRoute;

    private final List<CandidateRoute> alternativeRoutes = new ArrayList<>();

    /**
     * Creates a routing response to the routing request with the best {@link CandidateRoute} and the alternative routes.
     *
     * @param bestRoute         The best route based on the cost function.
     * @param alternativeRoutes List of alternative routes.
     */
    public RoutingResponse(CandidateRoute bestRoute, List<CandidateRoute> alternativeRoutes) {
        this.bestRoute = bestRoute;
        this.alternativeRoutes.addAll(alternativeRoutes);
    }

    /**
     * Returns alternative route(s) excluding the best one.
     *
     * @return List for alternative routes in addition to the best route.
     */
    public final List<CandidateRoute> getAlternativeRoutes() {
        return alternativeRoutes;
    }

    /**
     * Returns the "best" route in regards to the given cost function.
     * Alternative routes can be obtained via getAlternativeRoutes().
     *
     * @return The best route in terms of costs.
     */
    public final CandidateRoute getBestRoute() {
        return bestRoute;
    }
}
