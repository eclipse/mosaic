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

package org.eclipse.mosaic.fed.application.ambassador.simulation.navigation;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.app.api.navigation.PtRoutingModule;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.routing.pt.PtRoutingParameters;
import org.eclipse.mosaic.lib.routing.pt.PtRoutingRequest;
import org.eclipse.mosaic.lib.routing.pt.PtRoutingResponse;

public class AgentPtRoutingModule implements PtRoutingModule {

    private final double defaultWalkingSpeed;

    public AgentPtRoutingModule(double defaultWalkingSpeed) {
        this.defaultWalkingSpeed = defaultWalkingSpeed;
    }

    @Override
    public PtRoutingResponse calculateRoute(long requestTime, GeoPoint origin, GeoPoint destination, PtRoutingParameters routingParameters) {
        PtRoutingRequest routingRequest = new PtRoutingRequest(requestTime, origin, destination, routingParameters);
        if (routingParameters.getWalkingSpeedMps() == null) {
            routingParameters.walkingSpeedMps(defaultWalkingSpeed);
        }
        return SimulationKernel.SimulationKernel.getCentralNavigationComponent().findPtRoute(routingRequest);
    }
}
