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

package org.eclipse.mosaic.lib.objects.environment;

import org.eclipse.mosaic.lib.enums.SensorType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public final class EnvironmentEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public final SensorType type;
    public final int strength;
    public final long from;
    public final long until;

    /**
     * Creates a new environment event.
     *
     * @param type     sensor type
     * @param strength sensor strength
     * @param from     beginning time of the event
     * @param until    ending time of the event
     */
    public EnvironmentEvent(SensorType type, int strength, long from, long until) {
        this.type = type;
        this.strength = strength;
        this.from = from;
        this.until = until;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 37)
                .append(type)
                .append(strength)
                .append(from)
                .append(until)
                .toHashCode();
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

        EnvironmentEvent rhs = (EnvironmentEvent) obj;
        return new EqualsBuilder()
                .append(this.type, rhs.type)
                .append(this.strength, rhs.strength)
                .append(this.from, rhs.from)
                .append(this.until, rhs.until)
                .isEquals();
    }

    @Override
    public String toString() {
        return "EnvironmentEvent{" + "type=" + type + ", strength=" + strength + ", from=" + from + ", until=" + until + '}';
    }
}
