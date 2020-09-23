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

package org.eclipse.mosaic.fed.sumo.ambassador;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SumoAmbassadorTimeStepAdjustmentTest {

    private final static long sumoTimeStep = 10000;

    @Test
    public void testEqualTimeStep() {
        // Requested time is the same as the sumo timestep. Shouldn't be changed
        assertEquals(sumoTimeStep, AbstractSumoAmbassador.adjustToSumoTimeStep(10000, sumoTimeStep));
    }

    @Test
    public void testRoundDown() {
        // Just 2 over, should round down
        assertEquals(sumoTimeStep, AbstractSumoAmbassador.adjustToSumoTimeStep(10002, sumoTimeStep));
    }

    @Test
    public void testRoundUp() {
        // Closer to the next higher value, should round up
        assertEquals(sumoTimeStep * 2, AbstractSumoAmbassador.adjustToSumoTimeStep(19999, sumoTimeStep));
    }

    @Test
    public void testTooSmall() {
        // Requested event time is smaller than the half of the sumo step (closer to previous step value - 0 in this case),
        // so it is being reduced to 0 and then brought to sumo time step value
        assertEquals(sumoTimeStep, AbstractSumoAmbassador.adjustToSumoTimeStep(sumoTimeStep / 3, sumoTimeStep));
    }
}
