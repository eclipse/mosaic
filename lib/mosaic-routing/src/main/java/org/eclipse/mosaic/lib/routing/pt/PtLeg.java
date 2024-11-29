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

public class PtLeg {

    public record PtStop(GeoPoint location, Long departureTime, Long arrivalTime) {}

    private final List<PtStop> legs = new ArrayList<>();

    public PtLeg(List<PtStop> legs) {
        this.legs.addAll(legs);
    }

    public List<PtStop> getLegs() {
        return legs;
    }

}
