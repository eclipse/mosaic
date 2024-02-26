/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.cell.utility;

import org.eclipse.mosaic.fed.cell.config.CRegion;
import org.eclipse.mosaic.fed.cell.config.gson.RegionsNamingStrategy;
import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.util.ConfigurationReader;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.model.delay.GammaRandomDelay;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Helper that configures a GammaRandomDelay in a region.json file and writes it back to the source file.
 */
public class RegionsConfigGammaDelayUtility {
    public static CRegion getRegionsConfig(String regionConfigurationPath) {
        CRegion regionConfig = null;
        try {
            regionConfig = ConfigurationReader.importRegionConfig(regionConfigurationPath);
        } catch (InternalFederateException e) {
            throw new RuntimeException(e);
        }

        return regionConfig;
    }

    /**
     * Sets all transmission delays (uplink, downlink.multicst, downlink.unicast) to 'GammaRandomDelay'.
     *
     * @param cRegion The region configuration object
     * @param minDelay The minimum delay value (ns)
     * @param expDelay The expected delay value (ns)
     */
    public static void setGammaRandomDelay(CRegion cRegion, long minDelay, long expDelay) {
        GammaRandomDelay delay = new GammaRandomDelay();
        delay.expDelay = expDelay;
        delay.minDelay = minDelay;

        for (CNetworkProperties region : cRegion.regions) {
            region.downlink.multicast.delay = delay;
            region.downlink.unicast.delay = delay;
            region.uplink.delay = delay;
        }
    }

    /**
     * Writes the region configuration to a .json file.
     *
     * @param regionConfigurationPath The path to the output file (*.json)
     * @param cRegion The region configuration object
     */
    public static void exportRegionsConfig(String regionConfigurationPath, CRegion cRegion) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GeoPoint.class, new GeoPolygonAdapter())
                .setFieldNamingStrategy(new RegionsNamingStrategy())
                .setPrettyPrinting().create();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(regionConfigurationPath), Charsets.UTF_8)) {
            gson.toJson(cRegion, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error exporting regions config. \n"
                    + "The file exists but is a directory rather than a regular file, does not exist but cannot be created, "
                    + "or cannot be opened for any other reason. \n"
                    + "Path: " + regionConfigurationPath);
        }

    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: RegionsConfigUtility <path-to-regions.json> <minDelay (ns)> <expDelay (ns)>");
        }

        String regionConfigurationPath = args[0];
        long minDelay = Long.parseLong(args[1]);
        long expDelay = Long.parseLong(args[2]);

        CRegion cRegion = getRegionsConfig(regionConfigurationPath);
        setGammaRandomDelay(cRegion, minDelay, expDelay);
        exportRegionsConfig(regionConfigurationPath, cRegion);
    }


    static class GeoPolygonAdapter extends TypeAdapter<GeoPoint> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, GeoPoint point) throws IOException {
            out.beginObject();
            out.name("lon");
            out.value(point.getLongitude());
            out.name("lat");
            out.value(point.getLatitude());
            out.endObject();
        }

        @Override
        public GeoPoint read(JsonReader in) throws IOException {
            return null;
        }
    }
}
