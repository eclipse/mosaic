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

package org.eclipse.mosaic.lib.objects.trafficlight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class TrafficLightStateTest {

    TrafficLightState state1;
    TrafficLightState state2;
    TrafficLightState state3;
    TrafficLightState state4;
    TrafficLightState state5;


    @Before
    public void setup() {
        state1 = new TrafficLightState(false, false, false);
        state2 = new TrafficLightState(true, false, false);
        state3 = new TrafficLightState(false, true, false);
        state4 = new TrafficLightState(false, false, true);
        state5 = new TrafficLightState(true, false, true);
    }


    @Test
    public void states() {
        assertTrue("Initialized state didn't match the expected one", state1.isOff());
        assertTrue("Initialized state didn't match the expected one", state2.isRed());
        assertTrue("Initialized state didn't match the expected one", state3.isGreen());
        assertTrue("Initialized state didn't match the expected one", state4.isYellow());
        assertTrue("Initialized state didn't match the expected one", state5.isRedYellow());
    }

    @Test
    public void testToString() {
        assertEquals("The output of the overridden toString() didn't match the expected one", "off", state1.toString());
        assertEquals("The output of the overridden toString() didn't match the expected one", "red", state2.toString());
        assertEquals("The output of the overridden toString() didn't match the expected one", "green", state3.toString());
        assertEquals("The output of the overridden toString() didn't match the expected one", "yellow", state4.toString());
        assertEquals("The output of the overridden toString() didn't match the expected one", "red-yellow", state5.toString());
    }
}