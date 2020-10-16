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
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This application is used only as an addition to the InterconnectApp.
 * It doesn't implement processEvent method,
 * thus not being able to receive the message that is sent from InterconnectApp.
 **/
public class NotProcessingApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {

    @Override
    public void processEvent(final Event event) {
       //This application doesn't process scheduled events and therefore doesn't receive any message and doesn't log anything
    }

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "NotProcessingApp has started");
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown");
    }

    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {

    }
}
