/*
 * Copyright (c) 2025 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.app.api.navigation;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.routing.pt.PtRoutingParameters;
import org.eclipse.mosaic.lib.routing.pt.PtRoutingResponse;

/**
 * Interface to access public transport routing functionalities for agents.
 */
public interface PtRoutingModule {

    /**
     * Calculates a public transport route from the provided origin to the provided destination position.
     *
     * @param requestTime       The time at which the public transport route should begin earliest.
     * @param origin            The destination position of the required route.
     * @param destination       The destination position of the required route.
     * @param routingParameters Properties defining the way routes are calculated (e.g. number of routes, weighting).
     * @return The response including a public transport route towards the target.
     */
    PtRoutingResponse calculateRoute(long requestTime, GeoPoint origin, GeoPoint destination, PtRoutingParameters routingParameters);

}
