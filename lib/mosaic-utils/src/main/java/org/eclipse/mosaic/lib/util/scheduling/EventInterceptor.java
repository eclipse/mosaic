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

package org.eclipse.mosaic.lib.util.scheduling;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * The event interceptor creates intercepted events based on events with other
 * processors.
 */
public class EventInterceptor implements EventManager {

    /**
     * The event manager, which should process the intercepted event.
     */
    private final EventManager eventManager;

    /**
     * This list holds all processors, which should execute intercepted events.
     * The list is an unmodifiable view of all processors.
     */
    @Nonnull
    private final List<EventProcessor> processors;

    /**
     * Constructor for convenience. See {@link #EventInterceptor(EventManager, List) }.
     * Only one processor is given.
     * <p>
     * Create a new EventInterceptor based on a given event manager and the
     * processor for the intercepted event.</p>
     *
     * @param eventManager the manager, which should schedule the intercepted
     *                     event.
     * @param processor    the processor, which should process the intercepted
     *                     event.
     */
    public EventInterceptor(@Nonnull final EventManager eventManager, @Nonnull final EventProcessor processor) {
        this(eventManager, Collections.singletonList(processor));
    }

    /**
     * Create a new EventInterceptor based on a given event manager and the
     * processors for the intercepted event.
     *
     * @param eventManager the manager, which should schedule the intercepted
     *                     event.
     * @param processors   the processors, which should process the intercepted
     *                     event.
     */
    public EventInterceptor(@Nonnull final EventManager eventManager, @Nonnull final List<EventProcessor> processors) {
        this.eventManager = Objects.requireNonNull(eventManager);

        Validate.isTrue(processors.size() > 0, "The processor list must contain at minimum one processor.");
        for (EventProcessor eventProcessor : processors) {
            Objects.requireNonNull(eventProcessor, "All event processors must not be null.");
        }
        this.processors = Collections.unmodifiableList(new ArrayList<>(processors));
    }

    @Override
    public void addEvent(@Nonnull final Event event) {
        eventManager.addEvent(new InterceptedEvent(event, processors));
    }

}
