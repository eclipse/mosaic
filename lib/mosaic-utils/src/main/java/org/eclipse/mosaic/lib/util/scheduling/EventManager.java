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

package org.eclipse.mosaic.lib.util.scheduling;

import java.util.Arrays;
import javax.annotation.Nonnull;

/**
 * The event manager stores events and forces further steps that are required to
 * process the event.
 */
public interface EventManager {

    /**
     * Add an {@link Event} to the scheduler.
     *
     * @param event the event to schedule.
     */
    void addEvent(@Nonnull final Event event);

    /**
     * Add an {@link Event} to the scheduler.
     *
     * @param time            the time to schedule the event
     * @param eventProcessors the processors to process the event when scheduled
     */
    default void addEvent(long time, @Nonnull EventProcessor... eventProcessors) {
        addEvent(new Event(time, Arrays.asList(eventProcessors), null, Event.NICE_DEFAULT_PRIORITY));
    }

    /**
     * Creates a new {@link EventBuilder} object which must be used to
     * setup an event. Eventually, the {@link EventBuilder#schedule()} method must
     * be called to add the event to this {@link EventManager}.
     *
     * @param time           the time the new event should be scheduled
     * @param eventProcessor the processor to process the event when scheduled
     * @return an {@link EventBuilder} instance to create the {@link Event}
     */
    default EventBuilder newEvent(long time, @Nonnull EventProcessor... eventProcessor) {
        return new EventBuilder(time, this).withProcessors(eventProcessor);
    }

}
