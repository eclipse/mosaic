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

import java.util.ArrayList;
import java.util.List;

/**
 * A walking leg contains a linestring of geographical points which form the walking route.
 */
public class WalkLeg {

    private final List<GeoPoint> waypoints = new ArrayList<>();

    public WalkLeg(List<GeoPoint> waypoints) {
        this.waypoints.addAll(waypoints);
    }

    /**
     * Returns the list of geographical positions which form the walking route.
     */
    public final List<GeoPoint> getWaypoints() {
        return waypoints;
    }
}
