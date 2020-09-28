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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning.lane;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.mapping.ambassador.spawning.VehicleTypeSpawner;
import org.eclipse.mosaic.lib.enums.VehicleClass;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for {@link HighwaySpecificLaneIndexSelector}.
 */
public class HighwaySpecificLaneIndexSelectorTest {

    private VehicleTypeSpawner vTypeCar = mock(VehicleTypeSpawner.class);
    private VehicleTypeSpawner vTypeTruck = mock(VehicleTypeSpawner.class);

    @Before
    public void setup() {
        when(vTypeCar.getVehicleClass()).thenReturn(VehicleClass.Car);
        when(vTypeTruck.getVehicleClass()).thenReturn(VehicleClass.HeavyGoodsVehicle);
    }

    @Test
    public void laneSelection_012_onlyCars() {
        LaneIndexSelector selector = new HighwaySpecificLaneIndexSelector(Lists.newArrayList(0, 1, 2));

        assertEquals(0, selector.nextLane(vTypeCar));
        assertEquals(1, selector.nextLane(vTypeCar));
        assertEquals(2, selector.nextLane(vTypeCar));
        assertEquals(0, selector.nextLane(vTypeCar));
        assertEquals(1, selector.nextLane(vTypeCar));
        assertEquals(2, selector.nextLane(vTypeCar));
    }

    @Test
    public void laneSelection_012_withTrucks() {
        LaneIndexSelector selector = new HighwaySpecificLaneIndexSelector(Lists.newArrayList(0, 1, 2));

        assertEquals(0, selector.nextLane(vTypeCar));
        assertEquals(1, selector.nextLane(vTypeCar));
        assertEquals(0, selector.nextLane(vTypeTruck));
        assertEquals(2, selector.nextLane(vTypeCar));
        assertEquals(0, selector.nextLane(vTypeTruck));
        assertEquals(1, selector.nextLane(vTypeCar));
        assertEquals(2, selector.nextLane(vTypeCar));
        assertEquals(0, selector.nextLane(vTypeCar));
        assertEquals(0, selector.nextLane(vTypeTruck));
        assertEquals(1, selector.nextLane(vTypeCar));
        assertEquals(2, selector.nextLane(vTypeCar));
    }

    @Test
    public void laneSelection_312_withTrucks() {
        LaneIndexSelector selector = new HighwaySpecificLaneIndexSelector(Lists.newArrayList(3, 1, 2));

        assertEquals(3, selector.nextLane(vTypeCar));
        assertEquals(1, selector.nextLane(vTypeCar));
        assertEquals(1, selector.nextLane(vTypeTruck));
        assertEquals(2, selector.nextLane(vTypeCar));
        assertEquals(1, selector.nextLane(vTypeTruck));
        assertEquals(3, selector.nextLane(vTypeCar));
        assertEquals(1, selector.nextLane(vTypeCar));
        assertEquals(2, selector.nextLane(vTypeCar));
        assertEquals(1, selector.nextLane(vTypeTruck));
        assertEquals(3, selector.nextLane(vTypeCar));
        assertEquals(1, selector.nextLane(vTypeCar));
    }

    @Test
    public void laneSelection_2() {
        LaneIndexSelector selector = new HighwaySpecificLaneIndexSelector(Lists.newArrayList(2));

        assertEquals(2, selector.nextLane(vTypeCar));
        assertEquals(2, selector.nextLane(vTypeCar));
        assertEquals(2, selector.nextLane(vTypeTruck));
        assertEquals(2, selector.nextLane(vTypeCar));
    }

}