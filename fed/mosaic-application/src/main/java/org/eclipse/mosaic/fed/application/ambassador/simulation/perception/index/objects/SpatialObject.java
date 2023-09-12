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

public abstract class SpatialObject<T extends SpatialObject<T>> extends Vector3d {

    private final String id;

    protected final MutableCartesianPoint cartesianPosition = new MutableCartesianPoint();

    public SpatialObject(String id) {
        this.id = id;
    }

    public abstract T setPosition(CartesianPoint position);

    public abstract SpatialObjectBoundingBox getBoundingBox();

    /**
     * Sets the position of the {@link SpatialObject}.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public void setPosition(double x, double y, double z) {
        this.set(x, y, z);
        cartesianPosition.set(this.toCartesian());
    }

    /**
     * Returns the unique identifier of this spatial object.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the projected position on the X,Y-plane of this spatial object.
     */
    public CartesianPoint getProjectedPosition() {
        return cartesianPosition;
    }

    /**
     * Returns the position as a {@link Vector3d}.
     */
    public Vector3d getPosition() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SpatialObject<T> that = (SpatialObject<T>) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
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
     * Returns a hard copy of the {@link SpatialObject}, this should be used
     * when the data of a perceived object is to be altered or stored in memory.
     *
     * @return a copy of the {@link SpatialObject}
     */
    public abstract T copy();
}
