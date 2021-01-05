/*
 * Copyright (c) 2020 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.app.fmu;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import no.ntnu.ihb.fmi4j.importer.fmi2.*;

import java.io.File;
import java.io.IOException;

public class FmuApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {
    Fmu fmu;

    long currentTime;
    long lastStepTime = 0;

    @Override
    public void onStartup() {
        int startTime = 1;
        int stopTime = 2000;
        try{
            fmu = Fmu.from(new File("fmu/Linear_Pos.fmu"));
        }catch(IOException e){

        }
//        fmu.init(startTime, stopTime);
//        fmuAccess = new Access(fmu);
    }

    @Override
    public void onVehicleUpdated(VehicleData previousVehicleData, VehicleData updatedVehicleData) {
        getLog().info( "Tester");
        currentTime = getOs().getSimulationTimeMs();
        long stepSize = currentTime - lastStepTime;
//        fmuAccess.doStep(lastStepTime, stepSize);
        lastStepTime = currentTime;
//        fmuAccess.getRealOutputDerivatives();
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void processEvent(Event event) {
        // ...
    }

    @Override
    public VehicleOperatingSystem getOs() {
        return null;
    }
}