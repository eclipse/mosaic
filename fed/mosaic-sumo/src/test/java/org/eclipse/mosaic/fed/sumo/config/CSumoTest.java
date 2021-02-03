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

package org.eclipse.mosaic.fed.sumo.config;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

public class CSumoTest {

    /**
     * Test case using a properly formed json configuration and asserting, that all values
     * were properly deserialized.
     *
     * @throws InstantiationException if configuration couldn't be properly deserialized, under normal circumstances this should not occur
     */
    @Test
    public void readValidConfig_assertProperties() throws InstantiationException {
        // SETUP + RUN
        String validConfig = "/config/sumo_config_valid.json";
        CSumo sumoConfiguration = getSumoConfiguration(validConfig);
        // ASSERT
        assertNotNull(sumoConfiguration);  // assert that configuration is created
        assertEquals(new Long(200L), sumoConfiguration.updateInterval);
        assertEquals("placeholder.sumocfg", sumoConfiguration.sumoConfigurationFile);
        assertEquals(Boolean.TRUE, sumoConfiguration.exitOnInsertionError);
        assertEquals(" --time-to-teleport 10  --seed 200000", sumoConfiguration.additionalSumoParameters);
        assertEquals("red", sumoConfiguration.additionalVehicleTypeParameters.get("car").get("color"));
        assertEquals("Krauss", sumoConfiguration.additionalVehicleTypeParameters.get("car").get("carFollowModel"));
        assertEquals("50", sumoConfiguration.additionalVehicleTypeParameters.get("car").get("maxSpeed"));
        assertEquals("10", sumoConfiguration.additionalVehicleTypeParameters.get("truck").get("maxSpeed"));
        assertEquals(400, sumoConfiguration.trafficFlowMeasurementWindowInS);
        assertEquals(4.5d, sumoConfiguration.timeGapOffset, 0.1d);
        Collection<String> expectedSubscriptions = Arrays.asList("roadposition", "signals", "emissions");
        assertEquals(expectedSubscriptions, sumoConfiguration.subscriptions);
    }

    /**
     * Simple test case using an eventserver configuration, which has missing time values. Asserting
     * for thrown exception and proper error message.
     */
    @Test
    public void readInvalidConfig_assertExceptions() {
        try {
            // SETUP + RUN
            String invalidConfig = "/config/sumo_config_invalid.json";
            getSumoConfiguration(invalidConfig);
            fail("Expected InstantiationException");
        } catch (InstantiationException instantiationException) {
            // ASSERT
            assertThat(
                    instantiationException.getMessage(),
                    startsWith(
                            "The CSumo config is not valid: [7,31][/additionalVehicleTypeParameters/car/carFollowModel]"
                                + " The value must be of string type, but actual type is integer."
                    )
            );  // checking that proper Exception is thrown
        }
    }

    /**
     * Small helper class, which returns the instantiated object of a json-configuration.
     *
     * @param filePath the path to the configuration
     * @return the instantiated object
     * @throws InstantiationException if there was an error during deserialization/instantiation
     */
    private CSumo getSumoConfiguration(String filePath) throws InstantiationException {
        return new ObjectInstantiation<>(CSumo.class).read(getClass().getResourceAsStream(filePath));
    }

}