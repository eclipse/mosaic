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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.math.Vector3d;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class VehicleObject extends Vector3d implements SpatialObject {

    private final String id;
    private final MutableCartesianPoint cartesianPosition = new MutableCartesianPoint();

    private double speed;
    private double heading;

    public VehicleObject(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public VehicleObject setPosition(CartesianPoint position) {
        this.cartesianPosition.set(position);
        position.toVector3d(this);
        return this;
    }

    @Override
    public CartesianPoint getProjectedPosition() {
        return cartesianPosition;
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
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        VehicleObject that = (VehicleObject) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(speed, that.speed)
                .append(heading, that.heading)
                .append(id, that.id)
                .append(cartesianPosition, that.cartesianPosition)
                .isEquals();
    }

    @Override
    public int hashCode() {
        // use id as hashcode to store only one VehicleObject per vehicle id in perception index (e.q. quadtree)
        return this.id.hashCode();
    }

}
