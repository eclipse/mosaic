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

package org.eclipse.mosaic.lib.objects.vehicle;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.util.objects.Position;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * A container class holding basic information about a surrounding or detected vehicle object.
 */
@Immutable
public class SurroundingVehicle implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The id of the surrounding vehicle.
     */
    private final String id;
    /**
     * The (cartesian and geographical) position of the surrounding vehicle.
     */
    private final Position position;
    /**
     * The current speed of the surrounding vehicle.
     * Unit: [m/s]
     */
    private final double speed;
    /**
     * The current heading of the surrounding vehicle.
     * Unit: [degrees from north clockwise]
     */
    private final double heading;
    /**
     * The current edge the vehicle is on.
     */
    private final String edgeId;
    /**
     * The current lane index the vehicle is on. (From outermost lane ascending)
     */
    private final int laneIndex;
    /**
     * The length of the vehicle.
     */
    private final double length;
    /**
     * The width of the vehicle.
     */
    private final double width;
    /**
     * The height of the vehicle.
     */
    private final double height;

    public SurroundingVehicle(String id, Position position, double speed, double heading,
                              String edgeId, int laneIndex, double length, double width, double height) {
        this.id = id;
        this.position = position;
        this.speed = speed;
        this.heading = heading;
        this.edgeId = edgeId;
        this.laneIndex = laneIndex;
        this.length = length;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the id of the surrounding vehicle.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the cartesian position of the surrounding vehicle.
     */
    public CartesianPoint getProjectedPosition() {
        return position.getProjectedPosition();
    }

    /**
     * Returns the geographical position of the surrounding vehicle.
     */
    public GeoPoint getGeographicPosition() {
        return position.getGeographicPosition();
    }

    /**
     * Returns the current speed of the surrounding vehicle.
     * Unit: [m/s]
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns the current heading of the surrounding vehicle.
     * Unit: [degrees from north clockwise]
     */
    public double getHeading() {
        return heading;
    }

    /**
     * Returns the current edge of the surrounding vehicle.
     */
    public String getEdgeId() {
        return edgeId;
    }

    /**
     * Returns the current lane index of the surrounding vehicle.
     */
    public int getLaneIndex() {
        return laneIndex;
    }

    /**
     * Returns the length of the surrounding vehicle.
     * Unit: [m]
     */
    public double getLength() {
        return length;
    }

    /**
     * Returns the width of the surrounding vehicle.
     * Unit: [m]
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height of the surrounding vehicle.
     * Unit: [m]
     */
    public double getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SurroundingVehicle that = (SurroundingVehicle) o;

        return new EqualsBuilder()
                .append(speed, that.speed)
                .append(heading, that.heading)
                .append(id, that.id)
                .append(position, that.position)
                .append(edgeId, that.edgeId)
                .append(laneIndex, that.laneIndex)
                .append(length, that.length)
                .append(width, that.width)
                .append(height, that.height)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 41)
                .append(id)
                .append(position)
                .append(speed)
                .append(heading)
                .append(edgeId)
                .append(laneIndex)
                .append(length)
                .append(width)
                .append(height)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("position", position)
                .append("speed", speed)
                .append("heading", heading)
                .append("edgeId", edgeId)
                .append("laneIndex", laneIndex)
                .append("length", length)
                .append("width", width)
                .append("height", height)
                .toString();
    }
}
