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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionRange;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.SpatialVehicleIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.VehicleObject;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.spatial.BoundingBox;
import org.eclipse.mosaic.lib.spatial.Grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerceptionGrid implements SpatialVehicleIndex {
    /**
     * Stores {@link VehicleObject}s for fast removal and position update.
     */
    private final Map<String, VehicleObject> indexedVehicles = new HashMap<>();

    /**
     * The Grid to be used for spatial search of {@link VehicleObject}s.
     */
    private final Grid<VehicleObject> vehicleGrid;


    public PerceptionGrid(CartesianRectangle bounds, double cellWidth, double cellHeight) {
        BoundingBox boundingArea = new BoundingBox();
        boundingArea.add(bounds.getA().toVector3d(), bounds.getB().toVector3d());
        vehicleGrid = new Grid<>(new VehicleObjectAdapter(), cellWidth, cellHeight, boundingArea);
    }

    @Override
    public List<VehicleObject> getVehiclesInRange(PerceptionRange searchRange) {
        return vehicleGrid.getItemsInBoundingArea(searchRange.getBoundingBox(), searchRange::isInRange);
    }

    @Override
    public void removeVehicles(Iterable<String> vehiclesToRemove) {
        vehiclesToRemove.forEach(v -> {
            VehicleObject vehicleObject = indexedVehicles.remove(v);
            if (vehicleObject != null) {
                vehicleGrid.removeItem(vehicleObject);
            }
        });
    }

    @Override
    public void updateVehicles(Iterable<VehicleData> vehiclesToUpdate) {
        vehiclesToUpdate.forEach(v -> {
            VehicleObject vehicleObject = indexedVehicles.get(v.getName());
            if (vehicleObject == null) {
                vehicleObject = new VehicleObject(v.getName()).setPosition(v.getProjectedPosition());
                if (vehicleGrid.addItem(vehicleObject)) {
                    indexedVehicles.put(v.getName(), vehicleObject);
                }
            }
            vehicleObject
                    .setHeading(v.getHeading())
                    .setSpeed(v.getSpeed())
                    .setPosition(v.getProjectedPosition());

        });
        vehicleGrid.updateGrid();
    }

    @Override
    public int getNumberOfVehicles() {
        return indexedVehicles.size();
    }
}
