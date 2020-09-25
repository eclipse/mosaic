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

package org.eclipse.mosaic.fed.sns.config;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.mosaic.fed.sns.model.SimpleAdhocTransmissionModel;
import org.eclipse.mosaic.lib.model.delay.SimpleRandomDelay;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.rti.TIME;

import com.google.gson.JsonParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class CSnsTest {
    private URI validConfig;
    private URI invalidDelayConfig;


    @Before
    public void setUp() throws URISyntaxException {
        validConfig = Objects.requireNonNull(this.getClass().getClassLoader().getResource("sns_config_valid.json")).toURI();
        invalidDelayConfig = Objects.requireNonNull(this.getClass().getClassLoader().getResource("sns_config_invalid_delay.json")).toURI();
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
        CSns snsConfiguration = getSnsConfiguration(validConfig);
        // ASSERT
        assertNotNull(snsConfiguration);  // assert that configuration is created
        assertEquals(20, snsConfiguration.maximumTtl);
        assertNotNull(snsConfiguration.singlehopDelay);
        // AdhocTransmissionModel
        assertTrue(snsConfiguration.adhocTransmissionModel instanceof SimpleAdhocTransmissionModel);
        SimpleAdhocTransmissionModel transmissionModel = (SimpleAdhocTransmissionModel) snsConfiguration.adhocTransmissionModel;
        // simple multihop delay
        assertTrue(SimpleRandomDelay.class.isAssignableFrom(transmissionModel.simpleMultihopDelay.getClass()));
        assertEquals(6, ((SimpleRandomDelay) transmissionModel.simpleMultihopDelay).steps);
        assertEquals(10 * TIME.MILLI_SECOND, ((SimpleRandomDelay) transmissionModel.simpleMultihopDelay).minDelay, 0.01);
        assertEquals(30 * TIME.MILLI_SECOND, ((SimpleRandomDelay) transmissionModel.simpleMultihopDelay).maxDelay, 0.01);
        // simple multihop transmission
        // singlehop transmission
        assertEquals(0, transmissionModel.simpleMultihopTransmission.lossProbability, 0.01);
        assertEquals(1, transmissionModel.simpleMultihopTransmission.maxRetries);
        // singlehop delay
        assertTrue(SimpleRandomDelay.class.isAssignableFrom(snsConfiguration.singlehopDelay.getClass()));
        assertEquals(5, ((SimpleRandomDelay) snsConfiguration.singlehopDelay).steps);
        assertEquals(0.4 * TIME.MILLI_SECOND, ((SimpleRandomDelay) snsConfiguration.singlehopDelay).minDelay, 0.01);
        assertEquals(2.4 * TIME.MILLI_SECOND, ((SimpleRandomDelay) snsConfiguration.singlehopDelay).maxDelay, 0.01);

        // singlehop transmission
        assertEquals(0, snsConfiguration.singlehopTransmission.lossProbability, 0.01);
        assertEquals(0, snsConfiguration.singlehopTransmission.maxRetries);
    }

    /**
     * Simple test case using a sns configuration, that has an invalid Delay defined.
     */
    @Test
    public void readInvalidConfigWithWrongDelay_assertExceptions() {
        try {
            // SETUP + RUN
            getSnsConfiguration(invalidDelayConfig);
            fail("Expected InstantiationException");
        } catch (JsonParseException | InstantiationException e) {
            String message = e.getMessage();
            assertThat(message, startsWith("The CSns config is not valid: Exactly one of the following sets of problems must be resolved"));
        }
    }

    /**
     * Small helper class, which returns the instantiated object of a json-configuration.
     *
     * @param path the path to the configuration
     * @return the instantiated object
     * @throws InstantiationException if there was an error during deserialization/instantiation
     */
    private CSns getSnsConfiguration(URI path) throws InstantiationException {
        return new ObjectInstantiation<>(CSns.class).readFile(new File(path));
    }
}
