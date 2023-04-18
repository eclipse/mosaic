/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.starter.MosaicSimulation;
import org.eclipse.mosaic.test.junit.LogAssert;
import org.eclipse.mosaic.test.junit.MosaicSimulationRule;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class SumoTeleportIT {

    @ClassRule
    public static MosaicSimulationRule simulationRule = new MosaicSimulationRule().logLevelOverride("TRACE");

    private static MosaicSimulation.SimulationResult simulationResult;


    private final static String VEH_LOG = "apps/veh_0/TeleportingVehicleApp.log";

    @BeforeClass
    public static void runSimulation() {
        simulationRule.watchdog(0);
        // use Tiergarten scenario with different route and mapping file
        simulationRule.federateConfigurationManipulator("mapping", (conf) -> conf.configuration = "teleport_mapping_config.json")
                .federateConfigurationManipulator("sumo", (conf) -> {
                    conf.configuration = "teleport_sumo_config.json";
                });
        simulationResult = simulationRule.executeTestScenario("sumo-predefined-scenario-integration");
    }

    @Test
    public void executionSuccessful() {
        assertNull(simulationResult.exception);
        assertTrue(simulationResult.success);
    }

    @Test
    public void allLogsCreated() {
        LogAssert.exists(simulationRule, VEH_LOG);
    }

    @Test
    public void vehicleTeleportsAndIsStillSubscribed() throws Exception {
        LogAssert.contains(simulationRule, VEH_LOG, ".*I moved from .* to .* before teleport.*");
        LogAssert.contains(simulationRule, VEH_LOG, ".*I started teleporting.*");
        LogAssert.contains(simulationRule, VEH_LOG, ".*I'm currently teleporting.*");
        LogAssert.contains(simulationRule, VEH_LOG, ".*I moved from .* to .* after teleport.*");
    }
}
