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

package org.eclipse.mosaic.lib.routing;

import org.eclipse.mosaic.lib.geo.GeoPoint;

/**
 * Class containing parameters for determining the routing position.
 */
public class RoutingPosition {

    private final GeoPoint position;

    private final String connectionID;

    private final Double heading;

    public RoutingPosition(GeoPoint position) {
        this(position, null, null);
    }

    public RoutingPosition(GeoPoint position, Double heading) {
        this(position, heading, null);
    }

    public RoutingPosition(GeoPoint position, Double heading, String connectionID) {
        this.position = position;
        this.heading = heading;
        this.connectionID = connectionID;
    }

    public GeoPoint getPosition() {
        return position;
    }

    public Double getHeading() {
        return heading;
    }

    public String getConnectionID() {
        return connectionID;
    }

    @Override
    public String toString() {
        return "RoutingPosition [position=" + position + ", connectionID=" + connectionID + ", heading=" + heading + "]";
    }
}
