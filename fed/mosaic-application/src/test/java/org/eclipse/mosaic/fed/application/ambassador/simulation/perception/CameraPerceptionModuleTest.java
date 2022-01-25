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
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.lib.PerceptionIndex;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CameraPerceptionModuleTest {

    private final EventManager eventManagerMock = mock(EventManager.class);
    private final CentralPerceptionComponent cpcMock = mock(CentralPerceptionComponent.class);

    @Mock
    public VehicleData egoVehicleData;

    @Rule
    @InjectMocks
    public SimulationKernelRule simulationKernelRule = new SimulationKernelRule(eventManagerMock, null, null, cpcMock);

    @Mock
    public PerceptionIndex perceptionIndex;

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    private CameraPerceptionModule cameraPerceptionModule;

    private CameraPerceptionModuleConfiguration configuration = mock(CameraPerceptionModuleConfiguration.class);

    @Before
    public void setup() {
        // setup cpc
        when(cpcMock.getSpatialIndex()).thenReturn(perceptionIndex);
        // setup config
        configuration.viewingRange = 200d;
        configuration.viewingAngle = 108d;
        // setup perception module
        VehicleUnit egoVehicleUnit = spy(new VehicleUnit("veh_0", mock(VehicleType.class), null));
        doReturn(egoVehicleData).when(egoVehicleUnit).getVehicleData();
        cameraPerceptionModule = spy(new CameraPerceptionModule(egoVehicleUnit, mock(Logger.class)));
        cameraPerceptionModule.enable(configuration);

        // setup ego vehicle
        when(egoVehicleData.getName()).thenReturn("veh_0");
        when(egoVehicleData.getHeading()).thenReturn(90d);
        when(egoVehicleData.getProjectedPosition()).thenReturn(new MutableCartesianPoint(100, 100, 0));
    }

    @Test
    public void vehicleCanBePerceived_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(110, 100, 0));
        assertEquals(1, cameraPerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_outOfRange_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(310, 100, 0));
        assertEquals(0, cameraPerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_toFarLeft_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(105, 115, 0));
        assertEquals(0, cameraPerceptionModule.getPerceivedVehicles().size());
    }

    @Test
    public void vehicleCannotBePerceived_toFarRight_TrivialIndex() {
        setupSpatialIndex(new MutableCartesianPoint(105, 90, 0));
        assertEquals(0, cameraPerceptionModule.getPerceivedVehicles().size());
    }

    private void setupSpatialIndex(CartesianPoint... positions) {
        List<VehicleData> vehiclesInIndex = new ArrayList<>();
        int nameSuffix = 0;
        for (CartesianPoint position : positions) {
            VehicleData vehicleDataMock = mock(VehicleData.class);
            when(vehicleDataMock.getName()).thenReturn("perceived_vehicle_" + nameSuffix);
            when(vehicleDataMock.getProjectedPosition()).thenReturn(position);
            vehiclesInIndex.add(vehicleDataMock);
            nameSuffix++;
        }
        when(cpcMock.getSpatialIndex().getVehiclesInIndexRange(isA(CartesianPoint.class), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(vehiclesInIndex);
    }


}
