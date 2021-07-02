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

public class SumoTraciByteArrayInteractionIT {

    @ClassRule
    public static MosaicSimulationRule simulationRule = new MosaicSimulationRule().logLevelOverride("TRACE");

    private static MosaicSimulation.SimulationResult simulationResult;

    private final static String LOG_VEH_0 = "apps/veh_0/SumoTraciInteractionApp.log";
    private final static String LOG_VEH_1 = "apps/veh_1/SumoTraciInteractionApp.log";

    @BeforeClass
    public static void runSimulation() {
        simulationResult = simulationRule.executeTestScenario("sumo-traci-app-interaction");
    }

    @Test
    public void executionSuccessful() {
        assertNull(simulationResult.exception);
        assertTrue(simulationResult.success);
    }

    @Test
    public void allLogsCreated() throws Exception {
        int receivedTraciResponseVeh0 = LogAssert.count(simulationRule, LOG_VEH_0,
                ".*Received TraCI message from Sumo. Speed of vehicle veh_0 is.*");
        int receivedTraciResponseVeh1 = LogAssert.count(simulationRule, LOG_VEH_0,
                ".*Received TraCI message from Sumo. Speed of vehicle veh_0 is.*");
        assertEquals(3, receivedTraciResponseVeh0);
        assertEquals(3, receivedTraciResponseVeh1);
}
}
