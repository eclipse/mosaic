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

package org.eclipse.mosaic.lib.geo;

public interface Circle<T extends Point<T>> extends Area<T>  {

    T getCenter();

    double getRadius();

    Polygon<T> toPolygon();

    /**
     * Checks whether the coordinate located in a circular area.
     *
     * @param point the coordinate to check.
     * @return true if the coordinate is in the area, otherwise false.
     */
    @Override
    default boolean contains(final T point) {
        return getCenter().distanceTo(point) <= getRadius();
    }

    @Override
    default double getArea() {
        return getRadius() * getRadius() * Math.PI;
    }

}
