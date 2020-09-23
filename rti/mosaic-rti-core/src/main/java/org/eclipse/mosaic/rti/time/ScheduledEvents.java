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

package org.eclipse.mosaic.rti.time;

import org.eclipse.mosaic.rti.api.time.FederateEvent;

import java.util.concurrent.PriorityBlockingQueue;

class ScheduledEvents {

    final Object accessMutex = new Object();
    final Object isEmptyMutex = new Object();

    /**
     * Priority queue for events.
     */
    private final PriorityBlockingQueue<FederateEvent> events = new PriorityBlockingQueue<>();

    /**
     * Priority queue for lookahead values.
     */
    private final PriorityBlockingQueue<Long> lookahead = new PriorityBlockingQueue<>();

    /**
     * Check, whether event queue is empty.
     *
     * @return true, if the event queue contains no elements.
     */
    boolean isEmpty() {
        return this.events.isEmpty();
    }

    /**
     * Clear the PriorityBlockingQueue's events and lookahead.
     */
    void clear() {
        this.events.clear();
        this.lookahead.clear();
    }

    /**
     * Appends the specified element to the PriorityBlockingQueue's events and lookahead.
     *
     * @param event element to be appended.
     */
    void addEvent(FederateEvent event) {
        this.events.add(event);
        this.lookahead.add(event.getRequestedTime() + event.getLookahead());
    }

    /**
     * Returns the next event in the queue (the head) and removes it from the queue.
     *
     * @return the event that will be removed
     */
    FederateEvent getNextScheduledEvent() {
        return this.events.remove();
    }

    /**
     * Removes the event depending on input event.
     *
     * @param event event to be stored
     */
    void setEventProcessed(FederateEvent event) {
        this.lookahead.remove(event.getRequestedTime() + event.getLookahead());
    }

    /**
     * Returns the Long MAX_VALUE if the queue is empty, else the peek of the PriorityBlockingQueue lookahead.
     *
     * @return the maximum valid time
     */
    long getMaximumValidTime() {
        if (this.lookahead.isEmpty()) {
            return Long.MAX_VALUE;
        } else {
            return this.lookahead.peek();
        }
    }
}
