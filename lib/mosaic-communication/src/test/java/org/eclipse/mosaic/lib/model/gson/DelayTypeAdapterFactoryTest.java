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

package org.eclipse.mosaic.lib.model.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.model.delay.ConstantDelay;
import org.eclipse.mosaic.lib.model.delay.Delay;
import org.eclipse.mosaic.lib.model.delay.GammaRandomDelay;
import org.eclipse.mosaic.lib.model.delay.GammaSpeedDelay;
import org.eclipse.mosaic.lib.model.delay.SimpleRandomDelay;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DelayTypeAdapterFactoryTest {

    private String delayListJson = null;
    private CDelayList delayListConfig = new CDelayList();

    @Before
    public void setup() {
        ConstantDelay constantDelay = new ConstantDelay();
        constantDelay.delay = 32;
        delayListConfig.delays.add(constantDelay);

        SimpleRandomDelay randomDelay = new SimpleRandomDelay();
        randomDelay.minDelay = 100;
        randomDelay.maxDelay = 110;
        delayListConfig.delays.add(randomDelay);

        GammaRandomDelay gammaRandomDelay = new GammaRandomDelay();
        gammaRandomDelay.minDelay = 250;
        gammaRandomDelay.expDelay = 300;
        delayListConfig.delays.add(gammaRandomDelay);

        GammaSpeedDelay gammaSpeedDelay = new GammaSpeedDelay();
        gammaSpeedDelay.minDelay = 350;
        gammaSpeedDelay.expDelay = 400;
        delayListConfig.delays.add(gammaSpeedDelay);
    }

    @Before
    public void loadTestJson() throws IOException {
        StringBuilder jsonStringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/TestDelay.json"), StandardCharsets.UTF_8))
        ) {
            reader.lines().forEach(jsonStringBuilder::append);
        }
        delayListJson = jsonStringBuilder.toString();
    }


    @Test
    public void write() {
        //RUN
        String json = new Gson().toJson(delayListConfig);

        //ASSERT
        assertEquals(StringUtils.deleteWhitespace(delayListJson), StringUtils.deleteWhitespace(json));
    }

    @Test
    public void load() {
        //RUN
        CDelayList configFromJson = new Gson().fromJson(delayListJson, CDelayList.class);

        //ASSERT
        assertEquals(4, configFromJson.delays.size());
        Delay first = configFromJson.delays.get(0);
        assertTrue(first instanceof ConstantDelay);

        Delay second = configFromJson.delays.get(1);
        assertTrue(second instanceof SimpleRandomDelay);

        Delay third = configFromJson.delays.get(2);
        assertTrue(third instanceof GammaRandomDelay);

        Delay fourth = configFromJson.delays.get(3);
        assertTrue(fourth instanceof GammaSpeedDelay);
    }

    @Test
    public void unitIO() throws IOException{

        StringBuilder jsonStringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/TestDelay_2.json"), StandardCharsets.UTF_8))
        ) {
            reader.lines().forEach(jsonStringBuilder::append);
        }
        String delayListJson2 = jsonStringBuilder.toString();

        CDelayList configFromJsonUnitless = new Gson().fromJson(delayListJson2, CDelayList.class);
        CDelayList configFromJsonUnit = new Gson().fromJson(delayListJson, CDelayList.class);

        //ASSERT
        Delay first = configFromJsonUnit.delays.get(0);
        assertTrue(first instanceof ConstantDelay);
        ConstantDelay cDelayUnit = (ConstantDelay)first;

        Delay second = configFromJsonUnitless.delays.get(0);
        assertTrue(second instanceof ConstantDelay);
        ConstantDelay cDelayUnitless = (ConstantDelay)first;

        assertEquals(cDelayUnit.delay, cDelayUnitless.delay);

    }

    static class CDelayList {
        List<Delay> delays = new ArrayList<>();
    }

}