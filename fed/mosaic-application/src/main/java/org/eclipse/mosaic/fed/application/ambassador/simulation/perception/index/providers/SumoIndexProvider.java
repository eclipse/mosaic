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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import java.util.List;

/**
 * Marker class.
 * TODO: maybe there is a prettier way to achieve this.
 */
public class SumoIndexProvider implements VehicleIndexProvider {

    @Override
    public void initialize() {
        // nothing to initialize
    }

    @Override
    public List<VehicleObject> getVehiclesInRange(PerceptionModel perceptionModel) {
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
