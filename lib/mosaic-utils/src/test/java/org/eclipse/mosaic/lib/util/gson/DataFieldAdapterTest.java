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
 */

package org.eclipse.mosaic.lib.util.gson;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.rti.DATA;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;

public class DataFieldAdapterTest {

    @Mock
    private JsonReader jsonReader;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private TypeAdapter<Long> adapterUnderTest;

    @Test
    public void readNumber() throws IOException {
        adapterUnderTest = new DataFieldAdapter.Size().create(null, null);

        when(jsonReader.peek()).thenReturn(JsonToken.NUMBER);
        when(jsonReader.nextLong()).thenReturn(10000L);

        //RUN
        long value = adapterUnderTest.read(jsonReader);

        //ASSERT
        assertEquals(10000, value);
    }

    @Test
    public void readString() throws IOException {
        adapterUnderTest = new DataFieldAdapter.SizeQuiet().create(null, null);

        testConversion("1 b", DATA.BIT);
        testConversion("1 B", DATA.BYTE);

        testConversion("1", DATA.BIT);

        testConversion("1bit", DATA.BIT);
        testConversion("10 Byte", 10 * DATA.BYTE);

        testConversion("6 Mbit", 6 * DATA.MEGABIT);
        testConversion("6 MBit", 6 * DATA.MEGABIT);
        testConversion("6 MiByte", 6 * DATA.MEBIBYTE);
        testConversion("6.5 MB", (long) (6.5 * DATA.MEGABYTE));

        testConversion("0 ", 0);
    }

    @Test
    public void readString_Bandwidth() throws IOException {
        adapterUnderTest = new DataFieldAdapter.BandwidthQuiet().create(null, null);

        testConversion("1 bps", DATA.BIT);
        testConversion("1 Bps", DATA.BYTE);

        testConversion("1", DATA.BIT);

        testConversion("1bitps", DATA.BIT);
        testConversion("10 Byteps", 10 * DATA.BYTE);

        testConversion("6 Mbitps", 6 * DATA.MEGABIT);
        testConversion("6 MBitps", 6 * DATA.MEGABIT);
        testConversion("6 MiByteps", 6 * DATA.MEBIBYTE);
        testConversion("6.5 MBps", (long) (6.5 * DATA.MEGABYTE));

        testConversion("6 MB", 6 * DATA.MEGABYTE); // -> still valid, but results in log entry

        testConversion("0 ", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void readString_wrongSuffix_failOnError() throws IOException {
        adapterUnderTest = new DataFieldAdapter.Bandwidth().create(null, null);

        //SETUP
        when(jsonReader.peek()).thenReturn(JsonToken.STRING);
        when(jsonReader.nextString()).thenReturn("6 MB");

        //RUN
        adapterUnderTest.read(jsonReader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void readString_wrongUnit_failOnError() throws IOException {
        adapterUnderTest = new DataFieldAdapter.Size().create(null, null);

        //SETUP
        when(jsonReader.peek()).thenReturn(JsonToken.STRING);
        when(jsonReader.nextString()).thenReturn("1 second");

        //RUN
        adapterUnderTest.read(jsonReader);
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