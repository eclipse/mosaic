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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.TrafficObjectIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.util.VehicleIndexTypeAdapterFactory;
import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;

import com.google.gson.annotations.JsonAdapter;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonAdapter(VehicleIndexTypeAdapterFactory.class)
public abstract class VehicleIndex implements Serializable {

    /**
     * Stores {@link VehicleObject}s for fast removal and position update.
     */
    final Map<String, VehicleObject> indexedVehicles = new HashMap<>();

    /**
     * Stores the types of vehicles to extract vehicle dimensions.
     */
    private final Map<String, String> registeredVehicleTypes = new HashMap<>();

    /**
     * Adds a vehicle to the index if it hasn't been added to keep track of its dimensions.
     *
     * @param vehicleData the data containing information about the vehicle
     */
    protected VehicleObject addOrGetVehicle(VehicleData vehicleData) {
        String vehicleId = vehicleData.getName();
        VehicleObject vehicleObject = indexedVehicles.get(vehicleId);
        if (vehicleObject == null) {
            VehicleType vehicleType = SimulationKernel.SimulationKernel.getVehicleTypes().get(registeredVehicleTypes.get(vehicleId));
            vehicleObject = new VehicleObject(vehicleId)
                    .setHeading(vehicleData.getHeading())
                    .setSpeed(vehicleData.getSpeed())
                    .setPosition(vehicleData.getProjectedPosition());

            if (vehicleType != null) {
                vehicleObject.setDimensions(
                        vehicleType.getLength(),
                        vehicleType.getWidth(),
                        vehicleType.getHeight()
                );
            }
            indexedVehicles.put(vehicleId, vehicleObject);
        }
        return vehicleObject;
    }

    /**
     * Returns the amount of indexed vehicles.
     *
     * @return the number of vehicles
     */
    public int getNumberOfVehicles() {
        return indexedVehicles.size();
    }

    /**
     * Method called to initialize index after configuration has been read.
     */
    public abstract void initialize();

    /**
     * Queries the {@link TrafficObjectIndex} and returns all vehicles inside the {@link PerceptionModel}.
     */
    public abstract List<VehicleObject> getVehiclesInRange(PerceptionModel perceptionModel);

    public void registerVehicleType(String vehicleId, String vehicleTypeName) {
        registeredVehicleTypes.put(vehicleId, vehicleTypeName);
    }

    /**
     * Remove all vehicles from the {@link TrafficObjectIndex} by a list of vehicle ids.
     *
     * @param vehiclesToRemove the list of vehicles to remove from the index
     */
    public abstract void removeVehicles(Iterable<String> vehiclesToRemove);

    /**
     * Updates the {@link TrafficObjectIndex} with a list of {@link VehicleData} objects.
     *
     * @param vehiclesToUpdate the list of vehicles to add or update in the index
     */
    public abstract void updateVehicles(Iterable<VehicleData> vehiclesToUpdate);

    /**
     * Creates the perception module to be used for perception purposes. Allows for the implementation of different
     * provider sources.
     *
     * @param owner    the unit the perception module belongs to
     * @param database the database for the scenario
     * @param log      the logger
     * @return an instantiated perception module
     */
    public abstract PerceptionModule<SimplePerceptionConfiguration> createPerceptionModule(PerceptionModuleOwner owner, Database database, Logger log);
}
