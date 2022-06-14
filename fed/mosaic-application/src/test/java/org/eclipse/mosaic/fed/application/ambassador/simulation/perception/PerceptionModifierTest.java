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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.DistanceErrorModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.SimpleOcclusionModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.PerceptionIndex;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
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
public class PerceptionModifierTest {

    private final static double VIEWING_RANGE = 100d;
    private final static double VIEWING_ANGLE = 360d;
    private final static CartesianPoint EGO_POSITION = new MutableCartesianPoint(0, 0, 0);
    private final static int VEHICLE_AMOUNT = 100;
    private final RandomNumberGenerator rng = new DefaultRandomNumberGenerator(1);
    private final EventManager eventManagerMock = mock(EventManager.class);
    private final CentralPerceptionComponent cpcMock = mock(CentralPerceptionComponent.class);

    @Mock
    public VehicleData egoVehicleData;

    @Rule
    @InjectMocks
    public SimulationKernelRule simulationKernelRule = new SimulationKernelRule(eventManagerMock, null, null, cpcMock);

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
        simplePerceptionModule = spy(new SimplePerceptionModule(egoVehicleUnit, mock(Logger.class)));

        // setup ego vehicle
        when(egoVehicleData.getHeading()).thenReturn(90d);
        when(egoVehicleData.getProjectedPosition()).thenReturn(EGO_POSITION);

        List<CartesianPoint> randomPoints = getRandomlyDistributedPointsInRange(EGO_POSITION, VIEWING_RANGE, VEHICLE_AMOUNT);
        setupSpatialIndex(randomPoints.toArray(new CartesianPoint[0]));
    }

    @Test
    public void testOcclusionModifier() {
        SimpleOcclusionModifier occlusionModifier = new SimpleOcclusionModifier(3, 10);
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(VIEWING_ANGLE, VIEWING_RANGE, occlusionModifier));

//        for (int i = 0; i < randomPoints.size(); i++) {
//            System.out.println("Vehicle" + i + "=(" + randomPoints.get(i).getX() + ", " + randomPoints.get(i).getY() + ")");
//            System.out.println(randomPoints.get(i).getX() + ", " + randomPoints.get(i).getY());
//        }
        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        for (VehicleObject vehicleObject : perceivedVehicles) {
            CartesianPoint point = vehicleObject.toCartesian();
            System.out.println(point.getX() + ", " + point.getY());
        }
        assertTrue("The occlusion filter should remove vehicles", VEHICLE_AMOUNT >= perceivedVehicles.size());
    }

    @Test
    public void testDistanceErrorModifier() {
        DistanceErrorModifier distanceErrorModifier = new DistanceErrorModifier(rng);
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(VIEWING_ANGLE, VIEWING_RANGE, distanceErrorModifier));
    }


    private List<CartesianPoint> getRandomlyDistributedPointsInRange(CartesianPoint origin, double range, int amount) {
        List<CartesianPoint> points = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            points.add(getRandomPointInRange(origin, range));
        }
        return points;
    }

    private CartesianPoint getRandomPointInRange(CartesianPoint origin, double range) {
        double lowerX = origin.getX() - range;
        double lowerY = origin.getY() - range;
        double upperX = origin.getX() + range;
        double upperY = origin.getY() + range;

        CartesianPoint randomPoint = new MutableCartesianPoint(origin.getX() + (range + 10), origin.getY() + (range + 10), origin.getZ());
        while (randomPoint.distanceTo(origin) > VIEWING_RANGE) {
            double randomX = lowerX <= upperX ? rng.nextDouble(lowerX, upperX) : rng.nextDouble(upperX, lowerX);
            double randomY = lowerY <= upperY ? rng.nextDouble(lowerY, upperY) : rng.nextDouble(upperY, lowerY);
            randomPoint = new MutableCartesianPoint(randomX, randomY, 0);

        }
        return randomPoint;
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
}
