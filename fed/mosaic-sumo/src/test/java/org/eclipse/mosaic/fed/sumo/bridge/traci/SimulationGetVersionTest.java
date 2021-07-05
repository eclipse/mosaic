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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.fed.sumo.bridge.SumoVersion;
import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class SimulationGetVersionTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        // RUN
        SimulationGetVersion.CurrentVersion version = new SimulationGetVersion().execute(traci.getTraciConnection());

        // ASSERT
        assertNotNull(version);
    }

    @Test
    public void supportedVersion() throws Exception {
        // RUN
        SimulationGetVersion.CurrentVersion version = new SimulationGetVersion().execute(traci.getTraciConnection());

        // ASSERT
        assertTrue("New SUMO Version is not yet supported. Please fix.", version.apiVersion <= SumoVersion.HIGHEST.getApiVersion());
        assertTrue("Installed SUMO Version is not supported. Please update.", version.apiVersion >= SumoVersion.LOWEST.getApiVersion());
    }


}