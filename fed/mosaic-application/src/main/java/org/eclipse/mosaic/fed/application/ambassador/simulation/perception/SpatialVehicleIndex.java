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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import java.util.List;

/**
 * A {@link SpatialVehicleIndex} is a representation of space using a special data structure.
 * The goal is to allow for efficient querying of nearby entities.
 */
public interface SpatialVehicleIndex {

    /**
     * Queries the {@link SpatialVehicleIndex} and returns all vehicles inside the {@link PerceptionModel}
     * e.
     */
    List<VehicleObject> getVehiclesInRange(PerceptionModel searchRange);

    /**
     * Remove all vehicles from the {@link SpatialVehicleIndex} by a list of vehicle ids.
     *
     * @param vehiclesToRemove the list of vehicles to remove from the index
     */
    void removeVehicles(Iterable<String> vehiclesToRemove);

    /**
     * Updates the {@link SpatialVehicleIndex} with a list of {@link VehicleData} objects.
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
