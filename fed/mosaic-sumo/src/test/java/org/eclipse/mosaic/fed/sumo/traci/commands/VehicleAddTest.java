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
import static org.junit.Assert.assertNotEquals;

import org.eclipse.mosaic.fed.sumo.traci.complex.AbstractSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.complex.VehicleSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class VehicleAddTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        // RUN
        new VehicleAdd().execute(traci.getTraciConnection(), "veh_0", "1", "PKW", "0", "0", "max");

        // ASSERT
        new VehicleSubscribe(traci.getTraciConnection()).execute(traci.getTraciConnection(), "veh_0", 0, 10 * TIME.SECOND);
        List<AbstractSubscriptionResult> subscriptions = simulateStep.execute(traci.getTraciConnection(), 10 * TIME.SECOND);
        assertEquals(1, subscriptions.size());
        assertEquals("veh_0", Iterables.getOnlyElement(subscriptions).id);
        assertEquals(1.341011890611844, ((VehicleSubscriptionResult) Iterables.getOnlyElement(subscriptions)).heading, 0.01d);
        assertNotEquals("", ((VehicleSubscriptionResult) Iterables.getOnlyElement(subscriptions)).edgeId);
    }

    @Test
    public void execute_withDepartLaneModeFree() throws Exception {
        // RUN
        new VehicleAdd().execute(traci.getTraciConnection(), "veh_0", "1", "PKW", "free", "0", "max");
        new VehicleSubscribe(traci.getTraciConnection()).execute(traci.getTraciConnection(), "veh_0", 0, 10 * TIME.SECOND);

        // ASSERT
        List<AbstractSubscriptionResult> subscriptions = simulateStep.execute(traci.getTraciConnection(), 6 * TIME.SECOND);
        assertEquals(1, subscriptions.size());
        assertEquals("veh_0", Iterables.getOnlyElement(subscriptions).id);
        assertEquals(1, ((VehicleSubscriptionResult) Iterables.getOnlyElement(subscriptions)).laneIndex); // free lane "1" chosen
        assertEquals(1.34, ((VehicleSubscriptionResult) Iterables.getOnlyElement(subscriptions)).heading, 0.01);
    }

}