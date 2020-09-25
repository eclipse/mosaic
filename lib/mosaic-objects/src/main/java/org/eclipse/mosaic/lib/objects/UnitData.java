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

package org.eclipse.mosaic.lib.objects;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * General unit data container for e.g. position, time etc.
 */
@Immutable
public abstract class UnitData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Time in [ns] until this information is valid.
     */
    private final long time;

    /**
     * Unique string identifying the unit.
     */
    private final String name;

    /**
     * Current geographic position of the unit.
     */
    private final GeoPoint position;

    /**
     * Creates a new {@link UnitData}.
     *
     * @param time    time of the last update
     * @param name    name of the unit
     * @param position position of the unit
     */
    public UnitData(long time, String name, GeoPoint position) {
        this.time = time;
        this.name = name;
        this.position = position;
    }

    /**
     * Getter for the time of the last update.
     *
     * @return Time of the last update
     * @see UnitData#time
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns the unique string identifying the unit.
     * @see UnitData#name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current geographical position of the unit.
     */
    public GeoPoint getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 59)
                .append(time)
                .append(name)
                .append(position)
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

        UnitData other = (UnitData) obj;
        return new EqualsBuilder()
                .append(this.time, other.time)
                .append(this.name, other.name)
                .append(this.position, other.position)
                .isEquals();
    }

    @Override
    public String toString() {
        return "UnitData{" + "time=" + time + ", name=" + name + ", position=" + position + '}';
    }

}
