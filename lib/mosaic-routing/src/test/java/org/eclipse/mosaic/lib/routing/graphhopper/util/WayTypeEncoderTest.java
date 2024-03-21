/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.routing.graphhopper.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WayTypeEncoderTest {

    @Test
    public void encodeDecodeMotorway() {
        int c = WayTypeEncoder.encode("motorway", 2);
        assertEquals("motorway", WayTypeEncoder.decode(c));
        assertTrue(WayTypeEncoder.isHighway(c));
        assertFalse(WayTypeEncoder.isResidential(c));
        assertFalse(WayTypeEncoder.isMainRoad(c));
        assertFalse(WayTypeEncoder.isCycleway(c));
    }

    @Test
    public void encodeDecodeSecondary() {
        int c = WayTypeEncoder.encode("secondary", 2);
        assertEquals("secondary", WayTypeEncoder.decode(c));
        assertFalse(WayTypeEncoder.isHighway(c));
        assertFalse(WayTypeEncoder.isResidential(c));
        assertTrue(WayTypeEncoder.isMainRoad(c));
        assertFalse(WayTypeEncoder.isCycleway(c));
    }

    @Test
    public void encodeDecodeCycleway() {
        int c = WayTypeEncoder.encode("cycleway", 1);
        assertEquals("cycleway", WayTypeEncoder.decode(c));
        assertFalse(WayTypeEncoder.isHighway(c));
        assertFalse(WayTypeEncoder.isResidential(c));
        assertFalse(WayTypeEncoder.isMainRoad(c));
        assertTrue(WayTypeEncoder.isCycleway(c));
    }

}