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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.SpatialIndexProvider;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.util.VehicleIndexProviderTypeAdapterFactory;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import com.google.gson.annotations.JsonAdapter;

import java.io.Serializable;
import java.util.List;

@JsonAdapter(VehicleIndexProviderTypeAdapterFactory.class)
public interface VehicleIndexProvider extends Serializable {

    /**
     * Method called to initialize index after configuration has been read.
     */
    void initialize();

    /**
     * Queries the {@link SpatialIndexProvider} and returns all vehicles inside the {@link PerceptionModel}.
     */
    List<VehicleObject> getVehiclesInRange(PerceptionModel perceptionModel);

    /**
     * Remove all vehicles from the {@link SpatialIndexProvider} by a list of vehicle ids.
     *
     * @param vehiclesToRemove the list of vehicles to remove from the index
     */
    void removeVehicles(Iterable<String> vehiclesToRemove);

    /**
     * Updates the {@link SpatialIndexProvider} with a list of {@link VehicleData} objects.
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
}
