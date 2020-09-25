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

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

/**
 * This abstract class MUST be the parent class of all interactions that shall be
 * exchanged between federates.
 */
public abstract class Interaction implements Comparable<Interaction>, Cloneable, Serializable {

    private static final long serialVersionUID = 1L;
    private static AtomicInteger idCounter = new AtomicInteger();

    /**
     * Simulation time at which the interaction happens in ns.
     */
    protected final long time;

    /**
     * The unique interaction id.
     */
    private int id;

    /**
     * The ID of the sending federate.
     */
    private String senderId;

    /**
     * The type identifier.
     */
    private String typeId;

    /**
     * Returns a new unique id to identify an interaction.
     *
     * @return a new unique id.
     */
    private static int createUniqueId() {
        return idCounter.incrementAndGet();
    }

    /**
     * Constructor using fields.
     *
     * @param time Simulation time at which the interaction happens.
     */
    protected Interaction(long time) {
        this(time, createUniqueId());
    }

    /**
     * Creates a new interaction with the specified time and predetermined id.
     * Except for special cases the constructor {@link #Interaction(long)} should be
     * used instead.
     *
     * @param time the simulation time (in ns) at which the interaction happens.
     * @param id   the predetermined id.
     */
    protected Interaction(long time, int id) {
        this.time = time;
        this.id = id;
        this.typeId = createTypeIdentifier(getClass());
    }

    /**
     * Returns the simulation time at which the interaction happens in ns.
     *
     * @return simulation time in nano-seconds
     */
    public final long getTime() {
        return this.time;
    }

    /**
     * Returns the type identifier of the interaction.
     *
     * @return the type identifier.
     */
    public String getTypeId() {
        return typeId;
    }

    /**
     * Returns the unique ID of this interaction.
     *
     * @return the unique ID of this interaction.
     */
    public final int getId() {
        return id;
    }

    /**
     * Returns the ID of the sending federate.
     *
     * @return the ID of the sending federate or {@code null} if none was assigned yet.
     */
    public final String getSenderId() {
        return senderId;
    }

    /**
     * Assigns the specified ID of the sending federate to this interaction. This method is
     * called by the {@link RtiAmbassador} and should not be called somewhere else.
     *
     * @param senderId the ID of the sending federate to set.
     * @throws IllegalValueException if the sender ID was already assigned.
     */
    public final void setSenderId(@Nonnull String senderId) throws IllegalValueException {
        if (this.senderId != null && !this.senderId.equals(senderId)) {
            throw new IllegalValueException(String.format(
                    "%s at %d ns: Sender ID \"%s\" cannot be set as sender ID was already assigned",
                    getTypeId(), getTime(), senderId
            ));
        }
        this.senderId = senderId;
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
        /*
         * Calculate equals from all fields of class and its superclasses.
         * This admittedly inefficient implementation uses the Reflection API
         * but in exchange doesn't require changes to all interaction classes.
         */
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        /*
         * Calculate hash code from all fields of class and its superclasses.
         * This admittedly inefficient implementation uses the Reflection API
         * but in exchange doesn't require changes to all interaction classes.
         */
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public int compareTo(Interaction other) {
        if (other.time == this.time) {
            return Integer.compare(this.id, other.id);
        }
        return Long.compare(this.time, other.time);
    }

    @Override
    public String toString() {
        return getTypeId() + " at " + this.time;
    }

    /**
     * Helper method for creating type identifiers in a uniform way.
     *
     * @param interactionClass the class extending from {@link Interaction}
     * @return the type identifier based on the {@link Interaction} class
     */
    public static String createTypeIdentifier(Class<? extends Interaction> interactionClass) {
        return ClassUtils.getShortClassName(interactionClass);
    }

}
