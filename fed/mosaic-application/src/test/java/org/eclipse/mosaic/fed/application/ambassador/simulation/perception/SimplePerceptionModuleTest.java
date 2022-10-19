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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.PerceptionGrid;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.PerceptionIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.PerceptionTree;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
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
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SimplePerceptionModuleTest {

    private final EventManager eventManagerMock = mock(EventManager.class);
    private final CentralPerceptionComponent cpcMock = mock(CentralPerceptionComponent.class);
    private final CentralNavigationComponent cncMock = mock(CentralNavigationComponent.class);

    @Rule
    public MockitoRule initRule = MockitoJUnit.rule();

    @Mock
    public VehicleData egoVehicleData;

    @Rule
    public SimulationKernelRule simulationKernelRule = new SimulationKernelRule(eventManagerMock, null, cncMock, cpcMock);

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    public SpatialVehicleIndex vehicleIndex;

    private SimplePerceptionModule simplePerceptionModule;

    @Before
    public void setup() {
        SimulationKernel.SimulationKernel.setConfiguration(new CApplicationAmbassador());

        vehicleIndex = new PerceptionIndex();
        // setup cpc
        when(cpcMock.getVehicleIndex()).thenReturn(vehicleIndex);
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
        setupSpatialIndex(new MutableCartesianPoint(110, 100, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_outOfRange_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(310, 100, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnLeftBoundVector_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(110, 110, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_OnLeftBoundVector_OppositeDirection_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(90, 90, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnRightBoundVector_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(110, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarLeft_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(105, 115, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarRight_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(105, 90, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_270viewingAngle_VehicleOnDirectionVector() {
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupSpatialIndex(new MutableCartesianPoint(110, 100, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarLeft_270viewingAngle_TrivialIndex() {
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupSpatialIndex(new MutableCartesianPoint(105, 115, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarRight_270viewingAngle_TrivialIndex() {
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupSpatialIndex(new MutableCartesianPoint(105, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_QuadTree() {
        useQuadTree();
        setupSpatialIndex(new MutableCartesianPoint(110, 100, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_outOfRange_QuadTree() {
        useQuadTree();
        setupSpatialIndex(new MutableCartesianPoint(310, 100, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnLeftBoundVector_QuadTree() {
        useQuadTree();
        setupSpatialIndex(new MutableCartesianPoint(110, 110, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnRightBoundVector_QuadTree() {
        useQuadTree();
        setupSpatialIndex(new MutableCartesianPoint(110, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarLeft_QuadTree() {
        useQuadTree();
        setupSpatialIndex(new MutableCartesianPoint(105, 115, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarRight_QuadTree() {
        useQuadTree();
        setupSpatialIndex(new MutableCartesianPoint(105, 90, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarLeft_270viewingAngle_QuadTree() {
        useQuadTree();
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupSpatialIndex(new MutableCartesianPoint(105, 115, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarRight_270viewingAngle_QuadTree() {
        useQuadTree();
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupSpatialIndex(new MutableCartesianPoint(105, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_Grid() {
        useGrid();
        setupSpatialIndex(new MutableCartesianPoint(110, 100, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_outOfRange_Grid() {
        useGrid();
        setupSpatialIndex(new MutableCartesianPoint(310, 100, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnLeftBoundVector_Grid() {
        useGrid();
        setupSpatialIndex(new MutableCartesianPoint(110, 110, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_OnRightBoundVector_Grid() {
        useGrid();
        setupSpatialIndex(new MutableCartesianPoint(110, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarLeft_Grid() {
        useGrid();
        setupSpatialIndex(new MutableCartesianPoint(105, 115, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_tooFarRight_Grid() {
        useGrid();
        setupSpatialIndex(new MutableCartesianPoint(105, 90, 0));
        assertEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarLeft_270viewingAngle_Grid() {
        useGrid();
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupSpatialIndex(new MutableCartesianPoint(105, 115, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCanBePerceived_FarRight_270viewingAngle_Grid() {
        useGrid();
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(270d, 200d)); // overwrite config
        setupSpatialIndex(new MutableCartesianPoint(105, 90, 0));
        assertEquals(1, simplePerceptionModule.getPerceivedVehicles().size());
    }

    private void setupSpatialIndex(CartesianPoint... positions) {
        List<VehicleData> vehiclesInIndex = new ArrayList<>();
        int i = 1;
        for (CartesianPoint position : positions) {
            VehicleData vehicleDataMock = mock(VehicleData.class);
            when(vehicleDataMock.getProjectedPosition()).thenReturn(position);
            when(vehicleDataMock.getName()).thenReturn("veh_" + i++);
            vehiclesInIndex.add(vehicleDataMock);
        }
        vehicleIndex.updateVehicles(vehiclesInIndex);
    }

    private void useQuadTree() {
        vehicleIndex = new PerceptionTree(new CartesianRectangle(
                new MutableCartesianPoint(100, 90, 0), new MutableCartesianPoint(310, 115, 0)), 20, 12
        );
        when(cpcMock.getVehicleIndex()).thenReturn(vehicleIndex);
    }

    private void useGrid() {
        vehicleIndex = new PerceptionGrid(new CartesianRectangle(
                new MutableCartesianPoint(100, 90, 0), new MutableCartesianPoint(310, 115, 0)), 5, 5
        );
        when(cpcMock.getVehicleIndex()).thenReturn(vehicleIndex);
    }
}
