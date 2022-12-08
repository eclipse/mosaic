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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects;

import org.eclipse.mosaic.lib.geo.CartesianPoint;

import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.annotation.Nullable;

public class VehicleObject extends SpatialObject<VehicleObject> {

    /**
     * The current speed of the vehicle. [m/s]
     */
    private double speed;
    /**
     * The current heading of the vehicle. [degrees clockwise from north]
     */
    private double heading;
    /**
     * The edge the vehicle is currently on.
     */
    private String edgeId;
    /**
     * The lane index the vehicle is currently on.
     */
    private int laneIndex;
    /**
     * The length of the vehicle. [m]
     */
    private double length;
    /**
     * The width of the vehicle. [m]
     */
    private double width;
    /**
     * The height of the vehicle. [m]
     */
    private double height;

    /**
     * Before the first vehicle update is received vehicles have no valid positions.
     */
    private boolean isInitialized = false;

    public VehicleObject(String id) {
        super(id);
    }

    @Override
    public VehicleObject setPosition(CartesianPoint position) {
        cartesianPosition.set(position);
        position.toVector3d(this);
        return this;
    }

    public VehicleObject setEdgeAndLane(String edgeId, int laneIndex) {
        this.edgeId = edgeId;
        this.laneIndex = laneIndex;
        return this;
    }

    @Nullable
    public String getEdgeId() {
        return edgeId;
    }

    public int getLaneIndex() {
        return laneIndex;
    }

    public VehicleObject setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public double getSpeed() {
        return speed;
    }

    public VehicleObject setHeading(double heading) {
        this.heading = heading;
        return this;
    }

    public double getHeading() {
        return heading;
    }

    public VehicleObject setDimensions(double length, double width, double height) {
        this.length = length;
        this.width = width;
        this.height = height;
        return this;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public VehicleObject setInitialized() {
        this.isInitialized = true;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VehicleObject that = (VehicleObject) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(speed, that.speed)
                .append(heading, that.heading)
                .append(edgeId, that.edgeId)
                .append(laneIndex, that.laneIndex)
                .append(length, that.length)
                .append(width, that.width)
                .append(height, that.height)
                .isEquals();
    }

    /**
     * Returns a hard copy of the {@link VehicleObject}, this should be used
     * when the data of a perceived vehicle is to be stored in memory.
     *
     * @return a copy of the {@link VehicleObject}
     */
    public VehicleObject copy() {
        VehicleObject copy = new VehicleObject(getId());
        copy.setPosition(getProjectedPosition());
        copy.setHeading(getHeading());
        copy.setSpeed(getSpeed());
        copy.setEdgeAndLane(getEdgeId(), getLaneIndex());
        copy.setDimensions(getLength(), getWidth(), getHeight());
        return copy;
    }
}