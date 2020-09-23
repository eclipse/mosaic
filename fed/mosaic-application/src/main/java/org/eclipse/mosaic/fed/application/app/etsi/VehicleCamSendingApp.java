/*
 * Copyright (c) 2020 Fraunhofer FOKUS and others. All rights reserved.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.mosaic.fed.application.app.etsi;

import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

/**
 * ETSI conform application for vehicles.
 */
public class VehicleCamSendingApp extends AbstractCamSendingApp<VehicleOperatingSystem> {

    @Override
    public Data generateEtsiData() {
        VehicleData vehicleData = getOperatingSystem().getNavigationModule().getVehicleData();
        // failsafe
        if (vehicleData == null) {
            return null;
        }

        final Data myData = new Data();
        myData.heading = vehicleData.getHeading();
        myData.time = getOperatingSystem().getSimulationTime();
        myData.position = vehicleData.getPosition();
        myData.projectedPosition = vehicleData.getProjectedPosition();
        myData.velocity = vehicleData.getSpeed();
        return myData;
    }
}
