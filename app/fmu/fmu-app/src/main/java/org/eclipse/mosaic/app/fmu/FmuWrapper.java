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
import no.ntnu.ihb.fmi4j.VariableRead;
import no.ntnu.ihb.fmi4j.modeldescription.CoSimulationModelDescription;
import no.ntnu.ihb.fmi4j.importer.fmi2.CoSimulationSlave;
import no.ntnu.ihb.fmi4j.importer.fmi2.Fmu;
import no.ntnu.ihb.fmi4j.modeldescription.variables.*;

import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleParameters;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;



public class FmuWrapper {
    Hashtable<String, Hashtable<String, Object>> vars;
    Fmu fmu;
    CoSimulationSlave slave;

    private VehicleType currentVehicleType;
    private VehicleParameters currentVehicleParameters;
    private VehicleData currentVehicleData;

    public FmuWrapper(String configPath){
        FmuConfig fmuConfig = new FmuConfig(configPath);
        vars = fmuConfig.getActiveVariables();

        try {
            fmu = Fmu.from(new File(fmuConfig.fmuPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        slave = fmu.asCoSimulationFmu().newInstance();
        slave.simpleSetup();

        addSlaveVariables();
    }


    public void doStep(double stepSize){
        slave.doStep(stepSize);
    }


    public void terminate(){
        slave.terminate();
        fmu.close();
    }

    public void writeVariable(String variableName, double newValue){
        RealVariable v = (RealVariable) vars.get(variableName).get("fmuVar");
        Fmi4jVariableUtils.write(v, slave, newValue);
    }
    public void writeVariable(String variableName, boolean newValue){
        BooleanVariable v = (BooleanVariable) vars.get(variableName).get("fmuVar");
        Fmi4jVariableUtils.write(v, slave, newValue);
    }
    public void writeVariable(String variableName, String newValue){
        StringVariable v = (StringVariable) vars.get(variableName).get("fmuVar");
        Fmi4jVariableUtils.write(v, slave, newValue);
    }
    public void writeVariable(String variableName, int newValue){
        IntegerVariable v = (IntegerVariable) vars.get(variableName).get("fmuVar");
        Fmi4jVariableUtils.write(v, slave, newValue);
    }
    public void writeVariable(String variableName, Enum<?> newValue){
        EnumerationVariable v = (EnumerationVariable) vars.get(variableName).get("fmuVar");
        Fmi4jVariableUtils.write(v, slave, newValue);
    }

    public VariableRead<?> readVariable(String variableName){
        ScalarVariable c = (ScalarVariable) vars.get(variableName).get("fmuVar");

        return Fmi4jVariableUtils.read((TypedScalarVariable<?>) c, slave);
    }

    private void addSlaveVariables(){
        for(String varName: vars.keySet()){
            addVariable(varName, (String) vars.get(varName).get("name"), (VariableType) vars.get(varName).get("type"));
        }
    }

    private void addVariable(String varName, String internalName, VariableType type){
        CoSimulationModelDescription md = slave.getModelDescription();

        switch (type) {
            case REAL:
                vars.get(varName).put("fmuVar", md.getVariableByName(internalName).asRealVariable());
                break;
            case BOOLEAN:
                vars.get(varName).put("fmuVar", md.getVariableByName(internalName).asBooleanVariable());
                break;
            case INTEGER:
                vars.get(varName).put("fmuVar", md.getVariableByName(internalName).asIntegerVariable());
                break;
            case STRING:
                vars.get(varName).put("fmuVar", md.getVariableByName(internalName).asStringVariable());
                break;
            case ENUMERATION:
                vars.get(varName).put("fmuVar", md.getVariableByName(internalName).asEnumerationVariable());
                break;
            default:
                break;
        }
    }

    public void updateVehicle(VehicleParameters vehicleParameters, VehicleData vehicleData, VehicleType vehicleType){
        if(vehicleParameters != currentVehicleParameters){
            currentVehicleParameters = vehicleParameters;
            updateVehicleParameters(vehicleParameters);
        }
        if(vehicleData != currentVehicleData && vehicleData != null){
            currentVehicleData = vehicleData;
            updateVehicleData(vehicleData);
        }
        if(vehicleType != currentVehicleType){
            currentVehicleType = vehicleType;
            updateVehicleType(vehicleType);
        }
    }

    public Hashtable<String, Object> readVariables(){
        Hashtable<String, Object> ht = new Hashtable<>();

        for(String varName: vars.keySet()){
            if(vars.get(varName).get("direction").equals("out")){
                ht.put(varName, readVariable(varName).getValue());
            }
        }

        return ht;
    }

    public void updateVehicleParameters(VehicleParameters vehicleParameters) {
        Hashtable<String, Object> vp = new Hashtable<>();
        // Double
        vp.put("MIN_GAP", vehicleParameters.getMinimumGap());
        vp.put("MAX_SPEED", vehicleParameters.getMaxSpeed());
        vp.put("MAX_ACCELERATION", vehicleParameters.getMaxAcceleration());
        vp.put("MAX_DECELERATION", vehicleParameters.getMaxDeceleration());
        vp.put("REACTION_TIME", vehicleParameters.getReactionTime());

        writeFmuInputByHashtable(vp);
    }

    public void updateVehicleData(VehicleData vehicleData){
        Hashtable<String, Object> vd = new Hashtable<>();
        // Long
        vd.put("time", vehicleData.getTime());
        // Double
        vd.put("speed", vehicleData.getSpeed() * 3.6f);
        vd.put("heading", vehicleData.getHeading());
        vd.put("longitudinalAcceleration", vehicleData.getLongitudinalAcceleration());
        vd.put("distanceDriven", vehicleData.getDistanceDriven());
        vd.put("slope", vehicleData.getSlope());
        // Boolean
        vd.put("stopped", vehicleData.isStopped());
        // Others
        vd.put("currentLane", vehicleData.getRoadPosition().getLaneIndex());
        vd.put("positionLatitude", vehicleData.getPosition().getLatitude());
        vd.put("positionLongitude", vehicleData.getPosition().getLongitude());
        vd.put("positionX", vehicleData.getProjectedPosition().getX());
        vd.put("positionY", vehicleData.getProjectedPosition().getY());

        writeFmuInputByHashtable(vd);
    }

    public void updateVehicleType(VehicleType vehicleType){
        Hashtable<String, Object> vt = new Hashtable<>();
        vt.put("length", vehicleType.getLength());

        writeFmuInputByHashtable(vt);
    }

    private void writeFmuInputByHashtable(Hashtable<String, Object> ht){
        for(String varName: ht.keySet()){
            if(vars.containsKey(varName) && vars.get(varName).get("direction").equals("in")){
                Object varToWrite = ht.get(varName);

                if(varToWrite instanceof Double || varToWrite instanceof Long){
                    writeVariable(varName, (double) varToWrite);
                }else if(varToWrite instanceof Boolean){
                    writeVariable(varName, (boolean) varToWrite);
                }else if(varToWrite instanceof String){
                    writeVariable(varName, (String) varToWrite);
                }else if(varToWrite instanceof Integer){
                    writeVariable(varName, (int) varToWrite);
                }else if(varToWrite instanceof Enum<?>){
                    writeVariable(varName, (Enum<?>)varToWrite);
                }
            }
        }
    }
}
