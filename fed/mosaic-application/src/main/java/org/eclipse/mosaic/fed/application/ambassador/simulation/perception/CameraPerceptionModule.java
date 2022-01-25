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

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;
import org.eclipse.mosaic.lib.misc.Tuple;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CameraPerceptionModule extends AbstractPerceptionModule<CameraPerceptionModuleConfiguration> {

    private CameraPerceptionModuleConfiguration configuration = null;

    public CameraPerceptionModule(PerceptionModuleOwner<CameraPerceptionModuleConfiguration> owner, Logger log) {
        super(owner, log);
    }

    @Override
    public void enable(CameraPerceptionModuleConfiguration configuration) {
        if (configuration == null) {
            log.warn("Provided configuration is null.");
        }
        this.configuration = configuration;
    }

    @Override
    public List<VehicleData> getPerceivedVehicles() {
        // TODO: add proper exception handling
        if (configuration == null) {
            return null;
        }
        if (owner.getVehicleData() == null) {
            return null;
        }
        SimulationKernel.SimulationKernel.getCentralPerceptionComponentComponent().updateSpatialIndices();
        List<VehicleData> vehiclesInIndexRange = SimulationKernel.SimulationKernel.getCentralPerceptionComponentComponent()
                .getSpatialIndex().getVehiclesInIndexRange(
                        owner.getVehicleData().getProjectedPosition(), owner.getVehicleData().getHeading(),
                        configuration.viewingRange, configuration.viewingAngle
                );
        return getVehiclesInRange(vehiclesInIndexRange);
    }

    /**
     * Checks whether the pre-selection of vehicles actually fall in the viewing range of the
     * ego vehicle.
     * Note: We use ego-vehicle position as origin.
     *
     * @param vehiclesInIndexRange a pre-selection of vehicles, that could be perceived
     * @return the properly evaluated list of perceived vehicles
     */
    private List<VehicleData> getVehiclesInRange(List<VehicleData> vehiclesInIndexRange) {
        Tuple<Vector3d, Vector3d> circularSectorEdges = getCircularSectorEdges();

        Vector3d ownerVector = cartesianToVector3d(owner.getVehicleData().getProjectedPosition());
        List<VehicleData> vehiclesInRange = new ArrayList<>();

        Vector3d origin = new Vector3d();
        for (VehicleData otherVehicle : vehiclesInIndexRange) {
            CartesianPoint otherPosition = otherVehicle.getProjectedPosition();
            if (owner.getVehicleData().getName().equals(otherVehicle.getName())) {  // checks if it's the same vehicle TODO: verify if this works with names and not ids
                continue;
            }
            Vector3d otherVector = cartesianToVector3d(otherPosition).subtract(ownerVector);
            if (VectorUtils.isLeftOfLine(otherVector, origin, circularSectorEdges.getA()) // vehicle is left of right edge
                    && !VectorUtils.isLeftOfLine(otherVector, origin, circularSectorEdges.getB()) // vehicle is right of left edge
                    && origin.distanceTo(otherVector) <= configuration.viewingRange // other vehicle is in range
            ) {
                vehiclesInRange.add(otherVehicle);
            }
        }
        return vehiclesInRange;
    }

    /**
     * Calculates the two unit vectors circumscribing the circular sector of the viewing field.
     *
     * @return a tuple of {@link Vector3d} representing the coordinates of the two edges of the circular section
     */
    private Tuple<Vector3d, Vector3d> getCircularSectorEdges() {
        double heading = owner.getVehicleData().getHeading();
        // getting the direction vector of the heading from origin
        Vector3d directionVector = VectorUtils.getDirectionVectorFromHeading(heading, new Vector3d());
        // rotate the direction vector the right using new vector
        Vector3d leftVector = (new Vector3d(directionVector)).rotateDeg(-configuration.viewingAngle / 2, VectorUtils.UP);
        // rotate the direction vector to the left re-using direction vector
        Vector3d rightVector = directionVector.rotateDeg(configuration.viewingAngle / 2, VectorUtils.UP);
        return new Tuple<>(leftVector, rightVector);
    }

    private Vector3d cartesianToVector3d(CartesianPoint cartesianPoint) {
        return new Vector3d(cartesianPoint.getX(), cartesianPoint.getZ(), -cartesianPoint.getY());
    }
}
