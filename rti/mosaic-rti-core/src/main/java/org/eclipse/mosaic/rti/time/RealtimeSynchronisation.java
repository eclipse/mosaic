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

package org.eclipse.mosaic.rti.time;

import org.eclipse.mosaic.rti.TIME;

/**
 * Synchronizes the simulation time with the real time according to a given realtime factor.
 */
public class RealtimeSynchronisation {

    private static final long MINSYNC = 5 * TIME.MILLI_SECOND;

    private final double realtimeFactor;

    private long realNanoTimeLastSync;
    private long simNanoTimeLastSync;
    private long waitOffset;

    /**
     * If the passed realtimeFactor is greater than 0, the simulation
     * is slowed down to match the given real time factor. If the given
     * realtime factor is equal to 0, no synchronization is done.
     *
     * @param realtimeFactor the wanted realtime factor.
     */
    public RealtimeSynchronisation(double realtimeFactor) {
        this.realtimeFactor = realtimeFactor;
    }

    /**
     * Synchronization of the real and simulation time. If the simulation has a faster
     * real time factor than the wanted real time factor, this methods blocks for
     * a few milliseconds and, therefore, slows down the simulation to reach the
     * target real time factor.
     *
     * @param timestamp the current timestamp
     */
    public void sync(long timestamp) {

        if (realtimeFactor > 0d && realNanoTimeLastSync > 0) {
            long realTimeSinceLastSync = System.nanoTime() - realNanoTimeLastSync;
            long simTimeSinceLastSync = timestamp - simNanoTimeLastSync;

            // Conversion from simulation to wanted realtime according to the given realtime factor 
            long realTimeSinceLastSyncWanted = (long) (simTimeSinceLastSync * (1 / realtimeFactor));

            // The real nano seconds we now have to wait to fulfill the requirement above
            long nanoTimeToWait = realTimeSinceLastSyncWanted - realTimeSinceLastSync + waitOffset;

            // The actual waiting
            while (nanoTimeToWait > MINSYNC) {
                try {
                    Thread.sleep(MINSYNC / TIME.MILLI_SECOND);
                } catch (InterruptedException e) {
                    // be quiet
                }
                nanoTimeToWait -= MINSYNC;
            }

            // Store, how much time we did wait too much or too less for the next sync
            waitOffset = nanoTimeToWait;
        }

        realNanoTimeLastSync = System.nanoTime();
        simNanoTimeLastSync = timestamp;
    }
}
