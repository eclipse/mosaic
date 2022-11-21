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
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.math.Vector3d;

import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.annotation.Nullable;

public class VehicleObject extends Vector3d implements SpatialObject {

    private final String id;
    private final MutableCartesianPoint cartesianPosition = new MutableCartesianPoint();
    private double speed;
    private double heading;

    private String edgeId;

    private int laneIndex;

    public VehicleObject(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CartesianPoint getProjectedPosition() {
        return cartesianPosition;
    }

    @Override
    public Vector3d getPosition() {
        return this;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.set(x, y, z);
        cartesianPosition.set(this.toCartesian());
    }

    public VehicleObject setPosition(CartesianPoint position) {
        this.cartesianPosition.set(position);
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
                .append(id, that.id)
                .append(cartesianPosition, that.cartesianPosition)
                .isEquals();
    }

    @Override
    public int hashCode() {
        // use id as hashcode to store only one VehicleObject per vehicle id in perception index (e.q. quadtree)
        return this.id.hashCode();
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
        return copy;
    }
}