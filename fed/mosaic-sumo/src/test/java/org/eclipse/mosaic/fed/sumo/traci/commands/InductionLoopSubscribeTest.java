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
import static org.junit.Assert.assertNotEquals;

import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.complex.AbstractSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.complex.InductionLoopSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class InductionLoopSubscribeTest extends AbstractTraciCommandTest {
    @Test
    public void readSubscription() throws TraciCommandException, InternalFederateException {
        // PREPARE
        new InductionLoopSubscribe().execute(traci.getTraciConnection(), "induction_loop_1", 0, 40 * TIME.SECOND);

        // RUN
        List<AbstractSubscriptionResult> subscriptions = Lists.newArrayList();
        int vehiclesPassed = 0;
        // execute 30 simulation steps
        for (long i = 0; i < 30; i++) {
            subscriptions = simulateStep.execute(traci.getTraciConnection(), i * TIME.SECOND);
            InductionLoopSubscriptionResult result = ((InductionLoopSubscriptionResult) Iterables.getOnlyElement(subscriptions));
            if (result.meanSpeed != -1.0d) {
                assertNotEquals(0.0, result.meanSpeed); // assert that occupancy is set, when vehicle passes
                assertEquals(1, result.vehiclesOnInductionLoop.size()); // assert that one vehicle on induction loop
                vehiclesPassed++;
            }
        }

        // ASSERT
        assertEquals(1, subscriptions.size());
        assertEquals("induction_loop_1", Iterables.getOnlyElement(subscriptions).id);
        // 2 vehicles should have passed the induction loop
        assertEquals(2, vehiclesPassed);
    }
}