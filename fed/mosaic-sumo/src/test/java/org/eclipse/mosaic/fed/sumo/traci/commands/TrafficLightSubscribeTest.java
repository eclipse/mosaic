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

import org.eclipse.mosaic.fed.sumo.traci.complex.AbstractSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.complex.TrafficLightSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class TrafficLightSubscribeTest extends AbstractTraciCommandTest {


    @Test
    public void execute() throws Exception {
        new TrafficLightSubscribe().execute(traci.getTraciConnection(), "2", 0, 100 * TIME.SECOND);

        List<AbstractSubscriptionResult> subscriptions = simulateStep.execute(traci.getTraciConnection(), 30 * TIME.SECOND);

        assertEquals(1, subscriptions.size());
        assertEquals(0, ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).currentPhaseIndex);
        assertEquals(31 * TIME.SECOND,
                ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).assumedNextPhaseSwitchTime);

        subscriptions = simulateStep.execute(traci.getTraciConnection(), 32 * TIME.SECOND);
        assertEquals(1, ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).currentPhaseIndex);
        assertEquals(35 * TIME.SECOND,
                ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).assumedNextPhaseSwitchTime);

        subscriptions = simulateStep.execute(traci.getTraciConnection(), 36 * TIME.SECOND);
        assertEquals(2, ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).currentPhaseIndex);
        assertEquals(41 * TIME.SECOND,
                ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).assumedNextPhaseSwitchTime);

        subscriptions = simulateStep.execute(traci.getTraciConnection(), 42 * TIME.SECOND);
        assertEquals(3, ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).currentPhaseIndex);
        assertEquals(45 * TIME.SECOND,
                ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).assumedNextPhaseSwitchTime);

        subscriptions = simulateStep.execute(traci.getTraciConnection(), 50 * TIME.SECOND);
        assertEquals(4, ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).currentPhaseIndex);
        assertEquals(76 * TIME.SECOND,
                ((TrafficLightSubscriptionResult) Iterables.getOnlyElement(subscriptions)).assumedNextPhaseSwitchTime);
    }

}