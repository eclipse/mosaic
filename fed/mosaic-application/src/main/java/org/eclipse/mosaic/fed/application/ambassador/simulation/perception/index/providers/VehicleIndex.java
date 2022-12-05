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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModuleOwner;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.SimplePerceptionConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.TrafficObjectIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.util.VehicleIndexProviderTypeAdapterFactory;
import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import com.google.gson.annotations.JsonAdapter;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.List;

@JsonAdapter(VehicleIndexProviderTypeAdapterFactory.class)
public interface VehicleIndex extends Serializable {

    /**
     * Method called to initialize index after configuration has been read.
     */
    void initialize();

    /**
     * Queries the {@link TrafficObjectIndex} and returns all vehicles inside the {@link PerceptionModel}.
     */
    List<VehicleObject> getVehiclesInRange(PerceptionModel perceptionModel);

    /**
     * Remove all vehicles from the {@link TrafficObjectIndex} by a list of vehicle ids.
     *
     * @param vehiclesToRemove the list of vehicles to remove from the index
     */
    void removeVehicles(Iterable<String> vehiclesToRemove);

    /**
     * Updates the {@link TrafficObjectIndex} with a list of {@link VehicleData} objects.
     *
     * @param vehiclesToUpdate the list of vehicles to add or update in the index
     */
    void updateVehicles(Iterable<VehicleData> vehiclesToUpdate);

    /**
     * Returns the amount of indexed vehicles.
     *
     * @return the number of vehicles
     */
    int getNumberOfVehicles();

    /**
     * Creates the perception module to be used for perception purposes. Allows for the implementation of different
     * provider sources.
     *
     * @param owner    the unit the perception module belongs to
     * @param database the database for the scenario
     * @param log      the logger
     * @return an instantiated perception module
     */
    PerceptionModule<SimplePerceptionConfiguration> createPerceptionModule(PerceptionModuleOwner owner, Database database, Logger log);
}
