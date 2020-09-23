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

package org.eclipse.mosaic.fed.application.ambassador.navigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.road.SimpleRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class RoadPositionFactoryTest {

    @Mock
    private CentralNavigationComponent cncMock = mock(CentralNavigationComponent.class);
    @Mock
    private VehicleRoute routeMock = mock(VehicleRoute.class);

    @Rule
    @InjectMocks
    public SimulationKernelRule kernelRule = new SimulationKernelRule(null, null, cncMock);

    @Before
    public void setup() {
        Mockito.when(
                cncMock.refineRoadPosition(isA(IRoadPosition.class))).then((Answer<IRoadPosition>) invocation -> invocation.getArgument(0)
        );
        Mockito.when(cncMock.getPositionOfNode("1")).thenReturn(GeoPoint.latLon(52.46365064121868, 13.369996547698975));
        Mockito.when(cncMock.getPositionOfNode("2")).thenReturn(GeoPoint.latLon(52.463166922742516, 13.369985818862915)); // 1->2: 53.83m
        Mockito.when(cncMock.getPositionOfNode("3")).thenReturn(GeoPoint.latLon(52.46063711926438, 13.369739055633545));  // 2->3: 282m
        Mockito.when(cncMock.getPositionOfNode("4")).thenReturn(GeoPoint.latLon(52.4604998389485, 13.369653224945068));   // 3->4: 16.35m
        Mockito.when(cncMock.getPositionOfNode("5")).thenReturn(GeoPoint.latLon(52.46026450026822, 13.368569612503052));  // 4->5: 78.17m
        Mockito.when(cncMock.getPositionOfNode("6")).thenReturn(GeoPoint.latLon(52.459983399640414, 13.368204832077026)); // 5->6: 39.91m
        Mockito.when(cncMock.getPositionOfNode("7")).thenReturn(GeoPoint.latLon(52.45961731241121, 13.367979526519775));  // 6->7: 43.52m

        Mockito.when(routeMock.getNodeIdList()).thenReturn(Lists.newArrayList("1", "2", "3", "4", "5", "6", "7"));
    }

    @Test
    public void createFromSumoEdge_validEdgeId() {
        IRoadPosition roadPosition = RoadPositionFactory.createFromSumoEdge("1_2_3_4", 0, 10d);

        // ASSERT
        assertEquals("1", roadPosition.getConnection().getWay().getId());
        assertEquals("2", roadPosition.getConnection().getStartNode().getId());
        assertEquals("3", roadPosition.getConnection().getEndNode().getId());
        assertEquals("4", roadPosition.getPreviousNode().getId());
        assertEquals(0, roadPosition.getLaneIndex());
        assertEquals(10d, roadPosition.getOffset(), 0.0001d);

        verify(cncMock).refineRoadPosition(isA(IRoadPosition.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFromSumoEdge_invalidEdgeId() {
        RoadPositionFactory.createFromSumoEdge("1_2_3", 0, 10d);
    }

    @Test
    public void createAlongRoute_onSameEdgeAsVehicle_firstEdgeOfRoute() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("1", "2", 0, 10d), routeMock, 0, 23d);

        // ASSERT
        assertRoadPosition("1", "2", 33d, roadPosition);
    }

    @Test
    public void createAlongRoute_onSameEdgeAsVehicle_thirdEdgeOfRoute() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("3", "4", 0, 1d), routeMock, 0, 10d);

        // ASSERT
        assertRoadPosition("3", "4", 11d, roadPosition);
    }

    @Test
    public void createAlongRoute_onNextEdge() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("3", "4", 0, 10d), routeMock, 0, 60d);

        // ASSERT
        assertRoadPosition("4", "5", 53.65, roadPosition);
    }

    @Test
    public void createAlongRoute_farAwayOnLastEdge() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("2", "3", 0, 12d), routeMock, 0, 440d);

        // ASSERT
        assertRoadPosition("6", "7", 35.61, roadPosition);
    }

    @Test
    public void createAlongRoute_exceedingRouteLength_returnLastEdge() {
        // RUN
        IRoadPosition roadPosition = RoadPositionFactory.createAlongRoute(new SimpleRoadPosition("2", "3", 0, 12d), routeMock, 0, 1440d);

        // ASSERT
        assertRoadPosition("6", "7", 43.52, roadPosition);
    }


    private void assertRoadPosition(String expectedNodeA, String expectedNodeB, double expectedLaneOffset, IRoadPosition roadPosition) {
        assertNotNull("Road position is not null", roadPosition);
        assertEquals(expectedNodeA, roadPosition.getPreviousNode().getId());
        assertEquals(expectedNodeB, roadPosition.getUpcomingNode().getId());
        assertEquals(expectedLaneOffset, roadPosition.getOffset(), 0.01d);

        verify(cncMock).refineRoadPosition(isA(IRoadPosition.class));
    }

}
