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

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * This is a simple point that marks the corner of a building.
 */
@Immutable
public class Corner {

    /**
     * The geographical position this point is located at.
     */
    public final GeoPoint position;

    /**
     * The cartesian position
     */
    public transient CartesianPoint cartesianPosition;

    public Corner(@Nonnull GeoPoint position) {
        this.position = Objects.requireNonNull(position);
    }

    /**
     * @return Geographical position this point is located at.
     */
    @Nonnull
    public GeoPoint getPosition() {
        return this.position;
    }

    /**
     * @return Cartesian representation of this points geographical position.
     */
    @Nonnull
    public CartesianPoint getCartesianPosition() {
        if (this.cartesianPosition == null) {
            this.cartesianPosition = this.position.toCartesian();
        }
        return this.cartesianPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;

        Corner other = (Corner) obj;
        return new EqualsBuilder()
                .append(this.position, other.position)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 31)
                .append(position)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Corner{" +
                "position=" + position +
                ", cartesianPosition=" + cartesianPosition +
                '}';
    }
}
