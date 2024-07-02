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

package org.eclipse.mosaic.rti.api;

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import com.google.common.collect.ImmutableCollection;

import javax.annotation.Nonnull;

/**
 * This interface is offered to each <code>FederateAmbassador</code> to allow
 * communication to the RTI. Therefore, the RTI has to implement this interface.
 */
public interface RtiAmbassador extends Interactable {

    /**
     * <p>
     * This method should be called by a federate ambassador to request advancing
     * its simulation time. The RTI ambassador forwards this request to the time
     * management and an event representing the request is stored. When the
     * event is scheduled the federate ambassador is called by the RTI.
     * </p><p>
     * This method delivers exactly the same result as {@code requestAdvanceTime(time, 0, 0)}
     * </p>
     *
     * @param time the simulation time in [ns]
     * @throws IllegalValueException if an invalid time value has been passed
     */
    void requestAdvanceTime(long time) throws IllegalValueException;

    /**
     * This method should be called by a federate ambassador to request advancing
     * its simulation time. The RTI ambassador forwards this request to the time
     * management and an event representing the request is stored. When the
     * event is scheduled the federate ambassador is called by the RTI.
     *
     * @param time      the simulation time in [ns]
     * @param lookahead the time length in [ns] with which the federate guarantees to not request a time advance again
     * @param priority  the priority of the event
     * @throws IllegalValueException if an invalid time value has been passed
     */
    void requestAdvanceTime(long time, long lookahead, byte priority) throws IllegalValueException;

    /**
     * Provides the timestamp of them next scheduled event. [ns]
     * @throws IllegalValueException if
     */
    long getNextEventTimestamp() throws IllegalValueException;

    /**
     * Provides the list of the subscribed interactions.
     *
     * @return set of subscribed interactions
     */
    ImmutableCollection<String> getSubscribedInteractions();

    /**
     * Returns a new {@link RandomNumberGenerator} which is associated with the current federation.
     *
     * @return a new {@link RandomNumberGenerator}
     */
    @Nonnull
    RandomNumberGenerator createRandomNumberGenerator();

    /**
     * Provides the monitor instance to log specific events, such as the start of
     * the simulation or event triggers.
     *
     * @return the monitor instance to log specific events
     */
    @Nonnull
    Monitor getMonitor();
}
