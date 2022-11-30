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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;
import org.eclipse.mosaic.lib.enums.VehicleStopMode;
import org.eclipse.mosaic.lib.objects.vehicle.StoppingPlace;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class VehicleGetStopsTest extends AbstractTraciCommandTest {

    private final VehicleGetStops vehicleGetStops = new VehicleGetStops();

    @Test
    public void execute_existingVehicle() throws Exception {
        // RUN
        List<StoppingPlace> upcomingStops = vehicleGetStops.execute(traci.getTraciConnection(), "3", 0); // get all upcoming stops
        List<StoppingPlace> nextStops = vehicleGetStops.execute(traci.getTraciConnection(), "3", 1); // get next stop
        // ASSERT
        assertEquals("bs_0", upcomingStops.get(0).getStoppingPlaceId());
        assertEquals("1_4_3_0", upcomingStops.get(0).getLaneId());
        assertEquals(VehicleStopMode.BUS_STOP, upcomingStops.get(0).getStopType());
        assertEquals(2, upcomingStops.size());
        assertEquals(1, nextStops.size());

        simulateStep.execute(traci.getTraciConnection(), 60 * TIME.SECOND);

        // RUN 2
        List<StoppingPlace> upcomingStops2 = vehicleGetStops.execute(traci.getTraciConnection(), "3", 0);
        List<StoppingPlace> nextStops2 = vehicleGetStops.execute(traci.getTraciConnection(), "3", 0);
        // ASSERT 2
        assertEquals(1, upcomingStops2.size()); // first stop finished
        assertNotEquals(upcomingStops.get(0), upcomingStops2.get(0)); // second stop is next stop
    }

    @Test(expected = CommandException.class)
    public void execute_noSuchVehicle() throws Exception {
        // RUN
        vehicleGetStops.execute(traci.getTraciConnection(), "x", 0);
    }

}
