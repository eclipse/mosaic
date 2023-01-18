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

import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class RouteAddTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        // RUN
        new RouteAdd().execute(traci.getTraciConnection(), "3", Lists.newArrayList("1_1_2", "1_2_3", "1_3_4"));
    }

    @Test(expected = CommandException.class)
    public void executeWrongEdges() throws Exception {
        // RUN
        new RouteAdd().execute(traci.getTraciConnection(), "3", Lists.newArrayList("1_1_2_x", "1_2_3", "1_3_4"));
    }

    @Test(expected = CommandException.class)
    public void executeExistingRouteId() throws Exception {
        // RUN
        new RouteAdd().execute(traci.getTraciConnection(), "0", Lists.newArrayList("1_1_2", "1_2_3", "1_3_4"));
    }

}