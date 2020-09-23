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
 */

package org.eclipse.mosaic.rti.api;

public interface WatchDog {

    /**
     * Starts the watchdog thread.
     */
    void start();

    /**
     * Terminates the watchdog thread.
     */
    void stopWatching();

    /**
     * Attaches a process to be killed in case of a hang-up.
     *
     * @param p process to be attached
     */
    void attachProcess(Process p);

    /**
     * Updates the Watchdog with the current real time.
     */
    void updateCurrentTime();
}
