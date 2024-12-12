/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.routing.pt;

/**
 * The result of the routing request. Contains the multi-modal route which
 * matches the routing request at best.
 */
public class PtRoutingResponse {

    private final MultiModalRoute bestRoute;

    public PtRoutingResponse(MultiModalRoute bestRoute) {
        this.bestRoute = bestRoute;
    }

    /**
     * Returns the best multi-modal route of the route calculation.
     */
    public final MultiModalRoute getBestRoute() {
        return bestRoute;
    }
}
