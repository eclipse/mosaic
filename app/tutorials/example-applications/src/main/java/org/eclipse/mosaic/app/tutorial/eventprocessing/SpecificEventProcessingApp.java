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

package org.eclipse.mosaic.app.tutorial.eventprocessing;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

/**
 * This is a simple application that shows that we can process events in different methods
 * thereby creating different types of events.
 **/
public class SpecificEventProcessingApp extends AbstractApplication<VehicleOperatingSystem> {

    @Override
    public void onStartup() {
        getLog().info("Events are being scheduled");

        this.getOs().getEventManager().newEvent(getOs().getSimulationTime() + 5 * TIME.SECOND, this)
                .withResource("A message for my specific method!")
                .schedule();

        //using lambda expression to define a specific method for processing this event
        this.getOs().getEventManager().newEvent(getOs().getSimulationTime() + 10 * TIME.SECOND, this::mySpecificMethod)
                .withResource("A message for my specific method!")
                .schedule();
    }


    @Override
    public void processEvent(final Event event) throws Exception {
        getLog().infoSimTime(this, "Event has been triggered and is being processed by the processEvent method that is declared in EventProcessor interface!");

        if (event.getResource() instanceof String) {
            String message = (String)event.getResource();
            getLog().infoSimTime(this, "Received message: \"{}\"", message);
        }
    }

    @Override
    public void onShutdown() {

    }

    public void mySpecificMethod(Event event) {
        getLog().infoSimTime(this, "Event has been triggered and is being processed by a specific method!");

        if (event.getResource() instanceof String) {
            String message = (String)event.getResource();
            getLog().infoSimTime(this, "Received message: \"{}\"", message);
        }
    }
}
