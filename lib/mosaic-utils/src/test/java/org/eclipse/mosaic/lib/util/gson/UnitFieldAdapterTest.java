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

package org.eclipse.mosaic.lib.util.gson;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;

public class UnitFieldAdapterTest {

    @Mock
    private JsonReader jsonReader;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private TypeAdapter<Double> adapterUnderTest;

    @Test
    public void readNumber() throws IOException {
        adapterUnderTest = new UnitFieldAdapter.DistanceMeters().create(null, null);

        when(jsonReader.peek()).thenReturn(JsonToken.NUMBER);
        when(jsonReader.nextDouble()).thenReturn(1000d);

        //RUN
        double value = adapterUnderTest.read(jsonReader);

        //ASSERT
        assertEquals(1000d, value, 0.0001d);
    }

    @Test
    public void distanceReadString() throws IOException {
        adapterUnderTest = new UnitFieldAdapter.DistanceMetersQuiet().create(null, null);

        testConversion("50.1 meter", 50.1);
        testConversion("1 m", 1);
        testConversion("1.337 km", 1337);
        testConversion("1mm", 0.001);
        testConversion("1100 nm", 0.0000011);
        testConversion("1100nanometre", 0.0000011);
        testConversion("0.5kilometre", 500);
        testConversion("-5m", -5);
        testConversion("0m", 0);

        testConversion("", 0);
        testConversion("10 m/s", 0);
        testConversion("10km/h", 0);
        testConversion("10kmh", 0);
        testConversion("10mph", 0);
        testConversion("hour", 0);
    }

    @Test
    public void speedReadString() throws IOException {
        adapterUnderTest = new UnitFieldAdapter.SpeedMSQuiet().create(null, null);

        testConversion("50.1 meterpersecond", 50.1);
        testConversion("1 m/s", 1);
        testConversion("1 mps", 1);
        testConversion("1 meter/s", 1);
        testConversion("50 kmh", 13.888888888889);
        testConversion("50 km/h", 13.888888888889);
        testConversion("50kmperhour", 13.888888888889);
        testConversion("35 mph", 15.6464);
        testConversion("10 kmpersecond", 10000);
        testConversion("0 km/h", 0);

        testConversion("", 0);
        testConversion("10 ms", 0);
        testConversion("10km", 0);
        testConversion("hour", 0);
        testConversion("-10 km/h", 0);
    }

    @Test
    public void weightReadString() throws IOException {
        adapterUnderTest = new UnitFieldAdapter.WeightKiloGramsQuiet().create(null, null);

        testConversion("50.1 kg", 50.1);
        testConversion("50.1 kilograms", 50.1);
        testConversion("1 g", 0.001);
        testConversion("1337 kg", 1337);
        testConversion("1mg", 0.000001);
        testConversion("1100 ng", 0.0000000011);
        testConversion("1100nanograms", 0.0000000011);
        testConversion("0.5kilograms", 0.5);
        testConversion("-5kg", -5);
        testConversion("0kg", 0);

        testConversion("", 0);
        testConversion("10 m/s", 0);
        testConversion("10km/h", 0);
        testConversion("10kmh", 0);
        testConversion("10mph", 0);
        testConversion("hour", 0);
    }

    @Test
    public void voltageReadString() throws IOException {
        adapterUnderTest = new UnitFieldAdapter.VoltageVoltQuiet().create(null, null);

        testConversion("50.1 volt", 50.1);
        testConversion("50.1 volts", 50.1);
        testConversion("1 V", 1);
        testConversion("1 v", 1);
        testConversion("1.337 kV", 1337);
        testConversion("1mV", 0.001);
        testConversion("1100 nV", 0.0000011);
        testConversion("1100nanovolt", 0.0000011);
        testConversion("0.5kilovolt", 500);
        testConversion("-5V", -5);
        testConversion("0V", 0);

        testConversion("", 0);
        testConversion("10 m/s", 0);
        testConversion("10km/h", 0);
        testConversion("10kmh", 0);
        testConversion("10mph", 0);
        testConversion("hour", 0);
    }

    @Test
    public void currentReadString() throws IOException {
        adapterUnderTest = new UnitFieldAdapter.CurrentAmpereQuiet().create(null, null);

        testConversion("50.1 ampere", 50.1);
        testConversion("50.1 amperes", 50.1);
        testConversion("1 A", 1);
        testConversion("1.337 kA", 1337);
        testConversion("1mA", 0.001);
        testConversion("1100 nA", 0.0000011);
        testConversion("1100nanoampere", 0.0000011);
        testConversion("0.5kiloampere", 500);
        testConversion("-5A", -5);
        testConversion("0A", 0);

        testConversion("", 0);
        testConversion("10 m/s", 0);
        testConversion("10km/h", 0);
        testConversion("10kmh", 0);
        testConversion("10mph", 0);
        testConversion("hour", 0);
    }

    @Test
    public void capacityReadString() throws IOException {
        adapterUnderTest = new UnitFieldAdapter.CapacityAmpereHourQuiet().create(null, null);

        testConversion("50.1 Ah", 50.1);
        testConversion("50.1 ampereshours", 50.1);
        testConversion("1 Ah", 1);
        testConversion("1.337 kAh", 1337);
        testConversion("1mAh", 0.001);
        testConversion("1100 nAh", 0.0000011);
        testConversion("1100nanoamperehours", 0.0000011);
        testConversion("0.5kiloamperehours", 500);
        testConversion("-5Ah", -5);
        testConversion("0Ah", 0);

        testConversion("10A", 0);
        testConversion("", 0);
        testConversion("10 m/s", 0);
        testConversion("10km/h", 0);
        testConversion("10kmh", 0);
        testConversion("10mph", 0);
        testConversion("hour", 0);
    }


    private void testConversion(String input, double expected) throws IOException {
        //SETUP
        when(jsonReader.peek()).thenReturn(JsonToken.STRING);
        when(jsonReader.nextString()).thenReturn(input);

        //RUN
        double value = adapterUnderTest.read(jsonReader);

        //ASSERT
        assertEquals(String.format("Parsing of \"%s\" failed.", input), expected, value, 0.000000001d);
    }

}