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
 *
 * Contact: mosaic@fokus.fraunhofer.de
 */

package org.eclipse.mosaic.app.tutorial.vehicle;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is a simple application to react on a vehicle info update.
 * Mostly a vehicle info will be triggered from a traffic simulator (e.g. SUMO).
 */
public class OnVehicleUpdatedApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {

    /**
     * This method is called in each simulation step and thereby is dependent from the simulation update interval.
     * You can, for example, change the SUMO update interval to 400ms by adding "updateInterval" : 400" (with quotation marks) to
     * sumo_config.json in the sumo folder in the MOSAIC scenario folder structure.
     */
    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        getLog().infoSimTime(this, "Called #onVehicleUpdated");

        // log out new position
        final String latitude = String.format("%.5f", updatedVehicleData.getPosition().getLatitude());
        final String longitude = String.format("%.5f", updatedVehicleData.getPosition().getLongitude());
        getLog().infoSimTime(this, "Current position: ({},{})", latitude, longitude);
    }

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "Startup");
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown");
    }

    @Override
    public void processEvent(final Event event) throws Exception {
        getLog().infoSimTime(this, "Scheduled event at time {}", TIME.format(event.getTime()));
    }

}
