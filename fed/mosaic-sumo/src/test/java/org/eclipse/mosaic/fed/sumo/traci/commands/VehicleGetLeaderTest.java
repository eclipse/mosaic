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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.mosaic.fed.sumo.traci.TraciClient;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.complex.LeadingVehicle;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class VehicleGetLeaderTest extends AbstractTraciCommandTest {

    @Override
    public void simulateBefore() throws Exception {
        simulateStep.execute(traci.getTraciConnection(), 10 * TIME.SECOND);
    }

    private final VehicleGetLeader getLeader = new VehicleGetLeader();

    @Test
    public void execute_leadingVehicleExisting() throws Exception {
        // RUN
        LeadingVehicle leading = getLeader.execute(traci.getTraciConnection(), "0", 100d);

        // ASSERT
        assertNotNull(leading);
        assertEquals(TraciClient.VEHICLE_ID_TRANSFORMER.fromExternalId("1"), leading.getLeadingVehicleId());
    }

    @Test
    public void execute_noLeadingVehicleExisting() throws Exception {
        // RUN + ASSERT
        try {
            getLeader.execute(traci.getTraciConnection(), "1", 100d);
        } catch (TraciCommandException traciCommandException) {
            assertThat(traciCommandException.getMessage(), is("Couldn't get Leader of vehicle 1 with lookahead distance 100.000."));
        }
    }

}