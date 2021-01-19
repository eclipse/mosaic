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

import no.ntnu.ihb.fmi4j.Fmi4jVariableUtils;
import no.ntnu.ihb.fmi4j.modeldescription.CoSimulationModelDescription;
import no.ntnu.ihb.fmi4j.modeldescription.variables.ModelVariables;
import no.ntnu.ihb.fmi4j.modeldescription.variables.RealVariable;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleParameters;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.interactions.vehicle.VehicleDistanceSensorActivation;
import org.eclipse.mosaic.lib.enums.LaneChangeMode;
import org.eclipse.mosaic.lib.enums.SensorType;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import no.ntnu.ihb.fmi4j.importer.fmi2.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CollationElementIterator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;


public class FmuApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {
//    private static final Path fmuPath = Paths.get("C:", "Users", "Theo", "Nextcloud", "uni", "WS2020-2021", "dcaiti_Projekt_VuaF", "mosaic", "mosaic", "mosaic_own", "app", "fmu", "test", "fmu", "Linear_Pos.fmu");
    private static final Path fmuPath = Paths.get("C:\\Users\\Theo\\Nextcloud\\uni\\WS2020-2021\\dcaiti_Projekt_VuaF\\mosaic\\fmu");
    private static final Path fmuInUse = Paths.get(fmuPath.toAbsolutePath().normalize().toString(), "TriangularDriving.fmu");

    Fmu fmu;
    CoSimulationSlave slave;
    CoSimulationModelDescription slaveDes;

    long currentTime;
    long lastStepTime = 0;

    ModelVariables currentVars;
    ModelVariables previousVars;

    static final String SPEED = "speed";
    static final String SPEED_MIN = "speedMin";
    static final String SPEED_MAX = "speedMax";
    static final String SPEED_GOAL = "speedGoal";

    RealVariable speed;

    @Override
    public void onStartup() {
        //create fmu instance
        try{
            fmu = Fmu.from(new File(fmuInUse.normalize().toString()));
        }catch(IOException e){
            getLog().error(e.toString());
        }
        slave = fmu.asCoSimulationFmu().newInstance();
        slave.simpleSetup();

        //initialize variables
        slaveDes = slave.getModelDescription();
        speed = slaveDes.getVariableByName(SPEED).asRealVariable();

        getOs().activateVehicleDistanceSensors(100, VehicleDistanceSensorActivation.DistanceSensors.FRONT);
    }

    @Override
    public void onVehicleUpdated(VehicleData previousVehicleData, VehicleData updatedVehicleData) {

        // setup
        currentTime = getOs().getSimulationTimeMs();
        long stepSize = currentTime - lastStepTime;
        slaveDes = slave.getModelDescription();

        //in meter
//        double distance = updatedVehicleData.getVehicleSensors().distance.front.distValue;

        // write to fmu
        speed = slaveDes.getVariableByName(SPEED).asRealVariable();
        Fmi4jVariableUtils.write(speed, slave, updatedVehicleData.getSpeed() * 3.6f);

        // simulate
        slave.doStep(stepSize);

        // read from fmu
        RealVariable speedGoal = slaveDes.getVariableByName(SPEED_GOAL).asRealVariable();
        getOs().changeSpeedWithInterval(Fmi4jVariableUtils.read(speedGoal, slave).getValue() / 3.6f, 5000);

        // test output: print velocity
        if(getOs().getId().equals("veh_0")){
//            System.out.println(updatedVehicleData.getSpeed() * 3.6f);

            System.out.println(new String(new char[(int)(updatedVehicleData.getSpeed() * 3.6f)]).replace("\0", "I"));
        }

        // teardown
        previousVars = currentVars;
        currentVars = slave.getModelVariables();
        lastStepTime = currentTime;
    }



    @Override
    public void onShutdown() {
        slave.terminate();
        fmu.close();
    }

    @Override
    public void processEvent(Event event) {
        // ...
    }

    private void reactOnEnvironmentData(SensorType sensorType, int strength){

    }

    public void setInputVariables(ModelVariables vars){}

    public void setOutputVariables(ModelVariables vars){}
}