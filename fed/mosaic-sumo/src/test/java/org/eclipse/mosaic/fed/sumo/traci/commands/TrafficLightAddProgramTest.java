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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.fed.sumo.traci.SumoVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.SumoTrafficLightLogic.Phase;
import org.eclipse.mosaic.fed.sumo.traci.junit.SinceSumo;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class TrafficLightAddProgramTest extends AbstractTraciCommandTest {

    @SinceSumo(SumoVersion.SUMO_1_9_x)
    @Test
    public void execute() throws Exception {
        List<Phase> phases = Lists.newArrayList(
                new Phase(10000, "rrrrrrrrrrr"),
                new Phase(10000, "ggggggggggg")
        );
        // RUN
        new TrafficLightAddProgram().execute(traci.getTraciConnection(), "2", "1", 0, phases);

        // ASSERT
        new TrafficLightSetProgram().execute(traci.getTraciConnection(), "2", "1");
        simulateStep.execute(traci.getTraciConnection(), 6 * TIME.SECOND);

        String currentState = new TrafficLightGetState().execute(traci.getTraciConnection(), "2");
        assertEquals("rrrrrrrrrrr", currentState);

        simulateStep.execute(traci.getTraciConnection(), 16 * TIME.SECOND);
        currentState = new TrafficLightGetState().execute(traci.getTraciConnection(), "2");
        assertEquals("ggggggggggg", currentState);
    }

}