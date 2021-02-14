package org.eclipse.mosaic.app.fmu;

import com.google.gson.JsonObject;
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
import java.nio.file.Paths;
import java.util.Hashtable;



public class FmuWrapper {
    Hashtable<String, Hashtable<String, Object>> vars;
    Fmu fmu;
    CoSimulationSlave slave;

    private VehicleType currentVehicleType;
    private VehicleParameters currentVehicleParameters;
    private VehicleData currentVehicleData;

    public final Hashtable<String, Hashtable<String, Object>> potentialVariables;

    public FmuWrapper(String configPath){
        FmuConfigWrapper fcw = new FmuConfigWrapper(configPath);
        vars = fcw.getActiveVariables();
        potentialVariables = fcw.getVariables();

        try {
            fmu = Fmu.from(new File(fcw.path));
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

        VariableRead<?> retVal = Fmi4jVariableUtils.read((TypedScalarVariable<?>) c, slave);

        return retVal;
    }

    private void addSlaveVariables(){
        for(String varName: vars.keySet()){
            addVariable(varName, (String) vars.get(varName).get("name"), (VariableType) vars.get(varName).get("type"));
//            vars.get(varName).remove("type");
//            vars.get(varName).remove("direction");
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

    public void fmuUpdateVehicle(VehicleParameters vehicleParameters, VehicleData vehicleData, VehicleType vehicleType){
        if(vehicleParameters != currentVehicleParameters){
            currentVehicleParameters = vehicleParameters;
            fmuUpdateVehicleParameters(vehicleParameters);
        }
        if(vehicleData != currentVehicleData && vehicleData != null){
            currentVehicleData = vehicleData;
            fmuUpdateVehicleData(vehicleData);
        }
        if(vehicleType != currentVehicleType){
            currentVehicleType = vehicleType;
            fmuUpdateVehicleType(vehicleType);
        }
    }

    public Hashtable<String, Object> fmuReadVariables(){
        Hashtable<String, Object> ht = new Hashtable<>();

        for(String varName: vars.keySet()){
            if(vars.get(varName).get("direction").equals("out")){
                ht.put(varName, readVariable(varName).getValue());
            }
        }

        return ht;
    }

    public void fmuUpdateVehicleParameters(VehicleParameters vehicleParameters) {
        Hashtable<String, Object> vp = new Hashtable<>();
        // Double
        vp.put("MIN_GAP", vehicleParameters.getMinimumGap());
        vp.put("MAX_SPEED", vehicleParameters.getMaxSpeed());
        vp.put("MAX_ACCELERATION", vehicleParameters.getMaxAcceleration());
        vp.put("MAX_DECELERATION", vehicleParameters.getMaxDeceleration());
        vp.put("REACTION_TIME", vehicleParameters.getReactionTime());

        writeFmuInputByHashtable(vp);
    }

    public void fmuUpdateVehicleData(VehicleData vehicleData){
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
//        nonDouble.put("position", vehicleData.getPosition());
//        nonDouble.put("projectedPosition", vehicleData.getProjectedPosition());

        writeFmuInputByHashtable(vd);
    }

    public void fmuUpdateVehicleType(VehicleType vehicleType){
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


class FmuConfigWrapper{

    JsonObject fmuConfig;
    public final String path;

    private Hashtable<String, Hashtable<String, Object>> activeVariables = new Hashtable<>();
    private Hashtable<String, Hashtable<String, Object>> allVariables = new Hashtable<>();

    FmuConfigWrapper(String configPath){
        FmuConfigReader fcr = new FmuConfigReader(configPath);
        fmuConfig = fcr.fmuConfig;
//        path = fmuConfig.get("fmuPathAbs").getAsString();
        path = Paths.get(fmuConfig.get("fmuPathAbs").getAsString()).toString();
        fillConfig();
        fillExternalNames();
    }

    public Hashtable<String, Hashtable<String, Object>> getActiveVariables() {
        return activeVariables;
    }
    public Hashtable<String, Hashtable<String, Object>> getVariables() {return allVariables;}


    private void fillExternalNames(){
        for(String internalName: activeVariables.keySet()){
            String dir = (String) activeVariables.get(internalName).get("direction");
            String externalName = getVariableNameByInternalName(internalName, dir);

            if(externalName != null){
                activeVariables.get(internalName).put("name", externalName);
                allVariables.put(internalName, activeVariables.get(internalName));
            }
        }

        //remove if name is unused
        activeVariables.entrySet().removeIf(entry -> (entry.getValue().get("name").equals("") || entry.getValue().get("name").equals(null)));
    }

    private String getVariableNameByInternalName(String name, String dir){
        JsonObject inputVars = fmuConfig.get("variables").getAsJsonObject().get("fmuIn").getAsJsonObject();
        JsonObject outputVars = fmuConfig.get("variables").getAsJsonObject().get("fmuOut").getAsJsonObject();

        String sRet = null;

        if(inputVars.has(name) && dir.equals("in")){
            sRet = inputVars.get(name).getAsString();
        }else if(outputVars.has(name) && dir.equals("out")){
            sRet = outputVars.get(name).getAsString();
        }

        return sRet;
    }

    private void fillConfig(){
        activeVariables.put("time", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.INTEGER);
                put("direction", "in");
            }
        });
        activeVariables.put("position", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.STRING);
                put("direction", "in");
            }
        });
        activeVariables.put("projectedPosition", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.STRING);
                put("direction", "in");
            }
        });
        activeVariables.put("speed", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("heading", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("stopped", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.BOOLEAN);
                put("direction", "in");
            }
        });
        activeVariables.put("longitudinalAcceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("distanceDriven", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("slope", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("MIN_GAP", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("MAX_SPEED", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("MAX_ACCELERATION", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("MAX_DECELERATION", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("REACTION_TIME", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("length", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("currentLane", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.INTEGER);
                put("direction", "in");
            }
        });
        activeVariables.put("speedGoal", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        activeVariables.put("laneChange", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.INTEGER);
                put("direction", "out");
            }
        });
        activeVariables.put("stop", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        activeVariables.put("resume", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        activeVariables.put("paramMaxSpeed", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        activeVariables.put("paramMaxAcceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        activeVariables.put("paramMaxDeceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        activeVariables.put("paramEmergencyDeceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        activeVariables.put("paramMinimumGap", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
    }
}
