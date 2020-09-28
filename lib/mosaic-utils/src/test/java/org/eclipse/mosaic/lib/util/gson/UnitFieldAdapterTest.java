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