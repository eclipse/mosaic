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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModuleOwner;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.VehicleObject;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;

import java.util.List;

/**
 * Based on {@see <a href="https://mediatum.ub.tum.de/1519952">
 * Virtual Sensorics: Simulated Environmental Perception for Automated Driving Systems</a>}
 * Uses a lidar sensor
 */
public class PositionErrorModifier implements PerceptionModifier {

    private static double SIGMA_LON_OFFSET = 1.333; // [m]
    private static double SIGMA_LAT_OFFSET = 0.390; // [m]

    private final RandomNumberGenerator rng;

    public PositionErrorModifier(RandomNumberGenerator rng) {
        this.rng = rng;
    }


    @Override
    public List<VehicleObject> apply(PerceptionModuleOwner owner, List<VehicleObject> vehicleObjects) {
        Vector3d ownerPosition = owner.getVehicleData().getProjectedPosition().toVector3d();
        Vector3d ownerDirection = new Vector3d();
        VectorUtils.getDirectionVectorFromHeading(owner.getVehicleData().getHeading(), ownerDirection);
        double angleToNorth = ownerDirection.angle(VectorUtils.NORTH);
        vehicleObjects.forEach(
                vehicleObject -> {
                    Vector3d relativePosition = getVectorRelativeTo(ownerPosition, vehicleObject);
                    Vector3d adjustedVector = new Vector3d(relativePosition);
                    adjustedVector.rotate(-angleToNorth, VectorUtils.UP); // rotate vector according to orientation
                    // add latitudinal and longitudinal errors
                    adjustedVector.set(
                            rng.nextGaussian(adjustedVector.x, SIGMA_LAT_OFFSET),
                            adjustedVector.y,
                            rng.nextGaussian(adjustedVector.z, SIGMA_LON_OFFSET)
                    );
                    adjustedVector.rotate(angleToNorth, VectorUtils.UP); // rotate back
                    vehicleObject.set(
                            vehicleObject.x - (relativePosition.x - adjustedVector.x),
                            vehicleObject.y,
                            vehicleObject.z - (relativePosition.z - adjustedVector.z)
                    );
                }
        );

        return vehicleObjects;
    }

    private Vector3d getVectorRelativeTo(Vector3d origin, Vector3d relative) {
        return new Vector3d(origin).subtract(relative);
    }
}
