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

package org.eclipse.mosaic.app.tutorial;

import org.eclipse.mosaic.app.tutorial.message.IntraVehicleMsg;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.Application;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EventSendingApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {
    /**
     * Used for choosing a RAND id for the message that is sent intra-vehicle.
     */
    private final static int MAX_ID = 1000;

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "Initialize application");
    }

    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        final List<? extends Application> applications = getOs().getApplications();
        final IntraVehicleMsg message = new IntraVehicleMsg(getOs().getId(), getRandom().nextInt(0, MAX_ID));

        for (Application application : applications) {
            final Event event = new Event(getOs().getSimulationTime() + 10, application, message);
            this.getOs().getEventManager().addEvent(event);
        }
    }

    @Override
    public void processEvent(Event event) throws Exception {
        getLog().infoSimTime(this, "Received event: {}", event.getResourceClassSimpleName());
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown application");
    }
}
