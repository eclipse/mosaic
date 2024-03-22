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
 * Tests recursively every MatrixMapper configuration that misses some required property.
 */
public class InvalidMatrixMapperTest {

    /**
     * Expects errors due to missing properties of the MatrixMapper definition.
     * Missing properties:
     * - odValues
     * - points
     */
    @Test
    public void missingProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/matrixMapper/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("$.matrixMappers[0]: required property 'points' not found"));
            assertTrue(e.getMessage().contains("$.matrixMappers[0]: required property 'odValues' not found"));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to missing properties a point definition.
     * Missing properties:
     * - name
     * - position
     */
    @Test
    public void missingPointProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/matrixMapper/point/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("$.matrixMappers[0].points[0]: required property 'name' not found"));
            assertTrue(e.getMessage().contains("$.matrixMappers[0].points[0]: required property 'position' not found"));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to missing properties a point.position definition.
     * Missing properties:
     * - radius
     * - center
     */
    @Test
    public void missingPositionProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/matrixMapper/point/position/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("$.matrixMappers[0].points[0].position: required property 'radius' not found"));
            assertTrue(e.getMessage().contains("$.matrixMappers[0].points[0].position: required property 'center' not found"));
        }
        assertNull(mapping);
    }

    /**
     * Expects errors due to missing properties a point.position.center definition.
     * Missing properties:
     * - longitude
     * - latitude
     */
    @Test
    public void missingCenterProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/matrixMapper/point/position/center/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("$.matrixMappers[0].points[0].position.center: required property 'longitude' not found"));
            assertTrue(e.getMessage().contains("$.matrixMappers[0].points[0].position.center: required property 'latitude' not found"));
        }
        assertNull(mapping);
    }
}
