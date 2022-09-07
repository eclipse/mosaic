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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.starter.MosaicSimulation;
import org.eclipse.mosaic.test.junit.LogAssert;
import org.eclipse.mosaic.test.junit.MosaicSimulationRule;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class SumoPredefinedScenarioIntegrationIT {

    @ClassRule
    public static MosaicSimulationRule simulationRule = new MosaicSimulationRule().logLevelOverride("TRACE");

    private static MosaicSimulation.SimulationResult simulationResult;

    private final static String VEH_0_MAPPING = "apps/veh_0/MappingVehicle.log";
    private final static String VEH_1_SUMO = "apps/veh_1/SumoVehicle.log";
    private final static String VEH_2_SUMO = "apps/veh_2/SumoVehicle.log";

    @BeforeClass
    public static void runSimulation() {
        simulationResult = simulationRule.executeTestScenario("sumo-predefined-scenario-integration");
    }

    @Test
    public void executionSuccessful() {
        assertNull(simulationResult.exception);
        assertTrue(simulationResult.success);
    }

    @Test
    public void allLogsCreated() {
        LogAssert.exists(simulationRule, VEH_0_MAPPING);
        LogAssert.exists(simulationRule, VEH_1_SUMO);
        LogAssert.exists(simulationRule, VEH_2_SUMO);
    }

    @Test
    public void allSumoVehiclesCanReadTheirRoutes() throws Exception {
        LogAssert.contains(simulationRule, VEH_1_SUMO, ".*I can read my route:.*");
        LogAssert.contains(simulationRule, VEH_2_SUMO, ".*I can read my route:.*");
    }

}
