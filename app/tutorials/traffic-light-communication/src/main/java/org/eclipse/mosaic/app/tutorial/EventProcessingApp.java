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
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;

/**
 * Receiving application that reacts on events passed from another
 * app running on the same vehicle.
 */
public class EventProcessingApp extends AbstractApplication<VehicleOperatingSystem> {
    @Override
    public void processEvent(Event event) {
        Object resource = event.getResource();
        if (resource != null) {
            if (resource instanceof IntraVehicleMsg) {
                final IntraVehicleMsg message = (IntraVehicleMsg) resource;
                // message was passed from another app on the same vehicle
                if (message.getOrigin().equals(getOs().getId())) {
                    getLog().infoSimTime(this, "Received message from another application: {}", message.toString());
                }
            }
        }
    }

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "Initialize application");
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

}
