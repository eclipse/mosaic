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

package org.eclipse.mosaic.app.tutorial.interunitcommunication;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.MosaicApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.application.ApplicationInteraction;
import org.eclipse.mosaic.lib.objects.traffic.SumoTraciResult;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is a simple application to send a MOSAIC-interaction to all simulators.
 * This application could also react on {@link ApplicationInteraction}s.
 */
public class MosaicInteractionHandlingApp
        extends AbstractApplication<VehicleOperatingSystem>
        implements VehicleApplication, MosaicApplication {

    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        // when no unitId is given, broadcast this message to all applications on all running units
        final String uniqueVehicle = "veh_3";
        final MyInteraction mySpamInteraction = new MyInteraction(
                getOs().getSimulationTime(),
                null,
                "MosaicInteractionHandlingApp from " + getOs().getId() + " sends this message to all running units."
        );
        final MyInteraction myPersonalInteraction = new MyInteraction(
                getOs().getSimulationTime(),
                uniqueVehicle,
                "Only " + uniqueVehicle + " should receive this message from MosaicInteractionHandlingApp from " + getOs().getId()
        );
        getOs().sendInteractionToRti(mySpamInteraction);
        getOs().sendInteractionToRti(myPersonalInteraction);
    }

    @Override
    public void onInteractionReceived(ApplicationInteraction applicationInteraction) {
        if (applicationInteraction instanceof MyInteraction) {
            final MyInteraction myMessage = (MyInteraction) applicationInteraction;
            getLog().infoSimTime(this, "MosaicInteractionHandlingApp received MyInteraction: {}", myMessage.getContent());
        }
    }

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "MosaicInteractionHandlingApp has started on" + getOs().getId());
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown");
    }

    @Override
    public void processEvent(Event event) throws Exception {
    }

    @Override
    public void onSumoTraciResponded(SumoTraciResult sumoTraciResult) {
    }

}
