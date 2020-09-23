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
 */

package org.eclipse.mosaic.fed.sumo.traci.commands;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.fed.sumo.traci.complex.VehicleSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.rti.TIME;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class VehicleSetMoveToXyTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        // SETUP
        new VehicleSubscribe(traci.getTraciConnection()).execute(traci.getTraciConnection(), "1", 0L, 100 * TIME.SECOND);

        // PRE-ASSERT
        VehicleSubscriptionResult vehInfo =
                (VehicleSubscriptionResult) Iterables.getOnlyElement(simulateStep.execute(traci.getTraciConnection(), 6 * TIME.SECOND));
        assertEquals("1_1_2_1", vehInfo.road);
        assertEquals(5d, vehInfo.position.getX(), 5d);
        assertEquals(11d, vehInfo.position.getY(), 5d);

        // RUN
        new VehicleSetMoveToXY().execute(traci.getTraciConnection(), "1", "", 0,
                CartesianPoint.xyz(423.5, 2218.5, 0d), 270, VehicleSetMoveToXY.Mode.KEEP_ROUTE);

        // ASSERT
        vehInfo = (VehicleSubscriptionResult) Iterables.getOnlyElement(simulateStep.execute(traci.getTraciConnection(), 7 * TIME.SECOND));
        assertEquals("2_6_3_6", vehInfo.road);
        assertEquals(423d, vehInfo.position.getX(), 5d);
        assertEquals(2218d, vehInfo.position.getY(), 5d);
    }

}