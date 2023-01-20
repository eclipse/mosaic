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
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class SimulationGetDepartedVehicleIdsTest extends AbstractTraciCommandTest {

    @Override
    public void simulateBefore() throws Exception {
        simulateStep.execute(traci.getTraciConnection(), 10 * TIME.SECOND);
    }

    @Test
    public void execute() throws Exception {
        // SETUP
        SimulationGetDepartedVehicleIds simulateGetDepartedVehicles = new SimulationGetDepartedVehicleIds();

        // RUN
        List<String> vehicleIdsDeparted = simulateGetDepartedVehicles.execute(traci.getTraciConnection());

        // ASSERT
        assertEquals(2, vehicleIdsDeparted.size());
        assertEquals(Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId("1"), vehicleIdsDeparted.get(0));
        assertEquals(Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId("0"), vehicleIdsDeparted.get(1));

        // RUN
        simulateStep.execute(traci.getTraciConnection(), 11 * TIME.SECOND);
        vehicleIdsDeparted = simulateGetDepartedVehicles.execute(traci.getTraciConnection());

        // ASSERT
        assertEquals(1, vehicleIdsDeparted.size());
        assertEquals(Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId("2"), vehicleIdsDeparted.get(0));

        // RUN
        simulateStep.execute(traci.getTraciConnection(), 11 * TIME.SECOND);
        vehicleIdsDeparted = simulateGetDepartedVehicles.execute(traci.getTraciConnection());
        assertTrue(vehicleIdsDeparted.isEmpty());
    }

}