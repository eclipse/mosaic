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

import org.eclipse.mosaic.lib.geo.GeoPoint;

public class PtRoutingRequest {

    private final long requestTime;
    private final GeoPoint origin;
    private final GeoPoint destination;

    private final PtRoutingParameters routingParameters;

    /**
     * Constructs a request for calculating a public transport route.
     *
     * @param requestTime The earliest time to start the journey.
     * @param origin The geographic location to start the journey.
     * @param destination The geographic location to end the journey.
     */
    public PtRoutingRequest(long requestTime, GeoPoint origin, GeoPoint destination) {
        this(requestTime, origin, destination, new PtRoutingParameters());
    }

    /**
     * Constructs a request for calculating a public transport route.
     *
     * @param requestTime The earliest time to start the journey.
     * @param origin The geographic location to start the journey.
     * @param destination The geographic location to end the journey.
     * @param additionalParameters Additional parameters, such as walking speed.
     */
    public PtRoutingRequest(long requestTime, GeoPoint origin, GeoPoint destination, PtRoutingParameters additionalParameters) {
        this.requestTime = requestTime;
        this.origin = origin;
        this.destination = destination;
        this.routingParameters = additionalParameters;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public GeoPoint getOrigin() {
        return origin;
    }

    public GeoPoint getDestination() {
        return destination;
    }

    public PtRoutingParameters getRoutingParameters() {
        return routingParameters;
    }
}
