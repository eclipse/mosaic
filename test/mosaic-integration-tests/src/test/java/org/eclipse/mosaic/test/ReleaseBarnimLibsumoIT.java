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

import org.eclipse.mosaic.fed.sumo.ambassador.LibSumoAmbassador;
import org.eclipse.mosaic.starter.MosaicSimulation;
import org.eclipse.mosaic.test.junit.LibsumoCheckRule;
import org.eclipse.mosaic.test.junit.LogAssert;
import org.eclipse.mosaic.test.junit.MosaicSimulationRule;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class ReleaseBarnimLibsumoIT {

    @ClassRule
    public static LibsumoCheckRule libsumoCheckRule = new LibsumoCheckRule();

    @ClassRule
    public static MosaicSimulationRule simulationRule = new MosaicSimulationRule().logLevelOverride("DEBUG");

    private static MosaicSimulation.SimulationResult simulationResult;

    @BeforeClass
    public static void runSimulation() {
        simulationResult = simulationRule
                .federateConfigurationManipulator("sumo", fed -> fed.classname = LibSumoAmbassador.class.getCanonicalName())
                .executeReleaseScenario("Barnim");
    }

    @Test
    public void executionSuccessful() {
        assertNull(simulationResult.exception);
        assertTrue(simulationResult.success);
    }

    @Test
    public void navigationSuccessful() throws Exception {
        assertEquals(24,
                LogAssert.count(simulationRule, "Navigation.log", ".*Request to switch to new route for vehicle .*")
        );
        assertEquals(14,
                LogAssert.count(simulationRule, "Navigation.log", ".*Change to route [2-9] for vehicle .*")
        );
    }

    @Test
    public void noMissingMethodError() throws Exception {
        assertEquals(0, LogAssert.count(simulationRule, "MOSAIC.log",
                ".*java.lang.Exception: No method found for Configuration root: .* Caused by OutputGenerator .*"
        ));
    }

    @Test
    public void correctUnitRegistrations() throws Exception {
        assertEquals(1, LogAssert.count(simulationRule, "output.csv",
                ".*RSU_REGISTRATION;.*"
        ));
        assertEquals(120, LogAssert.count(simulationRule, "output.csv",
                ".*VEHICLE_REGISTRATION;.*"
        ));
//        //FIXME currently, libsumo.TrafficLightGetPrograms returns an empty list in any case
//        assertEquals(53, LogAssert.count(simulationRule, "output.csv",
//                ".*TRAFFICLIGHT_REGISTRATION;.*"
//        ));
        LogAssert.contains(simulationRule, "output.csv", "RSU_REGISTRATION;0;rsu_0;52.65027;13.545;0.0;null;\\[.*\\]");
    }

}
