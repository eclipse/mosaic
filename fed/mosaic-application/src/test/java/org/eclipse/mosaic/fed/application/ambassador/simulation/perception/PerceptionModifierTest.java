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
import org.eclipse.mosaic.fed.application.ambassador.navigation.CentralNavigationComponent;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.DistanceModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.PositionErrorModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.SimpleOcclusionModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.WallOcclusionModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.TrafficObjectIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.TrafficObjectIndexProvider;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleIndex;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.spatial.Edge;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class PerceptionModifierTest {
    // FLag used for visualization purposes
    private final static boolean PRINT_POSITIONS =
            Boolean.parseBoolean(StringUtils.defaultIfBlank(System.getenv("PRINT_POSITIONS"), "false"));

    private final static double VIEWING_RANGE = 100d;
    private final static double VIEWING_ANGLE = 360d;
    private final static CartesianPoint EGO_POSITION = CartesianPoint.xyz(0, 0, 0);
    private final static int VEHICLE_AMOUNT = 100;
    private final RandomNumberGenerator rng = new DefaultRandomNumberGenerator(1);
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

    public TrafficObjectIndex trafficObjectIndex;

    private SimplePerceptionModule simplePerceptionModule;

    @Before
    public void setup() {
        when(cpcMock.getScenarioBounds())
                .thenReturn(new CartesianRectangle(new MutableCartesianPoint(-VIEWING_RANGE * 2, -VIEWING_RANGE * 2, 0),
                        new MutableCartesianPoint(VIEWING_RANGE * 2, VIEWING_ANGLE * 2, 0)));
        SimulationKernel.SimulationKernel.setConfiguration(new CApplicationAmbassador());

        trafficObjectIndex = new TrafficObjectIndexProvider.Builder(mock(Logger.class))
                .withVehicleIndexProvider(new VehicleIndex())
                .build();
        // setup cpc
        when(cpcMock.getSpatialIndex()).thenReturn(trafficObjectIndex);
        // setup perception module
        VehicleUnit egoVehicleUnit = spy(new VehicleUnit("veh_0", mock(VehicleType.class), null));
        doReturn(egoVehicleData).when(egoVehicleUnit).getVehicleData();
        simplePerceptionModule = spy(new SimplePerceptionModule(egoVehicleUnit, null, mock(Logger.class)));
        doReturn(simplePerceptionModule).when(egoVehicleUnit).getPerceptionModule();
        // setup ego vehicle
        when(egoVehicleData.getHeading()).thenReturn(90d);
        when(egoVehicleData.getProjectedPosition()).thenReturn(EGO_POSITION);

        List<CartesianPoint> randomPoints = getRandomlyDistributedPointsInRange(EGO_POSITION, VIEWING_RANGE, VEHICLE_AMOUNT);
        if (PRINT_POSITIONS) {
            for (CartesianPoint randomPoint : randomPoints) {
                System.out.println(randomPoint.getX() + ", " + randomPoint.getY());
            }
            System.out.println();
        }
        setupSpatialIndex(randomPoints.toArray(new CartesianPoint[0]));
    }

    @Test
    public void testOcclusionModifier() {
        SimpleOcclusionModifier occlusionModifier = new SimpleOcclusionModifier(3, 10);
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(VIEWING_ANGLE, VIEWING_RANGE, occlusionModifier));
        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        if (PRINT_POSITIONS) {
            printPerceivedPositions(perceivedVehicles);
        }
        assertTrue("The occlusion filter should remove vehicles", VEHICLE_AMOUNT > perceivedVehicles.size());
    }

    @Test
    public void testDistanceErrorModifier() {
        DistanceModifier distanceModifier = new DistanceModifier(rng, 0);
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(VIEWING_ANGLE, VIEWING_RANGE, distanceModifier));

        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        if (PRINT_POSITIONS) {
            printPerceivedPositions(perceivedVehicles);
        }
        assertTrue("The distance filter should remove vehicles", VEHICLE_AMOUNT > perceivedVehicles.size());
    }

    @Test
    public void testPositionErrorModifier() {
        PositionErrorModifier positionErrorModifier = new PositionErrorModifier(rng, 1, 1);
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(VIEWING_ANGLE, VIEWING_RANGE, positionErrorModifier));

        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        if (PRINT_POSITIONS) {
            printPerceivedPositions(perceivedVehicles);
        }
        assertTrue("The position error filter shouldn't remove vehicles", VEHICLE_AMOUNT == perceivedVehicles.size());
    }

    @Test
    public void testWallOcclusionModifier() {
        List<Edge<Vector3d>> surroundingWalls = Lists.newArrayList(
                new Edge<>(CartesianPoint.xy(10, 10).toVector3d(), CartesianPoint.xy(10, -10).toVector3d())
        );
        doReturn(surroundingWalls).when(simplePerceptionModule).getSurroundingWalls();

        WallOcclusionModifier occlusionModifier = new WallOcclusionModifier();
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(VIEWING_ANGLE, VIEWING_RANGE, occlusionModifier));
        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        if (PRINT_POSITIONS) {
            printPerceivedPositions(perceivedVehicles);
        }
        assertTrue("The occlusion filter should remove vehicles", VEHICLE_AMOUNT > perceivedVehicles.size());
        // assert roughly that every perceived vehicle right of the wall is not hidden by the wall
        for (VehicleObject v : perceivedVehicles) {
            if (v.getProjectedPosition().getX() > 10) {
                assertTrue(v.getProjectedPosition().getY() > 10 || v.getProjectedPosition().getY() < -10);
            }
        }
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

        CartesianPoint randomPoint = CartesianPoint.xyz(origin.getX() + (range + 10), origin.getY() + (range + 10), origin.getZ());
        while (randomPoint.distanceTo(origin) > VIEWING_RANGE) {
            double randomX = lowerX <= upperX ? rng.nextDouble(lowerX, upperX) : rng.nextDouble(upperX, lowerX);
            double randomY = lowerY <= upperY ? rng.nextDouble(lowerY, upperY) : rng.nextDouble(upperY, lowerY);
            randomPoint = CartesianPoint.xyz(randomX, randomY, 0);

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
        trafficObjectIndex.updateVehicles(vehiclesInIndex);
    }

    private void printPerceivedPositions(List<VehicleObject> perceivedVehicles) {
        for (VehicleObject vehicleObject : perceivedVehicles) {
            CartesianPoint point = vehicleObject.toCartesian();
            System.out.println(point.getX() + ", " + point.getY());
        }
    }
}
