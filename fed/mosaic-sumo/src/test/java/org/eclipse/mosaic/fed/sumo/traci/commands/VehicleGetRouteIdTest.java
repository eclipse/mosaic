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

import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class VehicleGetRouteIdTest extends AbstractTraciCommandTest {

    private final VehicleGetRouteId getRouteId = new VehicleGetRouteId();

    @Test
    public void execute_existingVehicle() throws Exception {
        // RUN
        String routeId = getRouteId.execute(traci.getTraciConnection(), "0");

        // ASSERT
        assertEquals("0", routeId);
    }

    @Test(expected = TraciCommandException.class)
    public void execute_noSuchVehicle() throws Exception {
        // RUN
        getRouteId.execute(traci.getTraciConnection(), "x");
    }

}