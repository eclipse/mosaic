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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.SumoTrafficLightLogic;
import org.eclipse.mosaic.fed.sumo.junit.SinceTraci;
import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class TrafficLightGetProgramsTest extends AbstractTraciCommandTest {

    @Override
    public void simulateBefore() throws Exception {
        simulateStep.execute(traci.getTraciConnection(), 34 * TIME.SECOND);
    }

    @SinceTraci(TraciVersion.API_19)
    @Test
    public void execute() throws Exception {
        // RUN
        List<SumoTrafficLightLogic> programs = new TrafficLightGetPrograms().execute(traci.getTraciConnection(), "2");

        // ASSERT
        assertEquals(1, programs.size());

        SumoTrafficLightLogic tlProgram = Iterables.getOnlyElement(programs);
        assertEquals("0", tlProgram.getLogicId());
        assertEquals(1, tlProgram.getCurrentPhase());
        assertEquals(6, tlProgram.getPhases().size());
        assertEquals(4000, tlProgram.getPhases().get(tlProgram.getCurrentPhase()).getDuration());
        assertEquals("yyggrrryyyg", tlProgram.getPhases().get(tlProgram.getCurrentPhase()).getPhaseDef());
    }

    @SinceTraci(TraciVersion.API_19)
    @Test(expected = CommandException.class)
    public void execute_noSuchTrafficLight() throws Exception {
        // RUN
        new TrafficLightGetPrograms().execute(traci.getTraciConnection(), "3");
    }

}