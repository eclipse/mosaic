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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.WallProvider;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;
import org.eclipse.mosaic.lib.spatial.Edge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WallOcclusionModifier implements PerceptionModifier {

    private final Vector3d intersectionResult = new Vector3d();

    @Override
    public synchronized List<VehicleObject> apply(PerceptionModuleOwner owner, List<VehicleObject> vehicleObjects) {
        if (vehicleObjects.size() == 0) {
            return vehicleObjects;
        }

        if (!(owner.getPerceptionModule() instanceof WallProvider)) {
            return vehicleObjects;
        }

        final Collection<Edge<Vector3d>> walls = ((WallProvider) owner.getPerceptionModule()).getSurroundingWalls();
        final Vector3d ownerPosition = owner.getVehicleData().getProjectedPosition().toVector3d();
        final Vector3d otherPosition = new Vector3d();

        final List<VehicleObject> result = new ArrayList<>();

        vehicleLoop: for (VehicleObject vehicle: vehicleObjects) {
            vehicle.getProjectedPosition().toVector3d(otherPosition);

            for (Edge<Vector3d> wall: walls) {
                boolean isHidden = VectorUtils.computeXZEdgeIntersectionPoint(ownerPosition, otherPosition, wall.a, wall.b, intersectionResult);
                if (isHidden) {
                    continue vehicleLoop;
                }
            }
            result.add(vehicle);
        }

        return result;
    }

}
