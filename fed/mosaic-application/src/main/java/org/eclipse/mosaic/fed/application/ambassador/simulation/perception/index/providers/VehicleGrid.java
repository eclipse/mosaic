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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObjectAdapter;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.spatial.BoundingBox;
import org.eclipse.mosaic.lib.spatial.Grid;
import org.eclipse.mosaic.lib.util.gson.UnitFieldAdapter;

import com.google.gson.annotations.JsonAdapter;
import org.slf4j.Logger;

import java.util.List;

public class VehicleGrid extends VehicleIndex {

    @JsonAdapter(UnitFieldAdapter.DistanceMeters.class)
    public double cellWidth = 200;

    @JsonAdapter(UnitFieldAdapter.DistanceMeters.class)
    public double cellHeight = 200;

    /**
     * The Grid to be used for spatial search of {@link VehicleObject}s.
     */
    private Grid<VehicleObject> vehicleGrid;

    /**
     * Configures a grid as a spatial index for vehicles.
     */
    @Override
    public void initialize() {
        CartesianRectangle bounds = SimulationKernel.SimulationKernel.getCentralPerceptionComponent().getScenarioBounds();
        BoundingBox boundingArea = new BoundingBox();
        boundingArea.add(bounds.getA().toVector3d(), bounds.getB().toVector3d());
        vehicleGrid = new Grid<>(new SpatialObjectAdapter<>(), cellWidth, cellHeight, boundingArea);

    }

    @Override
    public List<VehicleObject> getVehiclesInRange(PerceptionModel searchRange) {
        return vehicleGrid.getItemsInBoundingArea(searchRange.getBoundingBox(), searchRange::isInRange);
    }

    @Override
    protected VehicleObject addOrGetVehicle(VehicleData vehicleData) {
        boolean isNew = !indexedVehicles.containsKey(vehicleData.getName());
        VehicleObject vehicleObject = super.addOrGetVehicle(vehicleData);
        if (isNew) {
            vehicleGrid.addItem(vehicleObject);
        }
        return vehicleObject;
    }

    @Override
    public void removeVehicles(Iterable<String> vehiclesToRemove) {
        vehiclesToRemove.forEach(v -> {
            VehicleObject vehicleObject = indexedVehicles.remove(v);
            if (vehicleObject != null) {
                vehicleGrid.removeItem(vehicleObject);
            }
            unregisterVehicleType(v);
        });
    }

    @Override
    public void updateVehicles(Iterable<VehicleData> vehiclesToUpdate) {
        vehiclesToUpdate.forEach(v -> {
            CartesianPoint vehiclePosition = v.getProjectedPosition();
            if (SimulationKernel.SimulationKernel.getCentralPerceptionComponent().getScenarioBounds().contains(vehiclePosition)) { // check if inside bounding area
                VehicleObject vehicleObject = addOrGetVehicle(v)
                        .setHeading(v.getHeading())
                        .setSpeed(v.getSpeed())
                        .setPosition(vehiclePosition);
                if (v.getRoadPosition() != null) {
                    vehicleObject.setEdgeAndLane(v.getRoadPosition().getConnectionId(), v.getRoadPosition().getLaneIndex());
                }
            } else { // if not inside or left bounding area
                VehicleObject vehicleObject = indexedVehicles.remove(v.getName());
                if (vehicleObject != null) {
                    vehicleGrid.removeItem(vehicleObject);
                }
            }

        });
        vehicleGrid.updateGrid();
    }

    @Override
    public PerceptionModule<SimplePerceptionConfiguration> createPerceptionModule(PerceptionModuleOwner owner, Database database, Logger log) {
        return new SimplePerceptionModule(owner, database, log);
    }
}
