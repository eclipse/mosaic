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

import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.junit.SinceTraci;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class TrafficLightSetRemainingPhaseDurationTest extends AbstractTraciCommandTest {

    @Override
    public void simulateBefore() throws Exception {
        simulateStep.execute(traci.getTraciConnection(), 34 * TIME.SECOND);
    }

    @SinceTraci(TraciVersion.API_19)
    @Test
    public void execute() throws Exception {

        new TrafficLightSetRemainingPhaseDuration().execute(traci.getTraciConnection(), "2", 5d);
    }


}