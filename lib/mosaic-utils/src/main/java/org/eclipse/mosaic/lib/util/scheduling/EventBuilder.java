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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class EventBuilder {

    private final long time;
    private final EventManager eventManager;
    private final List<EventProcessor> processors = new ArrayList<>();

    private Object resource;
    private long nice;

    EventBuilder(long time, EventManager eventManager) {
        // keep this package protected. only the EventManager should create an instance of this builder.
        this.time = time;
        this.eventManager = eventManager;
    }

    /**
     * Registers an array of {@link EventProcessor}s to the event currently being built.
     *
     * @param processors the {@link EventProcessor}s to add
     * @return this {@link EventBuilder} to continue building
     */
    public EventBuilder withProcessors(EventProcessor... processors) {
        Collections.addAll(this.processors, processors);
        return this;
    }

    /**
     * Registers a list of {@link EventProcessor}s to the event currently being built.
     *
     * @param processors the list of {@link EventProcessor}s
     * @return this {@link EventBuilder} to continue building
     */
    public EventBuilder withProcessors(Collection<EventProcessor> processors) {
        this.processors.addAll(processors);
        return this;
    }

    /**
     * Registers the event resource to the event currently being built.
     *
     * @param resource the event resource
     * @return this {@link EventBuilder} to continue building
     */
    public EventBuilder withResource(Object resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Registers the event priority (high value = low priority) to the event currently being built.
     *
     * @param nice the event priority (high value = low priority)
     * @return this {@link EventBuilder} to continue building
     */
    public EventBuilder withNice(long nice) {
        this.nice = nice;
        return this;
    }

    /**
     * Finishes building the event and adds it to the {@link EventManager}
     * associated with this {@link EventBuilder}.
     *
     * @return the freshly created {@link Event}
     */
    public Event schedule() {
        Event event = new Event(time, processors, resource, nice);
        eventManager.addEvent(event);
        return event;
    }
}
