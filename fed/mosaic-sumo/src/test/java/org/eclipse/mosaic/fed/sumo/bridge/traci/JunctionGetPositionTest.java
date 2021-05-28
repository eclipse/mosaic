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

import org.eclipse.mosaic.fed.sumo.junit.SumoRunner;
import org.eclipse.mosaic.lib.util.objects.Position;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SumoRunner.class)
public class JunctionGetPositionTest extends AbstractTraciCommandTest {

    @Test
    public void execute() throws Exception {
        // RUN
        Position position = new JunctionGetPosition().execute(traci.getTraciConnection(), "3");

        // ASSERT
        assertNotNull(position);
        assertEquals(52.5200, position.getGeographicPosition().getLatitude(), 0.0005);
        assertEquals(13.3100, position.getGeographicPosition().getLongitude(), 0.0005);

    }

}