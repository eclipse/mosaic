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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import no.ntnu.ihb.fmi4j.modeldescription.variables.VariableType;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Hashtable;


class FmuConfig{

    JsonObject fmuConfig;
    public final String fmuPath;

    private final Hashtable<String, Hashtable<String, Object>> activeVariables = new Hashtable<>();

    FmuConfig(String configPath){
        fmuConfig = getConfigFromFile(configPath);
        fmuPath = Paths.get(fmuConfig.get("fmuPathAbs").getAsString()).toString();
        fillConfig();
        fillExternalNames();
    }

    public Hashtable<String, Hashtable<String, Object>> getActiveVariables() {
        return activeVariables;
    }

    private JsonObject getConfigFromFile(String configPath){
        Gson gson = new Gson();

        JsonObject config = null;

        try(Reader reader = new FileReader(configPath)){
            config = gson.fromJson(reader, JsonObject.class);
        }catch (IOException e){
            e.printStackTrace();
        }

        return config;
    }

    private void fillExternalNames(){
        for(String internalName: activeVariables.keySet()){
            String dir = (String) activeVariables.get(internalName).get("direction");
            String externalName = getVariableNameByInternalName(internalName, dir);

            if(externalName != null){
                activeVariables.get(internalName).put("name", externalName);
            }
        }

        //remove variable if name is unused
        activeVariables.entrySet().removeIf(entry -> (entry.getValue().get("name").equals("")));
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
        activeVariables.put("positionLatitude", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("positionLongitude", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("positionX", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("positionY", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
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
        activeVariables.put("paramMinGap", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("maxSpeed", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("maxAcceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("maxDeceleration", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.REAL);
                put("direction", "in");
            }
        });
        activeVariables.put("reactionTime", new Hashtable<String, Object>() {
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
                put("type", VariableType.BOOLEAN);
                put("direction", "out");
            }
        });
        activeVariables.put("resume", new Hashtable<String, Object>() {
            {
                put("name", "");
                put("type", VariableType.BOOLEAN);
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
