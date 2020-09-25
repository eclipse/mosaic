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
 * Tests every CPrototype configuration that misses some required property.
 */
public class InvalidCPrototypeTest {

    /**
     * Expects an error due to missing properties of the prototype definition.
     * Missing properties:
     * - name
     */
    @Test
    public void missingProperties() {
        CMappingAmbassador mapping = null;
        try {
            mapping = new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(
                    "/mapping/invalid/prototype/MissingProperties.json"
            ));
        } catch (InstantiationException e) {
            assertTrue(e.getMessage().contains("[/prototypes/0] The object must have a property whose name is \"name\""));
        }
        assertNull(mapping);
    }
}
