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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class RouteAddTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        // RUN
        new RouteAdd().execute(traci.getTraciConnection(), "2", Lists.newArrayList("1_1_2_1", "1_2_3_2", "1_3_4_3"));
    }

    @Test(expected = TraciCommandException.class)
    public void executeWrongEdges() throws Exception {
        // RUN
        new RouteAdd().execute(traci.getTraciConnection(), "2", Lists.newArrayList("1_1_2_1_x", "1_2_3_2", "1_3_4_3"));
    }

    @Test(expected = TraciCommandException.class)
    public void executeExistingRouteId() throws Exception {
        // RUN
        new RouteAdd().execute(traci.getTraciConnection(), "0", Lists.newArrayList("1_1_2_1", "1_2_3_2", "1_3_4_3"));
    }

}