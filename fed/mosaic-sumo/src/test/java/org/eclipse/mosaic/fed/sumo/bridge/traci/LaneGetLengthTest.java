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
import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class LaneGetLengthTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        String laneId = "2_6_5_0";
        // RUN
        double length = new LaneGetLength().execute(traci.getTraciConnection(), laneId);

        // ASSERT
        assertEquals(1106.4, length, 0.1);
    }

    @Test(expected = CommandException.class)
    public void executeUnknownLane() throws Exception {
        String laneId = "2_6_5_2";

        // RUN
        new LaneGetLength().execute(traci.getTraciConnection(), laneId);
    }

}