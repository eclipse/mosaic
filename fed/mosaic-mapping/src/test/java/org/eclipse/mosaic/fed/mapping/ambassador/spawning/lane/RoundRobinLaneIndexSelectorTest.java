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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning.lane;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.eclipse.mosaic.fed.mapping.ambassador.spawning.VehicleTypeSpawner;

import com.google.common.collect.Lists;
import org.junit.Test;

/**
 * Test suite for {@link RoundRobinLaneIndexSelector}.
 */
public class RoundRobinLaneIndexSelectorTest {

    private VehicleTypeSpawner vType = mock(VehicleTypeSpawner.class);

    @Test
    public void laneSelection_012() {
        LaneIndexSelector selector = new RoundRobinLaneIndexSelector(Lists.newArrayList(0, 1, 2));

        assertEquals(0, selector.nextLane(vType));
        assertEquals(1, selector.nextLane(vType));
        assertEquals(2, selector.nextLane(vType));
        assertEquals(0, selector.nextLane(vType));
        assertEquals(1, selector.nextLane(vType));
        assertEquals(2, selector.nextLane(vType));
    }

    @Test
    public void laneSelection_132() {
        LaneIndexSelector selector = new RoundRobinLaneIndexSelector(Lists.newArrayList(1, 3, 2));

        assertEquals(1, selector.nextLane(vType));
        assertEquals(3, selector.nextLane(vType));
        assertEquals(2, selector.nextLane(vType));
        assertEquals(1, selector.nextLane(vType));
        assertEquals(3, selector.nextLane(vType));
        assertEquals(2, selector.nextLane(vType));
    }

    @Test
    public void laneSelection_2() {
        LaneIndexSelector selector = new RoundRobinLaneIndexSelector(Lists.newArrayList(2));

        assertEquals(2, selector.nextLane(vType));
        assertEquals(2, selector.nextLane(vType));
        assertEquals(2, selector.nextLane(vType));
    }

}