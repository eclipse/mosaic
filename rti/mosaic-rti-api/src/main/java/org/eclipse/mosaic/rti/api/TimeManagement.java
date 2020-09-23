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

import javax.annotation.Nonnull;

/**
 * The time management is responsible for continuing the simulation process and
 * synchronizing participating simulators. During runtime, each simulator
 * schedules events and waits for an approval before executing the corresponding
 * simulation steps.
 */
public interface TimeManagement {

    /**
     * Runs the simulation. When this method is called, the runtime
     * infrastructure runs the simulation based on the joined federates. This
     * method returns after the whole simulation is finished!
     *
     * @throws InternalFederateException an exception inside of a joined federate occurs
     * @throws IllegalValueException     a parameter has an invalid value
     */
    void runSimulation() throws InternalFederateException, IllegalValueException;

    /**
     * Stores a schedulable event representing the requested time. When the event
     * is scheduled the associated federate ambassador is called by the time
     * management.<br />
     * This method is to be called only by an <code>RtiAmbassador</code>
     * instance!
     *
     * @param federateId unique string identifying the calling federate
     * @param time       requested time
     * @param lookahead  time after the requested time in which the calling federate
     *                   will not schedule any further events or send any interactions
     * @param priority   priority of the event<br />
     *                   only events with equal priority are scheduled in parallel
     *                   even if they have the same time
     * @throws IllegalValueException an exception if a parameter has an invalid value
     */
    void requestAdvanceTime(String federateId, long time, long lookahead, byte priority) throws IllegalValueException;

    /**
     * The method is called once after the simulation has reached the end time.
     * It calls the finishSimulation method of all joined federates.
     *
     * @param statusCode the status code number describing the success or failure of the simulation
     *
     * @throws InternalFederateException an exception inside of a joined federate occurs
     */
    void finishSimulationRun(int statusCode) throws InternalFederateException;

    /**
     * Starts the watchdog thread.
     *
     * @param simId       Id of the simulation
     * @param maxIdleTime Period of idle time in seconds after which the thread reacts
     * @return the new {@link WatchDog}
     */
    @Nonnull
    WatchDog startWatchDog(String simId, int maxIdleTime);

    /**
     * Starts the external watchdog thread.
     *
     * @param simId Id of the simulation
     * @param port  Connection port.
     */
    void startExternalWatchDog(String simId, int port);

    /**
     * Updates the watchdog thread to show that wa are still alive.
     */
    void updateWatchDog();

    /**
     * Getter for current simulation time.
     *
     * @return the current simulation time
     */
    long getSimulationTime();

    /**
     * Getter for end time.
     *
     * @return end time of the running simulation
     */
    long getEndTime();

    /**
     * Returns the time stamp of the next event.
     *
     * @return time stamp of the next event
     * @throws IllegalValueException a parameter has an invalid value
     */
    long getNextEventTimestamp() throws IllegalValueException;
}
