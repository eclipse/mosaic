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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModuleOwner;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.SimplePerceptionConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.SimplePerceptionModule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.TrafficObjectIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Trivial implementation of {@link TrafficObjectIndex}, which uses a for loop to solve the range query.
 */
public class VehicleMap extends VehicleIndex {

    @Override
    public void initialize() {
        // nothing to initialize
    }

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
        vehiclesToUpdate.forEach(v -> {
                    if (SimulationKernel.SimulationKernel.getCentralPerceptionComponent().getScenarioBounds()
                            .contains(v.getProjectedPosition())) { // check if inside bounding area
                        VehicleObject currentVehicle = indexedVehicles.computeIfAbsent(v.getName(), VehicleObject::new)
                                .setHeading(v.getHeading())
                                .setSpeed(v.getSpeed())
                                .setPosition(v.getProjectedPosition());
                        if (!currentVehicle.isInitialized()) { // if this is the first update for a vehicle set initialized
                            currentVehicle.setInitialized();
                        }
                        if (v.getRoadPosition() != null) {
                            currentVehicle.setEdgeAndLane(v.getRoadPosition().getConnectionId(), v.getRoadPosition().getLaneIndex());
                        }
                    } else {
                        indexedVehicles.remove(v.getName());
                    }
                }
        );
    }

    @Override
    public PerceptionModule<SimplePerceptionConfiguration> createPerceptionModule(PerceptionModuleOwner owner, Database database, Logger log) {
        return new SimplePerceptionModule(owner, database, log);
    }
}
