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

public class HighwayReleaseIT {

    @ClassRule
    public static MosaicSimulationRule simulationRule = new MosaicSimulationRule();

    private static MosaicSimulation.SimulationResult simulationResult;

    @BeforeClass
    public static void runSimulation() {
        simulationResult = simulationRule.executeReleaseScenario("Highway");
    }

    @Test
    public void executionSuccessful() {
        assertNull(simulationResult.exception);
        assertTrue(simulationResult.success);
    }

    /**
     * This checks whether a cell message takes the expected amount to be send from tmc to vehicle
     * and from vehicle to tmc.
     * Should be 50ms for tmc uplink + 200ms for vehicle downlink + 200ms for vehicle uplink + 50ms for tmc downlink.
     */
    @Test
    public void roundTripMessageTakesRightAmountOfTime() throws Exception {
        final String tmcLog = "apps/tmc_0/SendAndReceiveRoundTripMessage.log";
        final String vehLog = "apps/veh_0/ReceiveAndReturnRoundTripMessage.log";
        LogAssert.exists(simulationRule, tmcLog);
        LogAssert.exists(simulationRule, vehLog);
        long timeOfArrivalAtVehicle = 10250000000L;
        long timeOfArrivalAtTmc = 10500000000L;
        LogAssert.contains(
                simulationRule,
                vehLog,
                ".*Received round trip message #0 at time " + timeOfArrivalAtVehicle + ".*"
        );
        LogAssert.contains(
                simulationRule,
                tmcLog,
                ".*Received round trip message #1 at time " + timeOfArrivalAtTmc + ".*"
        );
    }
}
