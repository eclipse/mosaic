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

import org.eclipse.mosaic.fed.sumo.traci.junit.SumoRunner;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(SumoRunner.class)
public class RouteGetIdsTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        // RUN
        List<String> routeIds = new RouteGetIds().execute(traci.getTraciConnection());

        // ASSERT
        assertEquals(2, routeIds.size());
        assertEquals("0, 1", StringUtils.join(routeIds, ", "));
    }

}