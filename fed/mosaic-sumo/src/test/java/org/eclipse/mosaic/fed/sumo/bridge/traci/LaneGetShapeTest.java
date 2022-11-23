/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.util.objects.Position;

import org.junit.Test;

import java.util.List;

public class LaneGetShapeTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        String laneId = "2_6_5_0";

        // RUN
        List<Position> shape = new LaneGetShape().execute(traci.getTraciConnection(), laneId);

        // ASSERT
        assertEquals(2, shape.size());
        assertTrue(shape.get(0).getProjectedPosition().distanceTo(CartesianPoint.xy(728.70, 2205.37)) < 0.1);
        assertTrue(shape.get(1).getProjectedPosition().distanceTo(CartesianPoint.xy(703.06, 1099.24)) < 0.1);
    }

    @Test(expected = CommandException.class)
    public void executeUnknownLane() throws Exception {
        String laneId = "2_6_5_2";

        // RUN
        new LaneGetShape().execute(traci.getTraciConnection(), laneId);
    }

}
