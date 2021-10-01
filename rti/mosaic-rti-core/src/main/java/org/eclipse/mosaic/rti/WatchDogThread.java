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

package org.eclipse.mosaic.rti;

import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.WatchDog;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.LinkedList;

/**
 * This thread is used to monitor a federate.
 */
public class WatchDogThread extends Thread implements WatchDog {

    private final ComponentProvider federation;

    /**
     * Period of idle time in seconds after which the thread reacts.
     */
    private final int maxIdleTime;

    /**
     * Current time in milliseconds, used to check if program is still alive.
     */
    private volatile long timeOfLastUpdate = 0;

    /**
     * If set to true this thread will terminate.
     */
    private volatile boolean watching = true;

    private final LinkedList<Process> processList;

    public WatchDogThread(ComponentProvider federation, int maxIdleTime) {
        this.federation = federation;
        this.maxIdleTime = maxIdleTime;
        processList = new LinkedList<>();
    }

    /**
     * Updates the Watchdog with the current real time.
     */
    public void updateCurrentTime() {
        this.timeOfLastUpdate = System.currentTimeMillis();
    }

    @SuppressWarnings(value = "DM_EXIT", justification = "That's the purpose of the Watchdog")
    @Override
    public void run() {
        while (watching) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                // be quiet
            }
            long timeSinceLastUpdate = System.currentTimeMillis() - timeOfLastUpdate;
            if (timeSinceLastUpdate > this.maxIdleTime * 1000L && watching) {
                try {
                    System.out.println();
                    System.err.println("--------------------------------------------------------------------------------");
                    System.err.println("ERROR: One or more federates did not respond for " + maxIdleTime + " seconds.");
                    System.err.println("       This could be caused by an error in a federate.");
                    System.err.println("       You can increase the timeout using the -w parameter.");
                    System.err.println("       Using \"-w 0\" disables the watchdog.");
                    System.err.println("--------------------------------------------------------------------------------");
                    System.err.println("       MOSAIC will now shut down.");
                    System.err.println("--------------------------------------------------------------------------------");

                    // wait a second to complete print lines
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // be quiet
                    }

                    for (Process p : processList) {
                        p.destroy();
                    }

                    // inform every federate
                    federation.getTimeManagement().finishSimulationRun(-1);
                    federation.getFederationManagement().stopFederation();

                } catch (Throwable e) {
                    // in this state something went already totally wrong, hence
                    // further errors can be ignored
                }
                System.exit(333);
            }
        }
    }

    /**
     * If called the thread will terminate.
     */
    @Override
    public void stopWatching() {
        this.watching = false;
    }

    /**
     * Attaches a new process to the watchdog to be killed in case of a hang-up.
     *
     * @param p process to be attached
     */
    @Override
    public void attachProcess(Process p) {
        processList.add(p);
    }
}
