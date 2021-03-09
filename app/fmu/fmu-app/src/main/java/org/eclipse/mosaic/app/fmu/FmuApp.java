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

import no.ntnu.ihb.fmi4j.modeldescription.variables.ModelVariables;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.interactions.vehicle.VehicleDistanceSensorActivation;
import org.eclipse.mosaic.interactions.vehicle.VehicleStop;
import org.eclipse.mosaic.lib.enums.SensorType;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import java.util.Hashtable;


public class FmuApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {
    long currentTime;
    long lastStepTime = 0;

    private final String configName;

    FmuWrapper fmu;
    Hashtable<String, Object> outVars;

    public FmuApp(String configName){
        this.configName = configName;
    }

    @Override
    public void onStartup() {
        //create fmu instance
        fmu = new FmuWrapper(getOs().getConfigurationPath() + "\\" + this.configName);
        fmu.updateVehicle(
                getOs().getVehicleParameters(),
                getOs().getVehicleData(),
                getOs().getInitialVehicleType()
        );
        outVars = fmu.readVariables();
        getOs().activateVehicleDistanceSensors(100, VehicleDistanceSensorActivation.DistanceSensors.FRONT);
    }

    @Override
    public void onVehicleUpdated(VehicleData previousVehicleData, VehicleData updatedVehicleData) {

        // setup
        currentTime = getOs().getSimulationTimeMs();
        double stepSize = currentTime - lastStepTime;

        // write to fmu
        fmu.updateVehicle(
                getOs().getVehicleParameters(),
                getOs().getVehicleData(),
                getOs().getInitialVehicleType()
        );

        // simulate
        fmu.doStep(stepSize);

        // read from fmu
        Hashtable<String, Object> newOutVars = fmu.readVariables();
        if(newOutVars != outVars){
            actOnOutput(newOutVars);
        }

        // teardown
        lastStepTime = currentTime;
    }

    private void actOnOutput(Hashtable<String, Object> fmuOutputVars){
        for(String varName: fmu.vars.keySet()){
            String fmuVarName = (String) fmu.vars.get(varName).get("name");
            String dir = (String) fmu.vars.get(varName).get("direction");

            if(fmuVarName.equals("") || !dir.equals("out")){
                continue;
            }

            Object currentValue = fmuOutputVars.get(varName);
            switch (varName){
                case "speedGoal":
                    getOs().changeSpeedWithInterval((double) currentValue / 3.6f, 0);
                    break;
                case "laneChange":
                    int newLane = getOs().getRoadPosition().getLaneIndex() - (int) currentValue;
                    getOs().changeLane(newLane, 1000);
                    break;
                case "stop":
                    if((boolean) currentValue){
                        getOs().stopNow(VehicleStop.VehicleStopMode.STOP, 1000);
                    }
                    break;
                case "resume":
                    if((boolean) currentValue){
                        getOs().resume();
                    }
                    break;
                case "paramMaxSpeed":
                    getOs().requestVehicleParametersUpdate().changeMaxSpeed((double) currentValue).apply();
                    break;
                case "paramMaxAcceleration":
                    getOs().requestVehicleParametersUpdate().changeMaxAcceleration((double) currentValue).apply();
                    break;
                case "paramMaxDeceleration":
                    getOs().requestVehicleParametersUpdate().changeMaxDeceleration((double) currentValue).apply();
                    break;
                case "paramEmergencyDeceleration":
                    getOs().requestVehicleParametersUpdate().changeEmergencyDeceleration((double) currentValue).apply();
                    break;
                case "paramMinimumGap":
                    getOs().requestVehicleParametersUpdate().changeMinimumGap((double) currentValue).apply();
                    break;
            }
        }
        outVars = fmuOutputVars;
    }


    @Override
    public void onShutdown() {
        fmu.terminate();
    }

    @Override
    public void processEvent(Event event) {
        // ...
    }
}