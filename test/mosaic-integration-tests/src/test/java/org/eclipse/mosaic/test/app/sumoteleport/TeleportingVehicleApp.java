/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.test.app.sumoteleport;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeleportingVehicleApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {

    private boolean startedTeleporting = false;
    private boolean stoppedTeleporting = false;

    @Override
    public void onStartup() {

    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void processEvent(Event event) throws Exception {

    }

    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        if (previousVehicleData == null) {
            return;
        }
        if (updatedVehicleData.isStopped()) {
            getLog().infoSimTime(this, "I'm stopped at", updatedVehicleData.getPosition());
        } else if (previousVehicleData.getPosition().equals(updatedVehicleData.getPosition())) {
            if (!startedTeleporting) {
                getLog().infoSimTime(this, "I started teleporting");
                startedTeleporting = true;
            } else {
                getLog().infoSimTime(this, "I'm currently teleporting");
            }
        } else {
            if (!startedTeleporting && !stoppedTeleporting) {
                getLog().infoSimTime(this, "I moved from {} to {} before teleport",
                        previousVehicleData.getPosition(), updatedVehicleData.getPosition());
            } else if (startedTeleporting && !stoppedTeleporting) {
                getLog().infoSimTime(this, "I finished teleporting at {}", updatedVehicleData.getPosition());
                stoppedTeleporting = true;
            } else {
                getLog().infoSimTime(this, "I moved from {} to {} after teleport",
                        previousVehicleData.getPosition(), updatedVehicleData.getPosition());
            }
        }
    }
}
