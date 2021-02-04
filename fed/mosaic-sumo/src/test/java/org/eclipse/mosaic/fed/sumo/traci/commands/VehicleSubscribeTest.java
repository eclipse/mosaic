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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.eclipse.mosaic.fed.sumo.traci.SumoVersion;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.complex.AbstractSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.complex.VehicleSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.List;

@RunWith(SumoRunner.class)
public class VehicleSubscribeTest extends AbstractTraciCommandTest {

    @Test
    public void execute_vehicleAlreadyDeparted() throws Exception {
        // PRE-ASSERT
        List<AbstractSubscriptionResult> subscriptions = simulateStep.execute(traci.getTraciConnection(), 6 * TIME.SECOND);
        assertTrue(subscriptions.isEmpty());

        // RUN
        new VehicleSubscribe(traci.getTraciConnection()).execute(traci.getTraciConnection(), "1", 0L, 10 * TIME.SECOND);

        // ASSERT
        subscriptions = simulateStep.execute(traci.getTraciConnection(), 10 * TIME.SECOND);
        assertEquals(1, subscriptions.size());
        assertEquals("1_1_2", ((VehicleSubscriptionResult) Iterables.getOnlyElement(subscriptions)).road);

        subscriptions = simulateStep.execute(traci.getTraciConnection(), 11 * TIME.SECOND);
        assertTrue(subscriptions.isEmpty());

    }

    @Test
    public void execute_vehicleNotYetDeparted() throws Exception {
        // RUN
        new VehicleSubscribe(traci.getTraciConnection()).execute(traci.getTraciConnection(), "0", 0L, 10 * TIME.SECOND);

        // ASSERT
        List<AbstractSubscriptionResult> subscriptions = simulateStep.execute(traci.getTraciConnection(), 6 * TIME.SECOND);
        assertEquals(1, subscriptions.size());
        assertEquals("", ((VehicleSubscriptionResult) Iterables.getOnlyElement(subscriptions)).road);
    }

    @Test
    public void execute_vehicleDeparted_olderSumoVersion() throws Exception {
        // PRE-ASSERT
        List<AbstractSubscriptionResult> subscriptions = simulateStep.execute(traci.getTraciConnection(), 6 * TIME.SECOND);
        assertTrue(subscriptions.isEmpty());

        // Override TraciVersion with LOWEST
        traci = Mockito.spy(traci);
        TraciConnection traciConnectionSpy = spy(traci.getTraciConnection());
        doReturn(traciConnectionSpy).when(traci).getTraciConnection();
        doReturn(SumoVersion.LOWEST).when(traciConnectionSpy).getCurrentVersion();

        // RUN
        new VehicleSubscribe(traci.getTraciConnection()).execute(traci.getTraciConnection(), "1", 0L, 10 * TIME.SECOND);

        // ASSERT
        subscriptions = simulateStep.execute(traci.getTraciConnection(), 10 * TIME.SECOND);
        assertEquals(1, subscriptions.size());

        VehicleSubscriptionResult result = (VehicleSubscriptionResult) Iterables.getOnlyElement(subscriptions);
        assertEquals("1_1_2", result.road);
        assertEquals(0d, result.lateralLanePosition, 0d);

        subscriptions = simulateStep.execute(traci.getTraciConnection(), 11 * TIME.SECOND);
        assertTrue(subscriptions.isEmpty());

    }

}