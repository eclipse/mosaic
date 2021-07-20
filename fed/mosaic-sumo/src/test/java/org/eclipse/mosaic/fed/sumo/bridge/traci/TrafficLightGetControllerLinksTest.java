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
import static org.junit.Assert.assertNotNull;

import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class TrafficLightGetControllerLinksTest extends AbstractTraciCommandTest {

    @Override
    public void simulateBefore() throws Exception {
        simulateStep.execute(traci.getTraciConnection(), 34 * TIME.SECOND);
    }

    @Test
    public void execute() throws Exception {
        // RUN
        List<TrafficLightGetControlledLinks.TrafficLightControlledLink> controlledLinks =
                new TrafficLightGetControlledLinks().execute(traci.getTraciConnection(), "2");

        // ASSERT
        assertEquals(11, controlledLinks.size());

        for (TrafficLightGetControlledLinks.TrafficLightControlledLink controlledLink : controlledLinks) {
            assertNotNull(controlledLink.getIncoming());
            assertNotNull(controlledLink.getOutgoing());
        }
    }

    @Test(expected = CommandException.class)
    public void execute_noSuchTrafficLight() throws Exception {
        // RUN
        new TrafficLightGetControlledLinks().execute(traci.getTraciConnection(), "3");
    }

}