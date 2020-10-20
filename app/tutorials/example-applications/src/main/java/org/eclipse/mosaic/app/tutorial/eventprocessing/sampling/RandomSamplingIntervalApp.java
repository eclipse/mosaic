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
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

/**
 * This is a simple application to demonstrate a sampling in a random interval.
 */
public class RandomSamplingIntervalApp extends AbstractApplication<VehicleOperatingSystem> {

    private static long generateNewOffset(final RandomNumberGenerator rng) {
        // generate a random number, minimum value is 0,9 second, max value is 1,0 second
        return rng.nextLong(900 * TIME.MILLI_SECOND, TIME.SECOND);
    }

    @Override
    public void onStartup() {
        sample();
    }

    private void sample() {
        long randomSamplingInterval = generateNewOffset(getRandom());
        getLog().infoSimTime(this, "Set random sampling interval to {}", TIME.format(randomSamplingInterval));

        getOs().getEventManager().addEvent(getOs().getSimulationTime() + randomSamplingInterval, this);
    }

    @Override
    public void processEvent(final Event event) throws Exception {
        sample();
    }

    @Override
    public void onShutdown() {

    }
}
