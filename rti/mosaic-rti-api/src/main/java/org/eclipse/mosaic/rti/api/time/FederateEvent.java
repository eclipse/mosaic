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

package org.eclipse.mosaic.rti.api.time;

import org.eclipse.mosaic.rti.api.parameters.FederatePriority;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;

/**
 * An instance of event is created after a federate has requested to advance its
 * local time. This Class is used only by the time management service.
 */
public class FederateEvent implements Comparable<FederateEvent> {

    private static final AtomicLong ID_COUNTER = new AtomicLong();

    public static long createUniqueId() {
        return ID_COUNTER.incrementAndGet();
    }

    /**
     * identifier of the requesting federate.
     */
    private final String federateId;

    /**
     * The unique ID of the event, assigned during construction of any {@link FederateEvent}.
     */
    private final long id;


    /**
     * Simulation time that has been requested.
     */
    protected long requestedTime;

    /**
     * Time interval after this event time in which the requesting federate
     * will not create any further events.
     */
    protected final long lookahead;

    /**
     * Priority to schedule two events if they have the same time.
     */
    protected final byte priority;

    /**
     * Constructor to create an event using all fields.
     *
     * @param federateId    identifier of the requesting federate
     * @param requestedTime simulation time that has been requested
     * @param lookahead     time interval after this event time in which the requesting
     *                      federate will not create any further events
     * @param priority      priority to schedule two events if they have the same time
     */
    public FederateEvent(String federateId, long requestedTime, long lookahead, byte priority) {
        id = createUniqueId();
        this.requestedTime = requestedTime;
        this.federateId = federateId;
        this.lookahead = lookahead;
        this.priority = priority;
    }

    /**
     * Returns the unique ID of this event.
     *
     * @return the unique ID of this event.
     */
    public long getId() {
        return id;
    }

    /**
     * Getter for federate id.
     *
     * @return federate id
     */
    public String getFederateId() {
        return this.federateId;
    }

    /**
     * Getter for time. Events with a lower time are handled first.
     *
     * @return time in [ns]
     */
    public long getRequestedTime() {
        return this.requestedTime;
    }

    /**
     * Getter for lookahead.
     *
     * @return lookahead in [ns]
     */
    public long getLookahead() {
        return this.lookahead;
    }

    /**
     * Getter for priority.
     *
     * @return priority
     */
    public byte getPriority() {
        return this.priority;
    }

    @Override
    public int compareTo(@Nonnull FederateEvent event) {
        if (event.requestedTime == this.requestedTime) {
            int priorityCompare = FederatePriority.compareTo(event.priority, this.priority);
            if (priorityCompare == 0) {
                return event.lookahead < this.lookahead ? 1 : -1;
            }
            return priorityCompare;
        }
        return event.requestedTime < this.requestedTime ? 1 : -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        FederateEvent rhs = (FederateEvent) obj;
        return new EqualsBuilder()
                .append(this.federateId, rhs.getFederateId())
                .append(this.requestedTime, rhs.getRequestedTime())
                .append(this.lookahead, rhs.lookahead)
                .append(this.priority, rhs.priority)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(41, 3)
                .append(requestedTime)
                .append(federateId)
                .append(lookahead)
                .append(priority)
                .toHashCode();
    }

    @Override
    public String toString() {
        return this.federateId + " at " + this.requestedTime;
    }
}
