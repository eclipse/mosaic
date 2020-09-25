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

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

public class TrafficLightProgramPhaseTest {

    TrafficLightProgramPhase onePhase;
    TrafficLightProgramPhase anotherPhase;

    @Before
    public void setup() throws Exception {
        TrafficLightState state1 = new TrafficLightState(true, false, false);;
        TrafficLightState state2 = new TrafficLightState(false, true, false);
        TrafficLightState state3 = new TrafficLightState(false, false, true);
        TrafficLightState state4 = new TrafficLightState(true, false, true);
        TrafficLightState state5 = new TrafficLightState(false, false, false);

        onePhase = new TrafficLightProgramPhase(0, 10_000_000_000L, Lists.newArrayList(state1, state2, state3, state4, state5));

        TrafficLightState state6 = new TrafficLightState(true, false, false);
        TrafficLightState state7 = new TrafficLightState(false, true, false);
        TrafficLightState state8 = new TrafficLightState(false, false, true);
        TrafficLightState state9 = new TrafficLightState(true, false, true);
        TrafficLightState state10 = new TrafficLightState(false, false, false);

        anotherPhase = new TrafficLightProgramPhase(1, 8_000_000_000L, Lists.newArrayList(state6, state7, state8, state9, state10));
    }

    @Test
    public void getId() {
        assertTrue("The actual id of a traffic light program phase didn't match the expected one", onePhase.getIndex() == 0);
        assertTrue("The actual id of a traffic light program phase didn't match the expected one", anotherPhase.getIndex() == 1);
    }

    @Test
    public void getConfiguredDuration() {
        assertTrue("The configured duration of a traffic light program phase didn't match the expected one", onePhase.getConfiguredDuration() == 10_000_000_000L);
        assertTrue("The actual configured duration of a traffic light program phase didn't match the expected one", anotherPhase.getConfiguredDuration() == 8_000_000_000L);
    }

    @Test
    public void changingRemainingDuration() {
        assertTrue("The remaining duration of a traffic light program phase didn't match the expected one", onePhase.getRemainingDuration() == 10_000_000_000L);
        onePhase.setRemainingDuration(12_000_000_000L);
        assertTrue("The configured duration of a traffic light program phase didn't match the expected one", onePhase.getConfiguredDuration() == 10_000_000_000L);
        assertTrue("The remaining duration of a traffic light program phase didn't match the expected one", onePhase.getRemainingDuration() == 12_000_000_000L);
        onePhase.setRemainingDuration(10_000_000_000L);
    }

    @Test
    public void getStates() {
        assertEquals("Traffic light state didn't match the expected one", "red", onePhase.getStates().get(0).toString());
        assertEquals("Traffic light state didn't match the expected one", "green", onePhase.getStates().get(1).toString());
        assertEquals("Traffic light state didn't match the expected one", "yellow", onePhase.getStates().get(2).toString());
        assertEquals("Traffic light state didn't match the expected one", "red-yellow", onePhase.getStates().get(3).toString());
        assertEquals("Traffic light state didn't match the expected one", "off", onePhase.getStates().get(4).toString());
    }

    @Test
    public void equalsOtherPhase() {
        assertTrue("States of two phases with equal states didn't match", onePhase.equalsOtherPhase(anotherPhase));
    }
}