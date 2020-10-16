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

package org.eclipse.mosaic.app.tutorial.interappcommunication;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.Application;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is a simple application to demonstrate an interconnection between
 * applications which are running on same units.
 * The approach works via the operating system which has the ability to
 * query all running applications on its unit.
 * After that, a new event is created that has the other application as receiver.
 */
public class InterconnectApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {

    /**
     * This method is called in each simulation step and thereby is dependent from the simulation update interval.
     * You can, for example, change the SUMO update interval to 400ms by adding "updateInterval" : 400" (with quotation marks) to
     * sumo_config.json in the sumo folder in the MOSAIC scenario folder structure.
     */
    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        String myMessage = "Hello from application " + this.getClass().getSimpleName() + " (set up on " + getOs().getId() + ")";
        List<? extends Application> applications = getOs().getApplications();
        for (Application application : applications) {
            //you won't get anything logged from the receiving application if it doesn't implement processEvent method (see an implementation example below)
            //so you can end up only with the Interconnect application receiving the message from itself if you don't have other applications that processing events
            this.getOs().getEventManager()
                    .newEvent(this.getOs().getSimulationTime() + 1, application)
                    .withResource(myMessage).schedule();
        }
    }

    @Override
    public void processEvent(final Event event) {
        Object resource = event.getResource();
        if (resource instanceof String) {
            String myMessage = (String) resource;
            getLog().infoSimTime(this, "Received message: \"{}\"", myMessage);
        }
    }

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "InterconnectApp has started");
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown");
    }

}
