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

import org.eclipse.mosaic.lib.gson.CartesianPointAdapter;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(CartesianPointAdapter.class)
public interface CartesianPoint extends Point<CartesianPoint> {

    CartesianPoint ORIGO = xyz(0, 0, 0);

    double getX();

    double getY();

    double getZ();

    GeoPoint toGeo();

    static MutableCartesianPoint xy(double x, double y) {
        return xyz(x, y, 0);
    }

    static MutableCartesianPoint xyz(double x, double y, double z) {
        return new MutableCartesianPoint(x, y, z);
    }
}
