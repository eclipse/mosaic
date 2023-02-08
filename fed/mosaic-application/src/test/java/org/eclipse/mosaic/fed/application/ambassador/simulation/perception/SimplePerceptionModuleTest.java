/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.navigation.CentralNavigationComponent;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.TrafficObjectIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.TrafficLightMap;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.TrafficLightTree;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleGrid;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleMap;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleTree;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLight;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgram;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class SimplePerceptionModuleTest {

    private final EventManager eventManagerMock = mock(EventManager.class);
    private final CentralPerceptionComponent cpcMock = mock(CentralPerceptionComponent.class);
    private final CentralNavigationComponent cncMock = mock(CentralNavigationComponent.class);

    @Rule
    public MockitoRule initRule = MockitoJUnit.rule();

    @Rule
    public GeoProjectionRule projectionRule = new GeoProjectionRule(GeoPoint.latLon(52, 13));
    @Mock
    public VehicleData egoVehicleData;

    @Rule
    public SimulationKernelRule simulationKernelRule = new SimulationKernelRule(eventManagerMock, null, cncMock, cpcMock);

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    public TrafficObjectIndex trafficObjectIndex;

    private SimplePerceptionModule simplePerceptionModule;

    @Before
    public void setup() {
        when(cpcMock.getScenarioBounds())
                .thenReturn(new CartesianRectangle(new MutableCartesianPoint(100, 90, 0), new MutableCartesianPoint(310, 115, 0)));
        SimulationKernel.SimulationKernel.setConfiguration(new CApplicationAmbassador());

        trafficObjectIndex = new TrafficObjectIndex.Builder(mock((Logger.class)))
                .withVehicleIndex(new VehicleMap())
                .withTrafficLightIndex(new TrafficLightMap())
                .build();
        // setup cpc
        when(cpcMock.getTrafficObjectIndex()).thenReturn(trafficObjectIndex);
        // setup perception module
        VehicleUnit egoVehicleUnit = spy(new VehicleUnit("veh_0", mock(VehicleType.class), null));
        doReturn(egoVehicleData).when(egoVehicleUnit).getVehicleData();
        simplePerceptionModule = spy(new SimplePerceptionModule(egoVehicleUnit, null, mock(Logger.class)));
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(90d, 200d));

        // setup ego vehicle
        when(egoVehicleData.getHeading()).thenReturn(90d);
        when(egoVehicleData.getProjectedPosition()).thenReturn(new MutableCartesianPoint(100, 100, 0));
    }

    @Test
    public void vehicleCanBePerceived_TrivialIndex() {
        setupVehicles(new MutableCartesianPoint(110, 100, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_includesDimensions_TrivialIndex() {
        setupVehicles(new MutableCartesianPoint(110, 100, 0));
        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        assertEquals(1, perceivedVehicles.size());
        VehicleObject perceivedVehicle = perceivedVehicles.get(0);
        assertEquals(5d, perceivedVehicle.getLength(), 0.01);
        assertEquals(2.5d, perceivedVehicle.getWidth(), 0.01);
        assertEquals(10d, perceivedVehicle.getHeight(), 0.01);
    }

    @Test
    public void vehicleCannotBePerceived_outOfRange_TrivialIndex() {
        setupVehicles(new MutableCartesianPoint(310, 100, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnLeftBoundVector_TrivialIndex() {
        setupVehicles(new MutableCartesianPoint(110, 110, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_OnLeftBoundVector_OppositeDirection_TrivialIndex() {
        setupVehicles(new MutableCartesianPoint(90, 90, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnRightBoundVector_TrivialIndex() {
        setupVehicles(new MutableCartesianPoint(110, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarLeft_TrivialIndex() {
        setupVehicles(new MutableCartesianPoint(105, 115, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarRight_TrivialIndex() {
        setupVehicles(new MutableCartesianPoint(105, 90, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_270viewingAngle_VehicleOnDirectionVector_TrivialIndex() {
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupVehicles(new MutableCartesianPoint(110, 100, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarLeft_270viewingAngle_TrivialIndex() {
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupVehicles(new MutableCartesianPoint(105, 115, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarRight_270viewingAngle_TrivialIndex() {
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupVehicles(new MutableCartesianPoint(105, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_QuadTree() {
        useQuadTree();
        setupVehicles(new MutableCartesianPoint(110, 100, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_includesDimensions_QuadTree() {
        useQuadTree();
        setupVehicles(new MutableCartesianPoint(110, 100, 0));
        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        assertEquals(1, perceivedVehicles.size());
        VehicleObject perceivedVehicle = perceivedVehicles.get(0);
        assertEquals(5d, perceivedVehicle.getLength(), 0.01);
        assertEquals(2.5d, perceivedVehicle.getWidth(), 0.01);
        assertEquals(10d, perceivedVehicle.getHeight(), 0.01);
    }

    @Test
    public void vehicleCannotBePerceived_outOfRange_QuadTree() {
        useQuadTree();
        setupVehicles(new MutableCartesianPoint(310, 100, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnLeftBoundVector_QuadTree() {
        useQuadTree();
        setupVehicles(new MutableCartesianPoint(110, 110, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnRightBoundVector_QuadTree() {
        useQuadTree();
        setupVehicles(new MutableCartesianPoint(110, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarLeft_QuadTree() {
        useQuadTree();
        setupVehicles(new MutableCartesianPoint(105, 115, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarRight_QuadTree() {
        useQuadTree();
        setupVehicles(new MutableCartesianPoint(105, 90, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarLeft_270viewingAngle_QuadTree() {
        useQuadTree();
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupVehicles(new MutableCartesianPoint(105, 115, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarRight_270viewingAngle_QuadTree() {
        useQuadTree();
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupVehicles(new MutableCartesianPoint(105, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_Grid() {
        useGrid();
        setupVehicles(new MutableCartesianPoint(110, 100, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_includesDimensions_Grid() {
        useQuadTree();
        setupVehicles(new MutableCartesianPoint(110, 100, 0));
        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        assertEquals(1, perceivedVehicles.size());
        VehicleObject perceivedVehicle = perceivedVehicles.get(0);
        assertEquals(5d, perceivedVehicle.getLength(), 0.01);
        assertEquals(2.5d, perceivedVehicle.getWidth(), 0.01);
        assertEquals(10d, perceivedVehicle.getHeight(), 0.01);
    }

    @Test
    public void vehicleCannotBePerceived_outOfRange_Grid() {
        useGrid();
        setupVehicles(new MutableCartesianPoint(310, 100, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnLeftBoundVector_Grid() {
        useGrid();
        setupVehicles(new MutableCartesianPoint(110, 110, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnRightBoundVector_Grid() {
        useGrid();
        setupVehicles(new MutableCartesianPoint(110, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarLeft_Grid() {
        useGrid();
        setupVehicles(new MutableCartesianPoint(105, 115, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarRight_Grid() {
        useGrid();
        setupVehicles(new MutableCartesianPoint(105, 90, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarLeft_270viewingAngle_Grid() {
        useGrid();
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupVehicles(new MutableCartesianPoint(105, 115, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarRight_270viewingAngle_Grid() {
        useGrid();
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupVehicles(new MutableCartesianPoint(105, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void trafficLightsCanBePerceived_TrivialIndex() {
        setupTrafficLights(new MutableCartesianPoint(110, 100, 0));

        assertEquals(1, simplePerceptionModule.getTrafficLightsInRange().size());
        assertEquals(TrafficLightState.GREEN, simplePerceptionModule.getTrafficLightsInRange().get(0).getTrafficLightState());
    }

    @Test
    public void trafficLightsCanBePerceived_TrafficLightTree() {
        useTlTree();
        setupTrafficLights(new MutableCartesianPoint(110, 100, 0));
        trafficObjectIndex.updateTrafficLights(mock(Map.class)); // update needs to be called to initialize tree

        assertEquals(1, simplePerceptionModule.getTrafficLightsInRange().size());
        assertEquals(TrafficLightState.GREEN, simplePerceptionModule.getTrafficLightsInRange().get(0).getTrafficLightState());
    }

    private void setupVehicles(CartesianPoint... positions) {
        List<VehicleData> vehiclesInIndex = new ArrayList<>();
        int i = 1;
        for (CartesianPoint position : positions) {
            String vehicleName = "veh_" + i++;
            VehicleData vehicleDataMock = mock(VehicleData.class);
            when(vehicleDataMock.getProjectedPosition()).thenReturn(position);
            when(vehicleDataMock.getName()).thenReturn(vehicleName);
            vehiclesInIndex.add(vehicleDataMock);
            VehicleType vehicleType = mock(VehicleType.class);
            when(vehicleType.getLength()).thenReturn(5d);
            when(vehicleType.getWidth()).thenReturn(2.5d);
            when(vehicleType.getHeight()).thenReturn(10d);
            when(vehicleType.getName()).thenReturn("vType");

            SimulationKernel.SimulationKernel.getVehicleTypes().put(vehicleType.getName(), vehicleType);
            trafficObjectIndex.registerVehicleType(vehicleName, vehicleType.getName());
        }
        trafficObjectIndex.updateVehicles(vehiclesInIndex);
    }

    private void setupTrafficLights(CartesianPoint... positions) {
        HashMap<String, TrafficLightProgram> trafficLightProgramsMocks = new HashMap<>();
        TrafficLightProgram trafficLightProgramMock = mock(TrafficLightProgram.class);
        trafficLightProgramsMocks.put("0", trafficLightProgramMock);
        List<TrafficLight> trafficLightMocks = new ArrayList<>();
        int i = 0;
        for (CartesianPoint position : positions) {
            TrafficLight trafficLightMock = mock(TrafficLight.class);
            when(trafficLightMock.getPosition()).thenReturn(position.toGeo());
            when(trafficLightMock.getCurrentState()).thenReturn(TrafficLightState.GREEN);
            when(trafficLightMock.getIncomingLane()).thenReturn("E0_0");
            when(trafficLightMock.getOutgoingLane()).thenReturn("E1_0");
            when(trafficLightMock.getId()).thenReturn(i++);
            trafficLightMocks.add(trafficLightMock);
        }
        TrafficLightGroup trafficLightGroup = new TrafficLightGroup("tls", trafficLightProgramsMocks, trafficLightMocks);
        trafficObjectIndex.addTrafficLightGroup(trafficLightGroup);
    }

    private void useQuadTree() {
        VehicleTree vehicleTree = new VehicleTree();
        vehicleTree.splitSize = 20;
        vehicleTree.maxDepth = 12;
        trafficObjectIndex = new TrafficObjectIndex.Builder((mock(Logger.class)))
                .withVehicleIndex(vehicleTree)
                .build();
        when(cpcMock.getTrafficObjectIndex()).thenReturn(trafficObjectIndex);
    }

    private void useGrid() {
        VehicleGrid vehicleGrid = new VehicleGrid();
        vehicleGrid.cellHeight = 5;
        vehicleGrid.cellWidth = 5;
        trafficObjectIndex = new TrafficObjectIndex.Builder((mock(Logger.class)))
                .withVehicleIndex(vehicleGrid)
                .build();
        when(cpcMock.getTrafficObjectIndex()).thenReturn(trafficObjectIndex);
    }

    private void useTlTree() {
        TrafficLightTree trafficLightTree = new TrafficLightTree();
        trafficObjectIndex = new TrafficObjectIndex.Builder((mock(Logger.class)))
                .withTrafficLightIndex(trafficLightTree)
                .build();

        when(cpcMock.getTrafficObjectIndex()).thenReturn(trafficObjectIndex);
    }
}
