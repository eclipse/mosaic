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
import org.eclipse.mosaic.lib.enums.SpeedMode;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class SumoSpeedModeTest {

    @Test
    public void testGetAsInteger() {
        SumoSpeedMode speedMode = new SumoSpeedMode();
        assertEquals(0, speedMode.getAsInteger());

        speedMode = new SumoSpeedMode().setRegardSafeSpeed(true);
        assertEquals(1, speedMode.getAsInteger());

        speedMode = new SumoSpeedMode().setRegardMaximumAcceleration(true);
        assertEquals(2, speedMode.getAsInteger());

        speedMode = new SumoSpeedMode().setRegardMaximumDeceleration(true);
        assertEquals(4, speedMode.getAsInteger());

        speedMode = new SumoSpeedMode().setRegardRightOfWay(true);
        assertEquals(8, speedMode.getAsInteger());

        speedMode = new SumoSpeedMode().setBrakeHardToAvoidRedLight(true);
        assertEquals(16, speedMode.getAsInteger());

        speedMode = new SumoSpeedMode()
                .setRegardSafeSpeed(true)
                .setRegardMaximumAcceleration(true)
                .setRegardMaximumDeceleration(true)
                .setBrakeHardToAvoidRedLight(true);
        assertEquals(23, speedMode.getAsInteger());

        speedMode.setRegardRightOfWay(true);
        assertEquals(31, speedMode.getAsInteger());
    }

    @Test
    public void testTranslateFromEnum() {
        assertEquals(31, SumoSpeedMode.translateFromEnum(SpeedMode.DEFAULT).getAsInteger());
        assertEquals(6, SumoSpeedMode.translateFromEnum(SpeedMode.AGGRESSIVE).getAsInteger());
        assertEquals(15, SumoSpeedMode.translateFromEnum(SpeedMode.NORMAL).getAsInteger());
        assertEquals(31, SumoSpeedMode.translateFromEnum(SpeedMode.CAUTIOUS).getAsInteger());
    }

}
