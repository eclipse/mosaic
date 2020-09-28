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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class VehicleGetParameterTest extends AbstractTraciCommandTest {

    @Test
    public void getParameter() throws Exception {
        simulateStep.execute(traci.getTraciConnection(), 10 * TIME.SECOND);

        String result = new VehicleGetParameter().execute(traci.getTraciConnection(), "1", "laneChangeModel.lcAssertive");

        assertTrue(NumberUtils.isCreatable(result));
        assertEquals(1d, Double.parseDouble(result), 0.00001);
    }

}