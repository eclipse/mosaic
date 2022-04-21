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

import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;
import org.eclipse.mosaic.interactions.vehicle.VehicleSightDistanceConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class SumoPerceptionModule implements PerceptionModule<SimplePerceptionConfiguration> {

    private final PerceptionModuleOwner owner;

    public SumoPerceptionModule(PerceptionModuleOwner owner) {
        this.owner = owner;
    }

    @Override
    public void enable(SimplePerceptionConfiguration configuration) {
        this.owner.sendInteractionToRti(new VehicleSightDistanceConfiguration(
                this.owner.getSimulationTime(),
                owner.getId(),
                configuration.getViewingRange(),
                configuration.getViewingAngle()
        ));
    }

    @Override
    public List<VehicleObject> getPerceivedVehicles() {
        return owner.getVehicleData().getVehiclesInSight().stream()
                .map(v -> new VehicleObject(v.getId())
                        .setPosition(v.getProjectedPosition())
                        .setSpeed(v.getSpeed())
                        .setHeading(v.getHeading())
                ).collect(Collectors.toList());
    }

}
