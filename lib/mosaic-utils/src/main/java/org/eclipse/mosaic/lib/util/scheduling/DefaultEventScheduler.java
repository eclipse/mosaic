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

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;

/**
 * The event scheduler implementation is a sequential implementation of the
 * event scheduler.
 */
public class DefaultEventScheduler implements EventScheduler {

    private final static AtomicLong SEQUENCE = new AtomicLong();

    /**
     * The queue stores and sorts the events.
     */
    protected final PriorityQueue<Event> eventQueue = new PriorityQueue<>();

    /**
     * The last scheduled time.
     */
    protected long scheduledTime = Long.MIN_VALUE;

    @Override
    public void addEvent(@Nonnull final Event event) {
        if (event.getTime() < getScheduledTime()) {
            throw new IllegalArgumentException("Event lies in the past.");
        }
        event.seqNr = SEQUENCE.getAndIncrement();
        eventQueue.add(event);
    }

    @Override
    public boolean isEmpty() {
        return eventQueue.isEmpty();
    }

    @Override
    public long getNextEventTime() {
        if (isEmpty()) {
            throw new IllegalStateException("No event in the queue.");
        }
        return eventQueue.peek().getTime();
    }

    @Override
    public long getScheduledTime() {
        return scheduledTime;
    }

    @Override
    @Nonnull
    public int scheduleEvents(final long time) {
        int processedEvents = 0;
        scheduledTime = time;
        while (true) {
            final Event nextEvent = eventQueue.peek();
            if (nextEvent == null) {
                return processedEvents;
            }

            if (nextEvent.getTime() < time) {
                throw new RuntimeException("Scheduled event lies in the past.");
            } else if (nextEvent.getTime() == time) {
                eventQueue.remove(); // remove the head of the queue
                processedEvents += nextEvent.execute();
            } else {
                // else case: nextEvent.getTime() > time
                // do not schedule this event, push it back to the queue
                return processedEvents;
            }
        }
    }

    @Override
    @Nonnull
    public Set<Event> getAllEvents() {
        return Collections.unmodifiableSet(new TreeSet<>(eventQueue));
    }
}
