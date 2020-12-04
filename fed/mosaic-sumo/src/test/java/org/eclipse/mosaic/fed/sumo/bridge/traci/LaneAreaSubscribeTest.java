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

import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.AbstractSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.LaneAreaSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class LaneAreaSubscribeTest extends AbstractTraciCommandTest {
    @Test
    public void readSubscription() throws CommandException, InternalFederateException {
        // PREPARE
        new LaneAreaSubscribe().execute(traci.getTraciConnection(), "lane_area_1", 0, 40 * TIME.SECOND);

        // RUN
        List<AbstractSubscriptionResult> subscriptions = simulateStep.execute(traci.getTraciConnection(), 20 * TIME.SECOND);

        // ASSERT
        assertEquals(1, subscriptions.size());
        assertEquals("lane_area_1", Iterables.getOnlyElement(subscriptions).id);
        assertEquals(2, ((LaneAreaSubscriptionResult) Iterables.getOnlyElement(subscriptions)).vehicleCount);

    }
}