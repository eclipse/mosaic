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

package org.eclipse.mosaic.fed.application.config;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class CApplicationAmbassadorTest {
    private URI validConfig;
    private URI invalidConfig;

    @Before
    public void setUp() throws URISyntaxException {
        validConfig = Objects.requireNonNull(this.getClass().getClassLoader().getResource("application_config_valid.json")).toURI();
        invalidConfig = Objects.requireNonNull(this.getClass().getClassLoader().getResource("application_config_invalid.json")).toURI();
    }

    /**
     * Test case using a properly formed json configuration and asserting, that all values
     * were properly deserialized.
     *
     * @throws InstantiationException if configuration couldn't be properly deserialized, under normal circumstances this should not occur
     */
    @Test
    public void readValidConfig_assertProperties() throws InstantiationException {
        // SETUP + RUN
        CApplicationAmbassador applicationAmbassadorConfiguration = getApplicationAmbassadorConfiguration(validConfig);
        // ASSERT
        assertNotNull(applicationAmbassadorConfiguration);  // assert that configuration is created
        assertEquals(40 * TIME.SECOND, applicationAmbassadorConfiguration.messageCacheTime);
        assertTrue(applicationAmbassadorConfiguration.encodePayloads);
        assertNotNull(applicationAmbassadorConfiguration.navigationConfiguration);
        assertEquals("database", applicationAmbassadorConfiguration.navigationConfiguration.type);
    }

    /**
     * Simple test case using an application configuration, which has an invalid value. Asserting
     * for thrown exception and proper error message.
     */
    @Test
    public void readInvalidConfig_assertExceptions() {
        try {
            // SETUP + RUN
            getApplicationAmbassadorConfiguration(invalidConfig);
            fail("Expected InstantiationException");
        } catch (InstantiationException instantiationException) {
            // ASSERT
            assertThat(
                    instantiationException.getMessage(),
                    startsWith("The CApplicationAmbassador config is not valid:")
            );  // checking that proper Exception is thrown
        }
    }

    /**
     * Small helper class, which returns the instantiated object of a json-configuration.
     *
     * @param path the path to the configuration
     * @return the instantiated object
     * @throws InstantiationException if there was an error during deserialization/instantiation
     */
    private CApplicationAmbassador getApplicationAmbassadorConfiguration(URI path) throws InstantiationException {
        return new ObjectInstantiation<>(CApplicationAmbassador.class).readFile(new File(path));
    }
}