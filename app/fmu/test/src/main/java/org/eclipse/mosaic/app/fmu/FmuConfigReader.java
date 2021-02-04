package org.eclipse.mosaic.app.fmu;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class FmuConfigReader {
    public static String configPath = "app/fmu/test/config/fmu_config.json";
    public static JsonObject fmuConfig;
//    public static void main(String[] args){
//        String cPath = "app/fmu/test/config/fmu_config.json";
//        new FmuConfigReader(cPath);
//    }
    static{
        Gson gson = new Gson();

        try(Reader reader = new FileReader(configPath)){
            fmuConfig = gson.fromJson(reader, JsonObject.class);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}