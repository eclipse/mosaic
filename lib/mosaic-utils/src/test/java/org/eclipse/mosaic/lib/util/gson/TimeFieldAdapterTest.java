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

import org.eclipse.mosaic.rti.TIME;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;

public class TimeFieldAdapterTest {

    @Mock
    private JsonReader jsonReader;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private TypeAdapter<Long> adapterUnderTest;

    @Test
    public void readNumber() throws IOException {
        adapterUnderTest = new TimeFieldAdapter.NanoSeconds().create(null, null);

        when(jsonReader.peek()).thenReturn(JsonToken.NUMBER);
        when(jsonReader.nextLong()).thenReturn(10000L);

        //RUN
        long value = adapterUnderTest.read(jsonReader);

        //ASSERT
        assertEquals(10000, value);
    }

    @Test
    public void readString() throws IOException {
        adapterUnderTest = new TimeFieldAdapter.NanoSecondsQuiet().create(null, null);

        testConversion("50.1 µs", 50 * TIME.MICRO_SECOND + 100 * TIME.NANO_SECOND);
        testConversion("1 second", TIME.SECOND);
        testConversion("1s", TIME.SECOND);
        testConversion("0.5 sec", 500 * TIME.MILLI_SECOND);
        testConversion("1", TIME.NANO_SECOND);
        testConversion("10ns", 10 * TIME.NANO_SECOND);
        testConversion("20000 ms", 20000 * TIME.MILLI_SECOND);
        testConversion("10 microseconds", 10 * TIME.MICRO_SECOND);
        testConversion("10 min", 10 * 60 * TIME.SECOND);
        testConversion("0.5h", 30 * 60 * TIME.SECOND);
        testConversion("2 hours", 2 * 3600 * TIME.SECOND);
        testConversion("1000000000", 1 * TIME.SECOND);
        testConversion("1_000_000_000", 1 * TIME.SECOND);

        testConversion("0", 0);
        testConversion("", 0);
        testConversion("hour", 0);
    }

    @Test
    public void readString_LegacyMilliSeconds() throws IOException {
        adapterUnderTest = new TimeFieldAdapter.LegacyMilliSecondsQuiet().create(null, null);

        testConversion("1 second", 1000);
        testConversion("1s", 1000);
        testConversion("0.5 sec", 500);
        testConversion("1", 1);
        testConversion("10ns", 0);
        testConversion("20000 ms", 20000);
        testConversion("10 min", 10 * 60 * 1000);
        testConversion("0.5h", 30 * 60 * 1000);
        testConversion("2 hours", 2 * 3600 * 1000);

        testConversion("0", 0);
        testConversion("", 0);
        testConversion("hour", 0);
    }

    @Test
    public void readNumber_LegacyMilliSeconds() throws IOException {
        adapterUnderTest = new TimeFieldAdapter.LegacyMilliSecondsQuiet().create(null, null);

        when(jsonReader.peek()).thenReturn(JsonToken.NUMBER);
        when(jsonReader.nextLong()).thenReturn(100L);

        //RUN
        long value = adapterUnderTest.read(jsonReader);
        assertEquals(100, value);
    }

    private void testConversion(String input, long expected) throws IOException {
        //SETUP
        when(jsonReader.peek()).thenReturn(JsonToken.STRING);
        when(jsonReader.nextString()).thenReturn(input);

        //RUN
        long value = adapterUnderTest.read(jsonReader);

        //ASSERT
        assertEquals(String.format("Parsing of \"%s\" failed.", input), expected, value);
    }

}