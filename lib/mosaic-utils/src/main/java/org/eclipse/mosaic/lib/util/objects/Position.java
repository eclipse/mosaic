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

package org.eclipse.mosaic.lib.util.objects;

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * Type, which should be used to define the position of an entity. It contains a
 * geographic position (longitude, latitude) as well as a projected position using
 * the projection defined for a federation.
 */
@Immutable
public class Position implements Serializable {

    public static final Position INVALID = new Position(false, CartesianPoint.ORIGO, GeoPoint.ORIGO);

    private static final long serialVersionUID = 1L;

    /**
     * Coordinates of the transformed position.
     */
    private final CartesianPoint projectedPosition;

    /**
     * Geographic position with latitude and longitude coordinate.
     */
    private final GeoPoint geographicPosition;

    private final boolean valid;

    private Position(boolean valid, CartesianPoint cartesianPoint, GeoPoint geoPoint) {
        this.valid = valid;
        this.projectedPosition = Validate.notNull(cartesianPoint);
        this.geographicPosition = Validate.notNull(geoPoint);
    }

    /**
     * Creates a new {@link Position} object based on geographic position.
     *
     * @param geographicPosition the {@link GeoPoint} describing the geographic position with latitude and longitude
     */
    public Position(final GeoPoint geographicPosition) {
        this(true, geographicPosition.toCartesian(), geographicPosition);
    }

    /**
     * Creates a new {@link Position} object based on a cartessian position.
     *
     * @param projectedPosition the {@link CartesianPoint} describing the cartessian position with x,y
     */
    public Position(final CartesianPoint projectedPosition) {
        this(true, projectedPosition, projectedPosition.toGeo());
    }

    /**
     * Returns the geographic coordinate of this position.
     */
    public GeoPoint getGeographicPosition() {
        return geographicPosition;
    }

    /**
     * Returns the x coordinate of the transformed position.
     */
    public double getX() {
        return projectedPosition.getX();
    }

    /**
     * Returns the y coordinate of the transformed position.
     */
    public double getY() {
        return projectedPosition.getY();
    }

    /**
     * Returns <code>true</code>, if this is a valid position.
     *
     * @return <code>true</code>, if this is a valid position
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Getter for projected position.
     *
     * @return projected position
     */
    public CartesianPoint getProjectedPosition() {
        return projectedPosition;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31)
            .append(geographicPosition)
            .append(projectedPosition)
            .append(valid)
            .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;

        Position rhs = (Position) obj;
        return new EqualsBuilder()
                        .append(this.geographicPosition, rhs.geographicPosition)
                        .append(this.projectedPosition, rhs.projectedPosition)
                        .append(this.valid, rhs.valid)
                        .isEquals();
    }

    @Override
    public String toString() {
        return "Position [valid=" + valid + ", projectedPosition=" + projectedPosition + ", geographicPosition=" + geographicPosition + "]";
    }
}
