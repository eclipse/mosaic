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
 * This is a simple application to demonstrate a concurrency task.
 * In your scenario may exist many vehicles (e.g. veh_0, veh_1, veh_2, ...).
 * This application demonstrates the following behavior:
 * <p>
 * veh_0, veh_1, veh_2, ... start a thread
 * </p>
 * <p>
 * veh_0, veh_1, veh_2, ... do some logic parallel to each other (e.g. difficult calculations independent from each other)
 * </p>
 * <p>
 * veh_0, veh_1, veh_2, ... join the thread
 * </p>
 *
 * <p>This will be an advantage by difficult tasks because RTI shouldn't wait for each vehicle to complete the calculation task.
 *
 */
public class MultithreadSamplingApp extends AbstractApplication<VehicleOperatingSystem> {

    private static final long SAMPLING_INTERVAL = 2 * TIME.SECOND;
    private Thread someDifficultTaskThread;

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "Set sampling interval to {}", TIME.format(SAMPLING_INTERVAL));
        sample();
    }

    private void sample() {
        // thread with a high priority to be scheduled first
        getOs().getEventManager()
                .newEvent(getOs().getSimulationTime() + SAMPLING_INTERVAL, this)
                .withResource("start")
                .withNice(0)
                .schedule();

        // thread with a low priority to be scheduled after the first event
        getOs().getEventManager()
                .newEvent(getOs().getSimulationTime() + SAMPLING_INTERVAL, this)
                .withResource("join")
                .withNice(1)
                .schedule();
    }

    @Override
    public void processEvent(final Event event) throws Exception {
        Object resource = event.getResource();
        if (resource instanceof String command) {
            switch (command) {
                case "start":
                    startThread();
                    break;
                case "join":
                    joinThread();
                    // only sample again after the thread was joined
                    sample();
                    break;
                default:
                    // unknown command
                    throw new RuntimeException("Can't process event: Unknown command extracted from event resource.");
            }
        }
    }


    /**
     * All running applications start their logic here.
     */
    private void startThread() {
        getLog().infoSimTime(this, "Started thread.");
        Runnable worker = () -> {
            // a difficult task =)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // quiet
            }
        };

        this.someDifficultTaskThread = new Thread(worker);
        this.someDifficultTaskThread.start();
    }

    /**
     * All running applications join their logic here.
     */
    private void joinThread() {
        try {
            this.someDifficultTaskThread.join();
            getLog().infoSimTime(this, "Joined thread.");
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onShutdown() {

    }

}
