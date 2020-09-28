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

package org.eclipse.mosaic.lib.util.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

public class ObjectInstantiationTest {

    @Test
    public void readValidObject() throws UnsupportedEncodingException, InstantiationException {

        String json = "{ " +
                " \"firstName\":\"First\"," +
                " \"lastName\":\"Last\"," +
                " \"age\": 42," +
                " \"gender\": \"MALE\"" +
                "}";

        TestConfig config = new ObjectInstantiation<>(TestConfig.class).read(new ByteArrayInputStream(json.getBytes("UTF-8")));

        assertNotNull(config);
        assertEquals("First", config.firstName);
        assertEquals("Last", config.lastName);
        assertEquals(42, config.age);
    }

    @Test(expected = InstantiationException.class)
    public void readInvalidObject_missingProperty() throws UnsupportedEncodingException, InstantiationException {

        String json = "{ " +
                " \"firstName\":\"First\"," +
                " \"age\":42" +
                "}";

        //should result in a JsonValidationException
        new ObjectInstantiation<>(TestConfig.class).read(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    @Test(expected = InstantiationException.class)
    public void readInvalidObject_wrongValue() throws UnsupportedEncodingException, InstantiationException {

        String json = "{ " +
                " \"lastName\":\"Last\"," +
                " \"age\":-1" +
                "}";

        //should result in a JsonValidationException
        new ObjectInstantiation<>(TestConfig.class).read(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    @Test(expected = InstantiationException.class)
    public void readInvalidObject_unknownEnumValue() throws UnsupportedEncodingException, InstantiationException {

        String json = "{ " +
                " \"lastName\":\"Last\"," +
                " \"age\":42," +
                " \"gender\": \"DINOSAUR\"" +
                "}";

        //should result in a JsonValidationException
        new ObjectInstantiation<>(TestConfig.class).read(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

}