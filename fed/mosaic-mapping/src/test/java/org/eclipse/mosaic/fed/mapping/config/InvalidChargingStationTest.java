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

package org.eclipse.mosaic.fed.mapping.config;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.junit.Test;

/**
 * Tests recursively every ChargingStation configuration that misses some required property.
 */
public class InvalidChargingStationTest {

    /**
     * Expects errors due to missing properties of the chargingStation definition.
     * Missing properties:
     * - position
     * - operator
     * - access
     * - chargingSpotDeserializers
     */
    @Test
    public void missingProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream("/mapping/invalid/chargingStation/MissingProperties.json"));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("[/chargingStations/0] The object must have a property whose name is \"position\""));
            assertTrue(e.getMessage().contains("[/chargingStations/0] The object must have a property whose name is \"operator\""));
            assertTrue(e.getMessage().contains("[/chargingStations/0] The object must have a property whose name is \"access\""));
            assertTrue(e.getMessage().contains("[/chargingStations/0] The object must have a property whose name is \"chargingSpots\""));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to missing properties of the chargingSpot definition.
     * Missing properties:
     * - type
     * - parkingPlaces
     */
    @Test
    public void missingChargingSpotProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/chargingStation/chargingSpot/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("[/chargingStations/0/chargingSpots/0]"
                    + " The object must have a property whose name is \"type\""));
            assertTrue(e.getMessage().contains("[/chargingStations/0/chargingSpots/0]"
                    + " The object must have a property whose name is \"parkingPlaces\""));
        }
        assertNull(mapping);
    }
}
