/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.lib;

import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trivial implementation of {@link SpatialIndex}, simply returning all vehicles when {@link #getVehiclesInIndexRange} is queried.
 */
public class PerceptionIndex implements SpatialIndex {

    private final Map<String, VehicleData> simulatedVehicles = new HashMap<>();

    @Override
    public List<VehicleData> getVehiclesInIndexRange(CartesianPoint position, double heading, double viewingDistance, double viewingAngle) {
        return new ArrayList<>(simulatedVehicles.values());
    }

    @Override
    public void update(VehicleUpdates latestUpdates) {
        latestUpdates.getAdded().forEach(added -> simulatedVehicles.put(added.getName(), added));
        latestUpdates.getUpdated().forEach(updated -> simulatedVehicles.put(updated.getName(), updated));
        latestUpdates.getRemovedNames().forEach(simulatedVehicles::remove);
    }
}
