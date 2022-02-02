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
import org.eclipse.mosaic.lib.spatial.Grid;

import java.util.List;

public class PerceptionGrid extends Grid<VehicleData> implements SpatialVehicleIndex {

    public PerceptionGrid(double cellWidth, double cellHeight, CartesianRectangle bounds) {
        super(cellWidth, cellHeight, bounds);
    }

    @Override
    public List<VehicleObject> getVehiclesInRange(PerceptionRange searchRange) {
        return null;
    }

    @Override
    public void removeVehicles(Iterable<String> vehiclesToRemove) {

    }

    @Override
    public void updateVehicles(Iterable<VehicleData> vehiclesToUpdate) {

    }

    @Override
    public int getNumberOfVehicles() {
        return 0;
    }
}
