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

package org.eclipse.mosaic.fed.mapping.config;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.junit.Test;

/**
 * Tests recursively every vehicle configuration that misses some required property.
 */
public class InvalidVehicleTest {

    /**
     * Expects errors due to missing properties of vehicle definition.
     * Missing properties:
     * - types
     */
    @Test
    public void missingProperties() {
        ObjectInstantiation<CMappingAmbassador> oi = new ObjectInstantiation<>(CMappingAmbassador.class);
        CMappingAmbassador mapping = null;
        try {
            mapping = oi.read(getClass().getResourceAsStream(
                    "/mapping/invalid/vehicle/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("$.vehicles[0]: should be valid to one and only one schema, but 0 are valid"));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to an empty "types" list of a vehicle configuration.
     */
    @Test
    public void emptyTypes() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/vehicle/type/EmptyTypes.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("$.vehicles[0].types: expected at least 1 items but found 0"));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to missing properties of a type definition.
     * Missing type properties:
     * - name
     */
    @Test
    public void missingTypesItemProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/vehicle/type/EmptyTypesItem.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("$.vehicles[0].types[0]: required property 'name' not found"));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to missing properties of origin definition.
     * Missing origin properties:
     * - center
     * - radius
     */
    @Test
    public void missingOriginProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/vehicle/origin/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("$.vehicles[0].origin: required property 'radius' not found"));
            assertTrue(e.getMessage().contains("$.vehicles[0].origin: required property 'center' not found"));
            assertTrue(e.getMessage().contains("$.vehicles[0]: should be valid to one and only one schema, but 0 are valid"));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to missing properties of origin.center definition.
     * Missing center properties:
     * - longitude
     * - latitude
     */
    @Test
    public void missingOriginCenterProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/vehicle/origin/center/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("$.vehicles[0].origin.center: required property 'longitude' not found"));
            assertTrue(e.getMessage().contains("$.vehicles[0].origin.center: required property 'latitude' not found"));
            assertTrue(e.getMessage().contains("$.vehicles[0]: should be valid to one and only one schema, but 0 are valid"));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to missing properties of destination definition.
     * Missing destination properties:
     * - center
     * - radius
     */
    @Test
    public void missingDestinationProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/vehicle/destination/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("$.vehicles[0].destination: required property 'radius' not found"));
            assertTrue(e.getMessage().contains("$.vehicles[0].destination: required property 'center' not found"));
            assertTrue(e.getMessage().contains("$.vehicles[0]: should be valid to one and only one schema, but 0 are valid"));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to missing properties of destination.center definition.
     * Missing center properties:
     * - longitude
     * - latitude
     */
    @Test
    public void missingDestinationCenterProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/vehicle/destination/center/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("$.vehicles[0].destination.center: required property 'longitude' not found"));
            assertTrue(e.getMessage().contains("$.vehicles[0].destination.center: required property 'latitude' not found"));
            assertTrue(e.getMessage().contains("$.vehicles[0]: should be valid to one and only one schema, but 0 are valid"));
        }
        assertNull(mapping);
    }
}
