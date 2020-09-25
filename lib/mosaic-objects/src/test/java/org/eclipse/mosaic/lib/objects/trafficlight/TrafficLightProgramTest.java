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

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TrafficLightProgramTest {

    TrafficLightProgram program;
    TrafficLightState state1 = new TrafficLightState(true, false, false);
    TrafficLightState state2 = new TrafficLightState(false, true, false);
    TrafficLightState state3 = new TrafficLightState(false, false, true);
    TrafficLightState state4 = new TrafficLightState(true, false, true);
    TrafficLightState state5 = new TrafficLightState(false, false, false);
    TrafficLightProgramPhase phase1;
    TrafficLightProgramPhase phase2;
    TrafficLightProgramPhase phase3;

    @Before
    public void setup() {
        phase1 = new TrafficLightProgramPhase(0, 31_000_000_000L, Lists.newArrayList(state1, state2));
        phase2 = new TrafficLightProgramPhase(1, 5_000_000_000L, Lists.newArrayList(state4, state3));
        phase3 = new TrafficLightProgramPhase(2, 31_000_000_000L, Lists.newArrayList(state2, state1));
        List<TrafficLightProgramPhase> phases = Lists.newArrayList(phase1, phase2, phase3);

        program = new TrafficLightProgram("0", phases, 0);
    }

    @Test
    public void getProgramDuration() {
        assertEquals("The actual program duration didn't match the expected one", 67_000_000_000L, program.getProgramDuration());
    }

    @Test
    public void getSignalSequence() {
        assertEquals("The sequence of states for a traffic light during a traffic light program phase didn't match the expected one", Lists.newArrayList(state1, state4, state2), program.getSignalSequence(0));
        assertEquals("The sequence of states for a traffic light during a traffic light program phase didn't match the expected one", Lists.newArrayList(state2, state3, state1), program.getSignalSequence(1));
    }

    @Test
    public void getProgramId() {
        assertEquals("The actual program id didn't match the expected one", "0", program.getProgramId());
    }

    @Test
    public void getPhases() {
        assertEquals(3, program.getPhases().size());
        assertEquals(phase1, program.getPhases().get(0));
        assertEquals(phase2, program.getPhases().get(1));
        assertEquals(phase3, program.getPhases().get(2));
    }

    @Test
    public void currentPhase() {
        assertEquals(0, program.getCurrentPhaseIndex());
        assertEquals(phase1, program.getCurrentPhase());
        program.setCurrentPhase(1);
        assertEquals(1, program.getCurrentPhaseIndex());
        assertEquals(phase2, program.getCurrentPhase());
        program.setCurrentPhase(0);
    }

}