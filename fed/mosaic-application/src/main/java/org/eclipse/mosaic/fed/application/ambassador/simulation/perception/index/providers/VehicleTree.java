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
import org.eclipse.mosaic.lib.spatial.QuadTree;

import com.google.gson.annotations.Expose;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Quad-tree based implementation of a {@link VehicleIndex}.
 */
public class VehicleTree extends VehicleIndex {
    /**
     * The maximum amount of vehicles in one leaf before it gets split into four sub-leaves.
     * Filled by gson.
     */
    @Expose()
    public int splitSize = 20;

    /**
     * Maximum depth of the quad tree.
     * Filled by gson.
     */
    @Expose()
    public int maxDepth = 12;

    /**
     * The Quad-Tree to be used for spatial search of {@link VehicleObject}s.
     */
    private QuadTree<VehicleObject> vehicleTree;

    /**
     * Configures a QuadTree as spatial index for vehicles on first use.
     */
    @Override
    public void initialize() {
        QuadTree.configure(splitSize, splitSize / 2, maxDepth);
        CartesianRectangle bounds = SimulationKernel.SimulationKernel.getCentralPerceptionComponent().getScenarioBounds();
        BoundingBox boundingArea = new BoundingBox();
        boundingArea.add(bounds.getA().toVector3d(), bounds.getB().toVector3d());
        vehicleTree = new QuadTree<>(new SpatialObjectAdapter<>(), boundingArea);
    }

    @Override
    public List<VehicleObject> getVehiclesInRange(PerceptionModel searchRange) {
        return vehicleTree.getObjectsInBoundingArea(searchRange.getBoundingBox(), searchRange::isInRange, new ArrayList<>());
    }

    @Override
    protected VehicleObject addOrGetVehicle(VehicleData vehicleData) {
        boolean isNew = !indexedVehicles.containsKey(vehicleData.getName());
        VehicleObject vehicleObject = super.addOrGetVehicle(vehicleData);
        if (isNew) {
            vehicleTree.addItem(vehicleObject);
        }
        return vehicleObject;
    }

    @Override
    public void removeVehicles(Iterable<String> vehiclesToRemove) {
        vehiclesToRemove.forEach(v -> {
            VehicleObject vehicleObject = indexedVehicles.remove(v);
            if (vehicleObject != null) {
                vehicleTree.removeObject(vehicleObject);
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
                if (v.getRoadPosition() != null) { // if not inside or left bounding area
                    vehicleObject.setEdgeAndLane(v.getRoadPosition().getConnectionId(), v.getRoadPosition().getLaneIndex());
                }
            } else { // if not inside or left bounding area
                VehicleObject vehicleObject = indexedVehicles.remove(v.getName());
                if (vehicleObject != null) {
                    vehicleTree.removeObject(vehicleObject);
                }
            }
        });
        vehicleTree.updateTree();
    }

    @Override
    public PerceptionModule<SimplePerceptionConfiguration> createPerceptionModule(PerceptionModuleOwner owner, Database database, Logger log) {
        return new SimplePerceptionModule(owner, database, log);
    }
}
