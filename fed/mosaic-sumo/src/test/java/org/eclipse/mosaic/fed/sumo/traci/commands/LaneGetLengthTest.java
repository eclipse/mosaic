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

import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class LaneGetLengthTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        String edgeId = "2_6_5_6";
        int laneIndex = 0;


        // RUN
        double length = new LaneGetLength().execute(traci.getTraciConnection(), edgeId, laneIndex);

        // ASSERT
        assertEquals(1106.3, length, 0.1);
    }

    @Test(expected = TraciCommandException.class)
    public void executeUnknownLane() throws Exception {
        String edgeId = "2_6_5_6";
        int laneIndex = 2;

        // RUN
        new LaneGetLength().execute(traci.getTraciConnection(), edgeId, laneIndex);
    }

}