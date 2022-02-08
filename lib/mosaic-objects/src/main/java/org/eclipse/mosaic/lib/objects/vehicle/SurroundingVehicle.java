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

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.util.objects.Position;

public class SurroundingVehicle {

    private final String id;
    private final Position position;
    private final double speed;
    private final double angle;

    public SurroundingVehicle(String id, Position position, double speed, double angle) {
        this.id = id;
        this.position = position;
        this.speed = speed;
        this.angle = angle;
    }

    public String getId() {
        return id;
    }

    public CartesianPoint getProjectedPosition() {
        return position.getProjectedPosition();
    }

    public GeoPoint getGeographicPosition() {
        return position.getGeographicPosition();
    }

    public double getSpeed() {
        return speed;
    }

    public double getAngle() {
        return angle;
    }
}
