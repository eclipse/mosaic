/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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


package org.eclipse.mosaic.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.starter.MosaicSimulation;
import org.eclipse.mosaic.test.junit.LogAssert;
import org.eclipse.mosaic.test.junit.MosaicSimulationRule;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class PerceptionModuleIT {
    @ClassRule
    public static MosaicSimulationRule simulationRule = new MosaicSimulationRule().watchdog(0);

    private static MosaicSimulation.SimulationResult simulationResult;

    private final static String PERCEPTION_VEHICLE_LOG = "apps/veh_0/SimplePerceptionApp.log";

    @BeforeClass
    public static void runSimulation() {
        simulationResult = simulationRule.executeTestScenario("perception-module");
    }

    @Test
    public void executionSuccessful() throws Exception {
        assertNull(simulationResult.exception);
        assertTrue(simulationResult.success);
    }

    @Test
    public void rightAmountOfVehiclesPerceived() throws Exception {
        // perceived vehicles repeat their route 10 times resulting in 11 perceptions
        assertEquals(11, LogAssert.count(simulationRule,
                        PERCEPTION_VEHICLE_LOG,
                        ".*Perceived vehicles: \\[veh_1\\].*"));
        assertEquals(11, LogAssert.count(simulationRule,
                        PERCEPTION_VEHICLE_LOG,
                        ".*Perceived vehicles: \\[veh_2\\].*"));
        assertEquals(11, LogAssert.count(simulationRule,
                PERCEPTION_VEHICLE_LOG,
                ".*Perceived vehicles: \\[veh_3\\].*"));
    }
}
