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
 * This is a simple application to demonstrate a sampling in a specific interval.
 * The app works similar to the {@link HelloWorldApp}, the only difference is
 * that a random offset is chosen and sampling interval has been doubled.
 */
public class IntervalSamplingApp extends AbstractApplication<VehicleOperatingSystem> {

    private static final long SAMPLING_INTERVAL = 2 * TIME.SECOND;

    private long timeOffset;

    @Override
    public void onStartup() {
        // Each vehicle will get a random offset, which can be different from another vehicle's offset.
        this.timeOffset = this.getRandom().nextLong(100 * TIME.MILLI_SECOND, 1 * TIME.SECOND);

        getLog().infoSimTime(this, "Set time offset for first event to {}", TIME.format(this.timeOffset));
        getLog().infoSimTime(this, "Set sampling interval to {}", TIME.format(IntervalSamplingApp.SAMPLING_INTERVAL));
        sampleWithOffset();
    }

    private void sampleWithOffset() {
        this.getOs().getEventManager().addEvent(getOs().getSimulationTime() + this.timeOffset, this);
    }


    private void sample() {
        this.getOs().getEventManager().addEvent(getOs().getSimulationTime() + SAMPLING_INTERVAL, this);
    }

    @Override
    public void processEvent(final Event event) throws Exception {
        getLog().infoSimTime(this, "Processing received event...");
        sample();
    }

    @Override
    public void onShutdown() {

    }

}
