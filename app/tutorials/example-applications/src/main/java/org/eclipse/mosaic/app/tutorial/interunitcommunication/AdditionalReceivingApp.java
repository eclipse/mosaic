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
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.application.ApplicationInteraction;
import org.eclipse.mosaic.lib.objects.traffic.SumoTraciResult;
import org.eclipse.mosaic.lib.util.scheduling.Event;

/**
 * This application is used only as an additional demonstration of MosaicInteractionHandlingApp's work.
 * It receives MyInteraction (inherits from ApplicationInteraction) sent by MosaicInteractionHandlingApp and logs its content.
 **/
public class AdditionalReceivingApp extends AbstractApplication<VehicleOperatingSystem> implements MosaicApplication {

    @Override
    public void onInteractionReceived(ApplicationInteraction applicationInteraction) {
        if (applicationInteraction instanceof MyInteraction) {
            final MyInteraction myMessage = (MyInteraction) applicationInteraction;
            getLog().infoSimTime(this, "AdditionalReceivingApp received MyInteraction: {}", myMessage.getContent());
        }
    }

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "AdditionalReceivingApp has started on" + getOs().getId());
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown");
    }

    @Override
    public void processEvent(Event event) throws Exception {

    }

    @Override
    public void onSumoTraciResponded(SumoTraciResult sumoTraciResult) {}

}
