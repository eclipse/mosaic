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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.SpatialVehicleIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.VehicleObject;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trivial implementation of {@link SpatialVehicleIndex}, which uses a for loop to solve the range query.
 */
public class PerceptionIndex implements SpatialVehicleIndex {

    private final Map<String, VehicleObject> indexedVehicles = new HashMap<>();

    @Override
    public List<VehicleObject> getVehiclesInRange(PerceptionModel searchRange) {
        return indexedVehicles.values().stream()
                .filter(searchRange::isInRange)
                .collect(Collectors.toList());
    }

    @Override
    public void removeVehicles(Iterable<String> vehiclesToRemove) {
        vehiclesToRemove.forEach(indexedVehicles::remove);
    }

    @Override
    public void updateVehicles(Iterable<VehicleData> vehiclesToUpdate) {
        vehiclesToUpdate.forEach(
                (v) -> indexedVehicles.computeIfAbsent(v.getName(), VehicleObject::new)
                        .setHeading(v.getHeading())
                        .setSpeed(v.getSpeed())
                        .setPosition(v.getProjectedPosition())
        );
    }

    @Override
    public int getNumberOfVehicles() {
        return indexedVehicles.size();
    }
}
