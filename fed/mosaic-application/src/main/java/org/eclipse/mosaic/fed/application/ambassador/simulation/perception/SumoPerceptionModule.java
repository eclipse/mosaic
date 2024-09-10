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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.TrafficLightObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.interactions.vehicle.VehicleSightDistanceConfiguration;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Edge;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SumoPerceptionModule extends AbstractPerceptionModule {

    public SumoPerceptionModule(PerceptionModuleOwner owner, Database database, Logger log) {
        super(owner, database, log);
    }

    @Override
    public void enable(SimplePerceptionConfiguration configuration) {
        super.enable(configuration);
        this.owner.sendInteractionToRti(new VehicleSightDistanceConfiguration(
                this.owner.getSimulationTime(),
                owner.getId(),
                this.configuration.getViewingRange(),
                this.configuration.getViewingAngle()
        ));
    }

    @Override
    List<VehicleObject> getVehiclesInRange() {
        return owner.getVehicleData().getVehiclesInSight().stream()
                .map(v -> new VehicleObject(v.getId())
                        .setPosition(v.getProjectedPosition())
                        .setEdgeAndLane(v.getEdgeId(), v.getLaneIndex())
                        .setSpeed(v.getSpeed())
                        .setHeading(v.getHeading())
                        .setDimensions(v.getLength(), v.getWidth(), v.getHeight())
                ).toList();
    }

    @Override
    public List<TrafficLightObject> getTrafficLightsInRange() {
        this.log.debug("Traffic Light Perception not implemented for {}.", this.getClass().getSimpleName());
        return new ArrayList<>();
    }

    @Override
    public List<SpatialObject> getObjectsInRange() {
        this.log.debug("Traffic Light Perception not implemented for {} only vehicles will be retrieved.", this.getClass().getSimpleName());
        return new ArrayList<>(getVehiclesInRange());
    }

    @Override
    public Collection<Edge<Vector3d>> getSurroundingWalls() {
        this.log.debug("Wall perception not implemented for {}.", this.getClass().getSimpleName());
        return new ArrayList<>();
    }


}
