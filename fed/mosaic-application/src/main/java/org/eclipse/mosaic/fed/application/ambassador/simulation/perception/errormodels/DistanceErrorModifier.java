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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DistanceErrorModifier implements PerceptionModifier {

    private final RandomNumberGenerator rng;

    private final double threshold;

    public DistanceErrorModifier(RandomNumberGenerator rng, double threshold) {
        this.rng = rng;
        this.threshold = threshold;
    }

    @Override
    public List<VehicleObject> apply(PerceptionModuleOwner owner, List<VehicleObject> vehicleObjects) {
        if (vehicleObjects.size() == 0) {
            return vehicleObjects;
        }
        Vector3d ownerPosition = owner.getVehicleData().getProjectedPosition().toVector3d();
        List<VehicleObject> sortedByDistance = new ArrayList<>(vehicleObjects);
        sortedByDistance.sort(Comparator.comparingDouble(vehicleObject -> vehicleObject.distanceTo(ownerPosition)));
        // fit linear function to (closest distance, min angle) and (furthest distance, max angle)
        double closestPerceivedDistance = ownerPosition.distanceTo(sortedByDistance.get(0));
        double furthestPerceivedDistance = ownerPosition.distanceTo(sortedByDistance.get(sortedByDistance.size() - 1));
        Iterator<VehicleObject> vehicleObjectIterator = vehicleObjects.listIterator();
        while (vehicleObjectIterator.hasNext()) {
            VehicleObject currentVehicleObject = vehicleObjectIterator.next();
            if (getDistanceRating(ownerPosition.distanceTo(currentVehicleObject)) >= threshold) {

            }
        }
        return vehicleObjects;
    }

    private double getDistanceRating(double distanceTo) {
        return 1d;
    }
}
