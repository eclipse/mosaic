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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleUnit;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.objects.road.IConnection;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.routing.IllegalRouteException;
import org.eclipse.mosaic.lib.routing.RoutingCostFunction;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingRequest;
import org.eclipse.mosaic.lib.routing.RoutingResponse;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Test suite for {@link NavigationModule}.
 */
public class NavigationModuleTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);


    private final EventManager eventMngMock = mock(EventManager.class);
    private final CentralNavigationComponent cncMock = mock(CentralNavigationComponent.class);

    private final VehicleData vehicleDataMock = mock(VehicleData.class);


    @InjectMocks
    @Rule
    public SimulationKernelRule kernelRule = new SimulationKernelRule(eventMngMock, null, cncMock);

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    private NavigationModule navigationModule;

    private RoutingRequest findRouteRequest;

    @Before
    public void setup() {
        VehicleUnit vehicle = new VehicleUnit("veh_0", mock(VehicleType.class), null);

        navigationModule = Mockito.spy(new NavigationModule(vehicle));
        navigationModule.setVehicleData(vehicleDataMock);

        when(vehicleDataMock.getHeading()).thenReturn(45.0d);
        when(vehicleDataMock.getPosition()).thenReturn(GeoPoint.latLon(10, 10));
        when(vehicleDataMock.getRoadPosition()).thenReturn(mock(IRoadPosition.class));
        when(vehicleDataMock.getRoadPosition().getConnection()).thenReturn(mock(IConnection.class));
        when(navigationModule.getRoadPosition().getConnectionId()).thenReturn("1_1_2");
        doAnswer((Answer<RoutingResponse>) invocation -> {
            findRouteRequest = invocation.getArgument(0);
            return mock(RoutingResponse.class);
        }).when(cncMock).findRoutes(isA(RoutingRequest.class));
        HashMap<String, VehicleRoute> routeMap = new HashMap<>();
        routeMap.put("123", new VehicleRoute("123", Collections.singletonList("edgeID"), Collections.singletonList("nodeID"), 0.0));
        when(cncMock.getRouteMap()).thenReturn(routeMap);
        when(cncMock.getTargetPositionOfRoute(ArgumentMatchers.anyString())).thenReturn(GeoPoint.latLon(30, 40));

    }

    @Test
    public void calculateRoutes_routeRequestBuiltCorrectly() {
        // PREPARE
        final RoutingParameters params = new RoutingParameters().alternativeRoutes(2).costFunction(RoutingCostFunction.Shortest);
        final RoutingPosition target = new RoutingPosition(GeoPoint.latLon(20, 20));

        // RUN
        navigationModule.calculateRoutes(target, params);

        // ASSERT
        calculateRoutes_routeRequestBuiltCorrectly_helper(params);
        assertEquals(target, findRouteRequest.getTarget());
    }

    @Test
    public void calculateRoutes_routeRequestBuiltCorrectly_usingGeoPoint() {
        // PREPARE
        final RoutingParameters params = new RoutingParameters().alternativeRoutes(2).costFunction(RoutingCostFunction.Shortest);
        final GeoPoint target = GeoPoint.lonLat(20, 20);

        // RUN
        navigationModule.calculateRoutes(target, params);

        // ASSERT
        calculateRoutes_routeRequestBuiltCorrectly_helper(params);
        assertEquals(target, findRouteRequest.getTarget().getPosition());

    }

    private void calculateRoutes_routeRequestBuiltCorrectly_helper(RoutingParameters params) {
        verify(cncMock, times(1)).findRoutes(isA(RoutingRequest.class));
        assertNotNull(findRouteRequest);
        assertEquals(vehicleDataMock.getPosition(), findRouteRequest.getSource().getPosition());
        assertEquals(vehicleDataMock.getHeading(), findRouteRequest.getSource().getHeading(), 0.01d);
        when(navigationModule.getRoadPosition().getConnection().getId()).thenReturn(findRouteRequest.getSource().getConnectionId());
        assertEquals(params, findRouteRequest.getRoutingParameters());
    }

    @Test
    public void switchRoute_callMethodInCnc_differentRoute_resultTrue() throws IllegalRouteException {
        // PREPARE
        final CandidateRoute candidateRoute = new CandidateRoute(Arrays.asList("1", "2", "3"), 0, 0);
        VehicleRoute routeReturnMock = mock(VehicleRoute.class);
        when(routeReturnMock.getId()).thenReturn("0");
        VehicleRoute currentRouteMock = mock(VehicleRoute.class);
        when(currentRouteMock.getId()).thenReturn("1");
        navigationModule.setCurrentRoute(currentRouteMock);
        when(cncMock.switchRoute(isA(VehicleData.class), isA(CandidateRoute.class), any(), anyLong())).thenReturn(routeReturnMock);

        // RUN
        boolean result = navigationModule.switchRoute(candidateRoute);

        // ASSERT
        assertTrue(result);
        verify(cncMock, times(1)).switchRoute(same(vehicleDataMock), same(candidateRoute), any(), anyLong());
    }

    @Test
    public void switchRoute_callMethodInCnc_sameRoute_resultFalse() throws IllegalRouteException {
        // PREPARE
        final CandidateRoute candidateRoute = new CandidateRoute(Arrays.asList("1", "2", "3"), 0, 0);
        VehicleRoute routeReturnMock = mock(VehicleRoute.class);
        when(routeReturnMock.getId()).thenReturn("0");
        VehicleRoute currentRouteMock = mock(VehicleRoute.class);
        when(currentRouteMock.getId()).thenReturn("0");
        navigationModule.setCurrentRoute(currentRouteMock);
        when(cncMock.switchRoute(isA(VehicleData.class), isA(CandidateRoute.class), any(), anyLong())).thenReturn(routeReturnMock);

        // RUN
        boolean result = navigationModule.switchRoute(candidateRoute);

        // ASSERT
        assertFalse(result);
        verify(cncMock, times(1)).switchRoute(same(vehicleDataMock), same(candidateRoute), any(), anyLong());
    }

    /**
     * Asserts that method returns collection of {@link CandidateRoute} if valid route exists using RoutingPosition as input.
     */
    @Test
    public void testRetrieveAllValidExistingRoutesToTargetValidRoute_usingRoutingPosition() {
        // PREPARE
        testRetrieveAllValidExistingRoutesToTargetValidRoute_prepare();

        // RUN
        Collection<CandidateRoute> coll = navigationModule.retrieveAllValidRoutesToTarget(
                new RoutingPosition(
                        GeoPoint.latLon(30, 40),
                        0.0,
                        "connectionID"
                )
        );

        // ASSERT
        testRetrieveAllValidExistingRoutesToTargetValidRoute_assert(coll);
    }

    /**
     * Asserts that method returns collection of {@link CandidateRoute} if valid route exists using GeoPoint as input.
     */
    @Test
    public void testRetrieveAllValidExistingRoutesToTargetValidRoute_usingGeoPoint() {
        // PREPARE
        testRetrieveAllValidExistingRoutesToTargetValidRoute_prepare();

        // RUN
        Collection<CandidateRoute> coll = navigationModule.retrieveAllValidRoutesToTarget(GeoPoint.lonLat(40, 30));

        // ASSERT
        testRetrieveAllValidExistingRoutesToTargetValidRoute_assert(coll);
    }

    private void testRetrieveAllValidExistingRoutesToTargetValidRoute_prepare() {
        // call to targetQuery method can return true for this test case, because the method itself is tested below.
        Mockito.doReturn(true).when(navigationModule).targetQuery(
                any(RoutingPosition.class),
                any(VehicleRoute.class),
                any(GeoPoint.class)
        );
        // call to onRouteQuery method can return true for this test case, because the method itself is tested below.
        Mockito.doReturn(true).when(navigationModule).onRouteQuery(any(VehicleRoute.class));

    }

    private void testRetrieveAllValidExistingRoutesToTargetValidRoute_assert(Collection<CandidateRoute> coll) {
        assertEquals(1, coll.size());
        assertTrue(coll.iterator().next().getConnectionIds().contains("edgeID"));

    }

    /**
     * Asserts that method returns collection of {@link CandidateRoute} if no valid route exists using RoutingPosition as input.
     */
    @Test
    public void testRetrieveAllValidExistingRoutesToNoTargetValidRoute_usingRoutingPosition() {
        // PREPARE
        testRetrieveAllValidExistingRoutesToNoTargetValidRoute_prepare();

        // RUN
        Collection<CandidateRoute> coll = navigationModule.retrieveAllValidRoutesToTarget(new RoutingPosition(
                GeoPoint.latLon(30, 40),
                0.0,
                "connectionID"
        ));

        // ASSERT
        testRetrieveAllValidExistingRoutesToNoTargetValidRoute_assert(coll);
    }

    /**
     * Asserts that method returns collection of {@link CandidateRoute} if valid no route exists using GeoPoint as input.
     */
    @Test
    public void testRetrieveAllValidExistingRoutesToNoTargetValidRoute_usingGeoPoint() {
        // PREPARE
        testRetrieveAllValidExistingRoutesToNoTargetValidRoute_prepare();

        // RUN
        Collection<CandidateRoute> coll = navigationModule.retrieveAllValidRoutesToTarget(GeoPoint.lonLat(40, 30));

        // ASSERT
        testRetrieveAllValidExistingRoutesToNoTargetValidRoute_assert(coll);
    }

    private void testRetrieveAllValidExistingRoutesToNoTargetValidRoute_prepare() {
        // call to targetQuery method can return true for this test case, because the method itself is tested below.
        Mockito.doReturn(true).when(navigationModule).targetQuery(
                any(RoutingPosition.class),
                any(VehicleRoute.class),
                any(GeoPoint.class)
        );
        // call to onRouteQuery method can return true for this test case, because the method itself is tested below.
        Mockito.doReturn(false).when(navigationModule).onRouteQuery(any(VehicleRoute.class));
    }

    private void testRetrieveAllValidExistingRoutesToNoTargetValidRoute_assert(Collection<CandidateRoute> coll) {
        assertTrue(coll.isEmpty());
    }

    /**
     * Asserts that targetQuery method returns true iff route's lastEdgeId starts with targetPosition's Id.
     */
    @Test
    public void testTargetQueryEdgeIdStartsWithConnectionId() {
        // call to targetQuery method can return true for this test case, because the method itself is tested below.
        Mockito.doReturn(true).when(navigationModule).targetQuery(
                any(RoutingPosition.class),
                any(VehicleRoute.class),
                any(GeoPoint.class)
        );
        // call to onRouteQuery method can return true for this test case, because the method itself is tested below.
        Mockito.doReturn(false).when(navigationModule).onRouteQuery(any(VehicleRoute.class));
    }

    /**
     * Asserts that targetQuery method returns true iff the two geoPoints are closer to
     * each other than specified in POSITION_DIFFERENCE_THRESHOLD constant.
     */
    @Test
    public void testTargetQueryGeoPointsClose() {
        RoutingPosition targetPosition =
                new RoutingPosition(GeoPoint.latLon(30, 40), 0.0, "Some_Not_Equal_Edge_ID");
        VehicleRoute route = new VehicleRoute("123", Collections.singletonList("Edge_ID"), Collections.singletonList("Node_ID"), 0.0);
        assertTrue(navigationModule.targetQuery(targetPosition, route, GeoPoint.latLon(30, 40)));
    }

    /**
     * Asserts null safeness of targetQuery method for targetPosition.connectionID and targetPosition.nodeID.
     */
    @Test
    public void testTargetQueryNullSafe() {
        RoutingPosition targetPosition = new RoutingPosition(GeoPoint.latLon(30, 40), 0.0, null);
        VehicleRoute route = new VehicleRoute("123", Collections.singletonList("Edge_ID"), Collections.singletonList("Node_ID"), 0.0);
        navigationModule.targetQuery(targetPosition, route, GeoPoint.latLon(30, 40));
    }

    /**
     * Asserts that targetQuery method returns false if neither of its if statements holds.
     */
    @Test
    public void testTargetQueryFalse() {
        RoutingPosition targetPosition =
                new RoutingPosition(GeoPoint.latLon(30, 40), 0.0, "Some_Not_Equal_Edge_ID");
        VehicleRoute route = new VehicleRoute("123", Collections.singletonList("Edge_ID"), Collections.singletonList("Node_ID"), 0.0);
        assertFalse(navigationModule.targetQuery(targetPosition, route, GeoPoint.latLon(90, 90)));
    }

    /**
     * Asserts that onRouteQuery method returns true if currentEdgeID is in route's edgeIdList.
     */
    @Test
    public void testOnRouteQueryTrue() {
        VehicleRoute route =
                new VehicleRoute("123", Collections.singletonList("connectionID"), Collections.singletonList("Node_ID"), 0.0);
        when(navigationModule.getRoadPosition().getConnectionId()).thenReturn("connectionID");
        assertTrue(navigationModule.onRouteQuery(route));
    }

    /**
     * Asserts that onRouteQuery method returns false if currentEdgeID is not in route's edgeIdList.
     */
    @Test
    public void testOnRouteQueryFalse() {
        VehicleRoute route =
                new VehicleRoute("123", Collections.singletonList("differentEdgeID"), Collections.singletonList("Node_ID"), 0.0);
        when(navigationModule.getRoadPosition().getConnectionId()).thenReturn("connectionID");
        assertFalse(navigationModule.onRouteQuery(route));
    }

}
