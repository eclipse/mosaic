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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class FmuConfigReader {
    public JsonObject fmuConfig;

    public FmuConfigReader(String configPath){

        Gson gson = new Gson();

        try(Reader reader = new FileReader(configPath)){
            fmuConfig = gson.fromJson(reader, JsonObject.class);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}