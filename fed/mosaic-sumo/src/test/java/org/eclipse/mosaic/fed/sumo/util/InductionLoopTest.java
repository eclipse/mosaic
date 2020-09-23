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

package org.eclipse.mosaic.fed.sumo.util;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;

public class InductionLoopTest {

    @Test
    public void trafficFlow_noUpdates_zero() {
        // PREPARE
        InductionLoop laneDetector00Lane0 = new InductionLoop("segment00_lane0_exit", 60 * TIME.SECOND);

        // RUN
        double flow = laneDetector00Lane0.getTrafficFlowVehPerHour(10 * TIME.SECOND);

        // ASSERT
        assertEquals(0, flow, 0.1d);
    }


    @Test
    public void trafficFlow_threeUpdates() {
        // PREPARE
        InductionLoop laneDetector00Lane0 = new InductionLoop("segment00_lane0_exit", 60 * TIME.SECOND);
        laneDetector00Lane0.update(0, 3);
        laneDetector00Lane0.update(2 * TIME.SECOND, 0);
        laneDetector00Lane0.update(4 * TIME.SECOND, 1);
        laneDetector00Lane0.update(8 * TIME.SECOND, 2);

        // RUN
        double flow = laneDetector00Lane0.getTrafficFlowVehPerHour(10 * TIME.SECOND);

        // ASSERT
        assertEquals(2160, flow, 0.1d);
    }

}