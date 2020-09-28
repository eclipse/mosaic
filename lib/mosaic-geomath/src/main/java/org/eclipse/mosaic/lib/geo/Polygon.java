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

import org.eclipse.mosaic.lib.math.Vector3d;

import java.util.List;

public interface Polygon<T extends Point<T>> extends Area<T> {

    List<T> getVertices();

    /**
     * Calculates the area of this polygon in square meter.
     *
     * @return the area of this polygon in square meter
     */
    @Override
    default double getArea() {
        double area = 0.0;

        final Vector3d prevV = new Vector3d();
        final Vector3d currV = new Vector3d();

        // shoelace formula
        T prev = null;
        for (T curr : getVertices()) {
            if (prev != null) {
                prev.toVector3d(prevV);
                curr.toVector3d(currV);
                area += (prevV.x + currV.x) * (prevV.z - currV.z);
            }
            prev = curr;
        }
        return Math.abs(area / 2.0);
    }

}
