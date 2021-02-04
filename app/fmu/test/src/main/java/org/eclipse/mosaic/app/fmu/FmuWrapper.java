package org.eclipse.mosaic.app.fmu;

import com.google.gson.JsonObject;
import no.ntnu.ihb.fmi4j.Fmi4jVariableUtils;
import no.ntnu.ihb.fmi4j.VariableRead;
import no.ntnu.ihb.fmi4j.modeldescription.CoSimulationModelDescription;
import no.ntnu.ihb.fmi4j.importer.fmi2.CoSimulationSlave;
import no.ntnu.ihb.fmi4j.importer.fmi2.Fmu;
import no.ntnu.ihb.fmi4j.modeldescription.variables.*;

import no.ntnu.ihb.fmi4j.importer.AbstractFmu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Hashtable;



public class FmuWrapper {
    Hashtable<String, Hashtable<String, Object>> vars;
    Fmu fmu;
    CoSimulationSlave slave;

    Hashtable<String, Hashtable<String, Object>> h = new Hashtable<>();

    public static void main(String[] args){

        FmuWrapper f = new FmuWrapper();
        VariableRead r = f.readVariable("speed");
        double rf = (double) r.getValue();

        System.out.println(r);
        System.out.println(r.getClass());
    }

    public FmuWrapper(){
        FmuConfigWrapper fcw = new FmuConfigWrapper();
        vars = fcw.getVariables();

        try {
            fmu = Fmu.from(new File(fcw.path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        slave = fmu.asCoSimulationFmu().newInstance();

        addSlaveVariables();
        System.out.println(vars);
    }

//    public setVariable(String variableName, float newValue){
//
//    }
//    public setVariable(String variableName, boolean newValue){
//        return newValue;
//    }

    public VariableRead readVariable(String variableName){
        Hashtable<String, Object> var = vars.get(variableName);

        ScalarVariable c = (ScalarVariable) vars.get(variableName).get("fmuVar");

        VariableRead<?> retVal = Fmi4jVariableUtils.read((TypedScalarVariable<?>) c, slave);

//        System.out.println(retVal.getValue().getClass());

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

    public void evalOutput(){

    }

    private void doStep(double stepSize){
        slave.doStep(stepSize);
    }

    public void terminate(){
        slave.terminate();
        fmu.close();
    }
}

class FmuConfigWrapper{

    JsonObject fmuConfig = FmuConfigReader.fmuConfig;
    public final String path;

    public Hashtable<String, Hashtable<String, Object>> variables = new Hashtable<>();

    FmuConfigWrapper(){
//        path = fmuConfig.get("fmuPathAbs").getAsString();
        path = Paths.get(fmuConfig.get("fmuPathAbs").getAsString()).toString();
        fillConfig();
        fillExternalNames();
    }

    public Hashtable<String, Hashtable<String, Object>> getVariables() {
        return variables;
    }

    //    public static void main(String[] args){
//        fillConfig();
//        fillExternalNames();
//
//        System.out.println(variables);
//    }

    private void fillExternalNames(){
        for(String internalName: variables.keySet()){
            variables.get(internalName).put("name", getVariableNameByInternalName(internalName));
        }
         //remove if name is unused
        variables.entrySet().removeIf(entry -> entry.getValue().get("name").equals(""));
    }

    private String getVariableNameByInternalName(String name){
        JsonObject inputVars = fmuConfig.get("variables").getAsJsonObject().get("fmuIn").getAsJsonObject();
        JsonObject outputVars = fmuConfig.get("variables").getAsJsonObject().get("fmuOut").getAsJsonObject();

        String sRet = null;

        if(inputVars.has(name)){
            sRet = inputVars.get(name).getAsString();
        }else if(outputVars.has(name)){
            sRet = outputVars.get(name).getAsString();
        }

        return sRet;
    }

    private void fillConfig(){
        variables.put("time", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.INTEGER);
                put("direction", "in");
            }
        });
        variables.put("position", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.STRING);
                put("direction", "in");
            }
        });
        variables.put("projectedPosition", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.STRING);
                put("direction", "in");
            }
        });
        variables.put("speed", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("stopped", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.BOOLEAN);
                put("direction", "in");
            }
        });
        variables.put("longitudinalAcceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("distanceDriven", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("slope", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("MIN_GAP", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("MAX_SPEED", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("MAX_ACCELERATION", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("MAX_DECELERATION", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("REACTION_TIME", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("length", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        variables.put("speedGoal", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        variables.put("laneChange", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        variables.put("stop", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        variables.put("resume", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        variables.put("paramMaxSpeed", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        variables.put("paramMaxAcceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        variables.put("paramMaxDeceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        variables.put("paramEmergencyDeceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
        variables.put("paramMinimumGap", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "out");
            }
        });
    }
}
