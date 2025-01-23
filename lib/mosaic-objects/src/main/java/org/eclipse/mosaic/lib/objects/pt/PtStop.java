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

package org.eclipse.mosaic.lib.objects.pt;

import org.eclipse.mosaic.lib.geo.GeoPoint;

/**
 * A {@link PtStop} represents a stop along a public transport line or route, e.g., a bus stop or train station.
 * This data structure consists of its geolocation and the planned arrival and departure time at the stop.
 *
 * @param location The geographic location of the stop.
 * @param arrivalTime The time when arriving at this stop. {@code null} for the first stop of a leg.
 * @param departureTime The time when leaving this stop. {@code null} for the last stop of a leg.
 */
public record PtStop(GeoPoint location, Long arrivalTime, Long departureTime) {

}
