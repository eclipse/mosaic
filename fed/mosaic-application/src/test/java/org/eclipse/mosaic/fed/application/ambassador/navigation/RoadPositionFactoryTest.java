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

package org.eclipse.mosaic.fed.application.ambassador.navigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.road.SimpleRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class RoadPositionFactoryTest {

    private CentralNavigationComponent cncMock = mock(CentralNavigationComponent.class);
    private VehicleRoute routeMock = mock(VehicleRoute.class);

    @Rule
    @InjectMocks
    public SimulationKernelRule kernelRule = new SimulationKernelRule(null, null, cncMock);

    @Before
    public void setup() {
        Mockito.when(
                cncMock.refineRoadPosition(isA(IRoadPosition.class))).then((Answer<IRoadPosition>) invocation -> invocation.getArgument(0)
        );
        Mockito.when(cncMock.getLengthOfConnection("1_2")).thenReturn(53.83d);
        Mockito.when(cncMock.getLengthOfConnection("2_3")).thenReturn(282d);
        Mockito.when(cncMock.getLengthOfConnection("3_4")).thenReturn(16.35d);
        Mockito.when(cncMock.getLengthOfConnection("4_5")).thenReturn(78.17d);
        Mockito.when(cncMock.getLengthOfConnection("5_6")).thenReturn(39.91d);
        Mockito.when(cncMock.getLengthOfConnection("6_7")).thenReturn(43.52d);

        Mockito.when(routeMock.getConnectionIds()).thenReturn(Lists.newArrayList("1_2", "2_3", "3_4", "4_5", "5_6", "6_7"));
    }

    @Test
    public void createFromSumoEdge_validEdgeId() {
        IRoadPosition roadPosition = RoadPositionFactory.createFromSumoEdge("1_2", 0, 10d);

        // ASSERT
        assertEquals("1_2", roadPosition.getConnection().getId());
        assertEquals(0, roadPosition.getLaneIndex());
        assertEquals(10d, roadPosition.getOffset(), 0.0001d);

        verify(cncMock).refineRoadPosition(isA(IRoadPosition.class));
    }

    @Test
    public void createAlongRoute_onSameEdgeAsVehicle_firstEdgeOfRoute() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("1_2", 0, 10d, 0d), routeMock, 0, 23d);

        // ASSERT
        assertRoadPosition("1_2", 33d, roadPosition);
    }

    @Test
    public void createAlongRoute_onSameEdgeAsVehicle_thirdEdgeOfRoute() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("3_4", 0, 1d, 0d), routeMock, 0, 10d);

        // ASSERT
        assertRoadPosition("3_4", 11d, roadPosition);
    }

    @Test
    public void createAlongRoute_onNextEdge() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("3_4", 0, 10d, 0d), routeMock, 0, 60d);

        // ASSERT
        assertRoadPosition("4_5", 53.65, roadPosition);
    }

    @Test
    public void createAlongRoute_farAwayOnLastEdge() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("2_3", 0, 12d, 0d), routeMock, 0, 440d);

        // ASSERT
        assertRoadPosition("6_7", 35.61, roadPosition);
    }

    @Test
    public void createAlongRoute_exceedingRouteLength_returnLastEdge() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("2_3", 0, 12d, 0d), routeMock, 0, 1440d);

        // ASSERT
        assertRoadPosition("6_7", 43.52, roadPosition);
    }


    private void assertRoadPosition(String expectedConnection, double expectedLaneOffset, IRoadPosition roadPosition) {
        assertNotNull("Road position is not null", roadPosition);
        assertEquals(expectedConnection, roadPosition.getConnection().getId());
        assertEquals(expectedLaneOffset, roadPosition.getOffset(), 0.1d);

        verify(cncMock).refineRoadPosition(isA(IRoadPosition.class));
    }

}
