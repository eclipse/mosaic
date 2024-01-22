/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.util;

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;

/**
 * Utility class collecting methods to work with vehicle positions, such
 * as converting the reference point from center to front bumper and vice versa.
 */
public class VehicleReferenceUtils {

    /**
     * Moves the position reference of a vehicle from its center to the center of the front bumper.
     */
    public static Vector3d fromCenterToFrontBumper(Vector3d pos, double heading, double length) {
        return VectorUtils.getDirectionVectorFromHeading(heading, new Vector3d()).multiply(length / 2).add(pos);
    }

    /**
     * Moves the position reference of a vehicle from its center to the center of the front bumper.
     */
    public static CartesianPoint fromCenterToFrontBumper(CartesianPoint pos, double heading, double length) {
        return fromCenterToFrontBumper(pos.toVector3d(), heading, length).toCartesian();
    }

    /**
     * Moves the position reference of a vehicle from its center to the center of the front bumper.
     */
    public static GeoPoint fromCenterToFrontBumper(GeoPoint pos, double heading, double length) {
        return fromCenterToFrontBumper(pos.toVector3d(), heading, length).toGeo();
    }
    /**
     * Moves the position reference of a vehicle from the center of the front bumper to its bounding box center.
     */
    public static Vector3d fromFrontBumperToCenter(Vector3d pos, double heading, double length) {
        return VectorUtils.getDirectionVectorFromHeading(heading, new Vector3d()).multiply(-length / 2).add(pos);
    }

    /**
     * Moves the position reference of a vehicle from the center of the front bumper to its bounding box center.
     */
    public static CartesianPoint fromFrontBumperToCenter(CartesianPoint pos, double heading, double length) {
        return fromFrontBumperToCenter(pos.toVector3d(), heading, length).toCartesian();
    }

    /**
     * Moves the position reference of a vehicle from the center of the front bumper to its bounding box center.
     */
    public static GeoPoint fromFrontBumperToCenter(GeoPoint pos, double heading, double length) {
        return fromFrontBumperToCenter(pos.toVector3d(), heading, length).toGeo();
    }

}
