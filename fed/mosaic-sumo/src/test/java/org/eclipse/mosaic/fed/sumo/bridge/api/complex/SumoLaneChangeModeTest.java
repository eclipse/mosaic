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

package org.eclipse.mosaic.fed.sumo.bridge.api.complex;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;
import org.eclipse.mosaic.lib.enums.LaneChangeMode;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class SumoLaneChangeModeTest {

    @Test
    public void testGetAsInteger() {
        SumoLaneChangeMode laneChangeMode = new SumoLaneChangeMode();
        assertEquals(0, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setStrategicChanges(true, false);
        assertEquals(1, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setCooperativeChanges(true, false);
        assertEquals(4, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setSpeedChanges(true, false);
        assertEquals(16, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setRightDriveChanges(true, false);
        assertEquals(64, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setRespectOtherDrivers(SumoLaneChangeMode.AVOID_COLLISIONS_WITH_OTHER_DRIVERS);
        assertEquals(256, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode()
                .setStrategicChanges(true, false)
                .setCooperativeChanges(true, false)
                .setSpeedChanges(true, false)
                .setRightDriveChanges(true, false);
        assertEquals(85, laneChangeMode.getAsInteger());

        laneChangeMode.setRespectOtherDrivers(SumoLaneChangeMode.AVOID_COLLISIONS_WITH_OTHER_DRIVERS);
        assertEquals(341, laneChangeMode.getAsInteger());
    }

    @Test
    public void testGetAsInteger_overrideTraci() {
        SumoLaneChangeMode laneChangeMode;
        assertEquals(0, new SumoLaneChangeMode().getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setStrategicChanges(true, true);
        assertEquals(2, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setCooperativeChanges(true, true);
        assertEquals(8, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setSpeedChanges(true, true);
        assertEquals(32, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setRightDriveChanges(true, true);
        assertEquals(128, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode().setRespectOtherDrivers(SumoLaneChangeMode.AVOID_COLLISIONS_WITH_OTHER_DRIVERS);
        assertEquals(256, laneChangeMode.getAsInteger());

        laneChangeMode = new SumoLaneChangeMode()
                .setStrategicChanges(true, true)
                .setCooperativeChanges(true, true)
                .setSpeedChanges(true, true)
                .setRightDriveChanges(true, true);
        assertEquals(170, laneChangeMode.getAsInteger());

        laneChangeMode.setRespectOtherDrivers(SumoLaneChangeMode.AVOID_COLLISIONS_WITH_OTHER_DRIVERS);
        assertEquals(426, laneChangeMode.getAsInteger());
    }

    @Test
    public void testTranslateFromEnum() {
        assertEquals(1621, SumoLaneChangeMode.translateFromEnum(LaneChangeMode.DEFAULT).getAsInteger());
        assertEquals(512, SumoLaneChangeMode.translateFromEnum(LaneChangeMode.OFF).getAsInteger());
        assertEquals(513, SumoLaneChangeMode.translateFromEnum(LaneChangeMode.FOLLOW_ROUTE).getAsInteger());
        assertEquals(1041, SumoLaneChangeMode.translateFromEnum(LaneChangeMode.AGGRESSIVE).getAsInteger());
        assertEquals(1621, SumoLaneChangeMode.translateFromEnum(LaneChangeMode.COOPERATIVE).getAsInteger());
        assertEquals(1877, SumoLaneChangeMode.translateFromEnum(LaneChangeMode.CAUTIOUS).getAsInteger());
        assertEquals(1621, SumoLaneChangeMode.translateFromEnum(LaneChangeMode.PASSIVE).getAsInteger());
    }

}
