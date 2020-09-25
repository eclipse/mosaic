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

package org.eclipse.mosaic.rti.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MosaicVersionTest {

    @Test
    public void getVersion() {
        // RUN
        String version = MosaicVersion.get().toString();

        // ASSERT
        assertNotNull(version);
        assertTrue(version + " is in correct format", version.matches("^[0-9]+\\.[0-9]+(-SNAPSHOT)?$"));
    }


    /**
     * Test of compareTo method, of class Version on the base of a regular version string.
     */
    @Test
    public void testCompareToRegularBase() {
        MosaicVersion baseVersion = MosaicVersion.createFromString("2.0");

        // check all version components lower than base
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("0.0")));
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("0.1")));
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("1.0")));
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("1.1")));
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("1.9")));
        // check version the same as base
        assertEquals(0, baseVersion.compareTo(MosaicVersion.createFromString("2.0")));
        // check versions higher than base
        assertEquals(-1, baseVersion.compareTo(MosaicVersion.createFromString("2.1")));
        assertEquals(-1, baseVersion.compareTo(MosaicVersion.createFromString("3.0")));
        assertEquals(-1, baseVersion.compareTo(MosaicVersion.createFromString("3.1")));

        // check snapshot markers
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("2.0-SNAPSHOT")));
    }


    /**
     * Test of compareTo method, of class MosaicVersion on the base of a snapshot version string.
     */
    @Test
    public void testCompareToSnapshotBase() {
        // start with a regular base
        MosaicVersion baseVersion = MosaicVersion.createFromString("2.0-SNAPSHOT");

        // check all version components lower than base
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("0.0")));
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("0.1")));
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("1.0")));
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("1.1")));
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("1.9")));
        // check version the same as base
        assertEquals(0, baseVersion.compareTo(MosaicVersion.createFromString("2.0-SNAPSHOT")));
        // check versions higher than base
        assertEquals(-1, baseVersion.compareTo(MosaicVersion.createFromString("2.0")));
        assertEquals(-1, baseVersion.compareTo(MosaicVersion.createFromString("2.1")));
        assertEquals(-1, baseVersion.compareTo(MosaicVersion.createFromString("3.0")));
        assertEquals(-1, baseVersion.compareTo(MosaicVersion.createFromString("3.1")));

        // check snapshot markers
        assertEquals(1, baseVersion.compareTo(MosaicVersion.createFromString("1.9-SNAPSHOT")));
    }

}