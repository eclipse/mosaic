package org.eclipse.mosaic.app.fmu;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class FmuConfigReader {
    public String configPath;
    public JsonObject fmuConfig;

    public FmuConfigReader(String configPath){
        this.configPath = configPath;

        Gson gson = new Gson();

        try(Reader reader = new FileReader(configPath)){
            fmuConfig = gson.fromJson(reader, JsonObject.class);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}