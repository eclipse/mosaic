/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.database.building;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * This is a wrapper object for the outline of a {@link Building}.
 */
@Immutable
public class Wall {

    /**
     * First {@link Corner} of this wall.
     */
    public final Corner fromCorner;

    /**
     * Second {@link Corner} of this wall.
     */
    public final Corner toCorner;

    /**
     * Length of this wall, determined by the distance between its two corners im meters.
     */
    public transient Double length = null;

    /**
     * Default constructor
     *
     * @param fromCorner first {@link Corner}
     * @param toCorner   second {@link Corner}
     */
    public Wall(@Nonnull Corner fromCorner, @Nonnull Corner toCorner) {
        this.fromCorner = Objects.requireNonNull(fromCorner);
        this.toCorner = Objects.requireNonNull(toCorner);
    }

    /**
     * @return Length of this wall, determined by the distance between its two corners im meters.
     */
    public double getLength() {
        if (this.length == null) {
            this.length = fromCorner.getCartesianPosition().distanceTo(toCorner.getCartesianPosition());
        }
        return this.length;
    }

    /**
     * @return First {@link Corner} of this wall.
     */
    @Nonnull
    public Corner getFromCorner() {
        return this.fromCorner;
    }

    /**
     * @return Second {@link Corner} of this wall.
     */
    @Nonnull
    public Corner getToCorner() {
        return this.toCorner;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;

        Wall other = (Wall) obj;
        return new EqualsBuilder()
                .append(this.fromCorner, other.fromCorner)
                .append(this.toCorner, other.toCorner)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 31)
                .append(fromCorner)
                .append(toCorner)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Wall{"
                + "length=" + length
                + ", fromCorner=" + fromCorner
                + ", toCorner=" + toCorner
                + '}';
    }
}
