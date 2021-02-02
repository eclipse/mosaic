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

package org.eclipse.mosaic.app.tutorial.eventprocessing.sampling;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

/**
 * This is a simple Hello World application.
 * The app calls a sample() method every second by scheduling a new event at the (current call time + interval)
 * and therefore causing a call of processEvent() method at this time, which in turn calls the sample() method again.
 * Otherwise, despite logging the time when the event was received, the app does nothing.
 */
public class HelloWorldApp extends AbstractApplication<VehicleOperatingSystem> {

    /**
     * Sample interval. Unit: [ns].
     */
    private final static long TIME_INTERVAL = TIME.SECOND;

    private void sample() {
        // create a new simple event (no resource to process and no niceness of the event is given) to sample something in a specific interval
        getOs().getEventManager().addEvent(getOs().getSimulationTime() + TIME_INTERVAL, this);
    }

    @Override
    public void onStartup() { //this method is declared in Application interface
        getLog().infoSimTime(this, "Hello World! I'm a " + getOs().getInitialVehicleType().getName() + ".");
        sample();
    }

    @Override
    public void onShutdown() { // from Application interface
        getLog().infoSimTime(this, "Bye bye World");
    }

    @Override
    public void processEvent(Event event) throws Exception { //this method is declared in EventProcessor interface
        getLog().infoSimTime(this, "I'm still here!");
        
        sample();
    }

}
