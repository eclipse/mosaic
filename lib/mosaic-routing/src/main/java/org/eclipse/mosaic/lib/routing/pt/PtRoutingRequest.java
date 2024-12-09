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
    private final GeoPoint startingGeoPoint;
    private final GeoPoint targetGeoPoint;

    private final PtRoutingParameters routingParameters;

    public PtRoutingRequest(long requestTime, GeoPoint from, GeoPoint to) {
        this(requestTime, from, to, new PtRoutingParameters());
    }

    public PtRoutingRequest(long requestTime, GeoPoint from, GeoPoint to, PtRoutingParameters additionalParameters) {
        this.requestTime = requestTime;
        this.startingGeoPoint = from;
        this.targetGeoPoint = to;
        this.routingParameters = additionalParameters;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public GeoPoint getStartingGeoPoint() {
        return startingGeoPoint;
    }

    public GeoPoint getTargetGeoPoint() {
        return targetGeoPoint;
    }

    public PtRoutingParameters getRoutingParameters() {
        return routingParameters;
    }
}
