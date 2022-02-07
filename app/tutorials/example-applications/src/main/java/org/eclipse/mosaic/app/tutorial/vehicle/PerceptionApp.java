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


package org.eclipse.mosaic.app.tutorial.vehicle;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.CameraPerceptionModuleConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.VehicleObject;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PerceptionApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {

    private static final double VIEWING_ANGLE = 60d;
    private static final double VIEWING_RANGE = 200d;

    @Override
    public void onStartup() {
        getLog().debugSimTime(this, "Started {} on {}.", this.getClass().getSimpleName(), getOs().getId());

        enablePerceptionModule();
    }

    private void enablePerceptionModule() {
        CameraPerceptionModuleConfiguration perceptionModuleConfiguration =
                new CameraPerceptionModuleConfiguration(VIEWING_ANGLE, VIEWING_RANGE);
        getOs().getPerceptionModule().enable(perceptionModuleConfiguration);
    }

    @Override
    public void onShutdown() {

    }


    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        perceiveVehicles();
    }

    private void perceiveVehicles() {
        List<VehicleObject> perceivedVehicles = getOs().getPerceptionModule().getPerceivedVehicles();
        getLog().debugSimTime(this, "Perceived vehicles: {}",
                perceivedVehicles.stream().map(VehicleObject::getId).collect(Collectors.toList()));
    }

    @Override
    public void processEvent(Event event) throws Exception {

    }
}
