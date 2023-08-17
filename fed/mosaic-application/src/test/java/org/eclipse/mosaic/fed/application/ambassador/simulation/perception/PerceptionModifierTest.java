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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.navigation.CentralNavigationComponent;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.BoundingBoxOcclusionModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.DistanceModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.PositionErrorModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.SimpleOcclusionModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.WallOcclusionModifier;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.TrafficObjectIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleMap;
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
import java.util.Map;
import java.util.stream.Collectors;

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

    @Mock
    public VehicleType vehicleType;

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
        // register vehicleType
        when(vehicleType.getName()).thenReturn("vType");
        when(vehicleType.getLength()).thenReturn(5d);
        when(vehicleType.getWidth()).thenReturn(2.5d);
        when(vehicleType.getHeight()).thenReturn(10d);
        trafficObjectIndex = new TrafficObjectIndex.Builder(mock(Logger.class))
                .withVehicleIndex(new VehicleMap())
                .build();
        // setup cpc
        when(cpcMock.getTrafficObjectIndex()).thenReturn(trafficObjectIndex);
        // setup perception module
        trafficObjectIndex.registerVehicleType("veh_0", vehicleType);
        VehicleUnit egoVehicleUnit = spy(new VehicleUnit("veh_0", vehicleType, null));
        doReturn(egoVehicleData).when(egoVehicleUnit).getVehicleData();
        simplePerceptionModule = spy(new SimplePerceptionModule(egoVehicleUnit, null, mock(Logger.class)));
        doReturn(simplePerceptionModule).when(egoVehicleUnit).getPerceptionModule();
        // setup ego vehicle
        when(egoVehicleData.getHeading()).thenReturn(90d);
        when(egoVehicleData.getProjectedPosition()).thenReturn(EGO_POSITION);

        List<CartesianPoint> randomPoints = createRandomlyDistributedPointsInRange(EGO_POSITION, VIEWING_RANGE, VEHICLE_AMOUNT);
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
        simplePerceptionModule.enable(
                new SimplePerceptionConfiguration.Builder(VIEWING_ANGLE, VIEWING_RANGE).addModifier(occlusionModifier).build()
        );
        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        if (PRINT_POSITIONS) {
            printPerceivedPositions(perceivedVehicles);
        }
        assertTrue("The occlusion filter should remove vehicles", VEHICLE_AMOUNT > perceivedVehicles.size());
    }

    @Test
    public void testDistanceErrorModifier() {
        DistanceModifier distanceModifier = new DistanceModifier(rng, 0);
        simplePerceptionModule.enable(
                new SimplePerceptionConfiguration.Builder(VIEWING_ANGLE, VIEWING_RANGE).addModifier(distanceModifier).build()
        );

        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        if (PRINT_POSITIONS) {
            printPerceivedPositions(perceivedVehicles);
        }
        assertTrue("The distance filter should remove vehicles", VEHICLE_AMOUNT > perceivedVehicles.size());
    }

    @Test
    public void testPositionErrorModifier() {
        PositionErrorModifier positionErrorModifier = new PositionErrorModifier(rng, 1, 1);
        simplePerceptionModule.enable(
                new SimplePerceptionConfiguration.Builder(VIEWING_ANGLE, VIEWING_RANGE).addModifier(positionErrorModifier).build()
        );

        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        if (PRINT_POSITIONS) {
            printPerceivedPositions(perceivedVehicles);
        }
        assertEquals("The position error filter shouldn't remove vehicles", VEHICLE_AMOUNT, perceivedVehicles.size());
    }

    @Test
    public void testWallOcclusionModifier() {
        List<Edge<Vector3d>> surroundingWalls = Lists.newArrayList(
                new Edge<>(CartesianPoint.xy(10, 10).toVector3d(), CartesianPoint.xy(10, -10).toVector3d())
        );
        doReturn(surroundingWalls).when(simplePerceptionModule).getSurroundingWalls();

        WallOcclusionModifier occlusionModifier = new WallOcclusionModifier();
        simplePerceptionModule.enable(
                new SimplePerceptionConfiguration.Builder(VIEWING_ANGLE, VIEWING_RANGE).addModifier(occlusionModifier).build()
        );
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

    @Test
    public void testIndexedObjectsNotChanged() {
        PositionErrorModifier positionErrorModifier = new PositionErrorModifier(rng, 1, 1);
        simplePerceptionModule.enable(
                new SimplePerceptionConfiguration.Builder(VIEWING_ANGLE, VIEWING_RANGE).addModifier(positionErrorModifier).build()
        );

        // collect positions of perceived objects BEFORE applying modifier
        PerceptionModel godView = mock(PerceptionModel.class);
        when(godView.isInRange(isA(SpatialObject.class))).thenReturn(true);
        Map<String, Vector3d> allVehiclesInIndexPre = trafficObjectIndex.getVehiclesInRange(godView)
                .stream().collect(Collectors.toMap(VehicleObject::getId, v -> new Vector3d(v.getPosition())));

        // RUN perceived objects and modify positions
        List<VehicleObject> perceivedAndAlteredObjects = simplePerceptionModule.getPerceivedVehicles();

        // collect positions of perceived objects AFTER applying modifier
        Map<String, Vector3d> allVehiclesInIndexPost = trafficObjectIndex.getVehiclesInRange(godView)
                .stream().collect(Collectors.toMap(VehicleObject::getId, v -> new Vector3d(v.getPosition())));

        // ASSERT that all positions in the index are still the same as before applying modifier
        allVehiclesInIndexPre.forEach((id, pos) -> {
            assertTrue(pos.isFuzzyEqual(allVehiclesInIndexPost.get(id)));
        });

        // ASSERT that all modified positions differ from the positions before (or after) applying the modifier
        for (VehicleObject object : perceivedAndAlteredObjects) {
            assertFalse(object.getPosition().isFuzzyEqual(allVehiclesInIndexPre.get(object.getId())));
        }
    }

    @Test
    public void testBoundingBoxOcclusionModifier() {
        BoundingBoxOcclusionModifier boundingBoxOcclusionModifier = new BoundingBoxOcclusionModifier();
        simplePerceptionModule.enable(
                new SimplePerceptionConfiguration.Builder(VIEWING_ANGLE, VIEWING_RANGE).addModifier(boundingBoxOcclusionModifier).build()
        );
        List<VehicleObject> perceivedVehicles = simplePerceptionModule.getPerceivedVehicles();
        if (PRINT_POSITIONS) {
            printPerceivedPositions(perceivedVehicles);
        }
        assertTrue("The occlusion filter should remove vehicles", VEHICLE_AMOUNT > perceivedVehicles.size());
    }

    private List<CartesianPoint> createRandomlyDistributedPointsInRange(CartesianPoint origin, double range, int amount) {
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
            String vehicleId = "veh_" + i++;
            VehicleData vehicleDataMock = mock(VehicleData.class);
            when(vehicleDataMock.getProjectedPosition()).thenReturn(position);
            when(vehicleDataMock.getName()).thenReturn(vehicleId);
            when(vehicleDataMock.getHeading()).thenReturn(rng.nextDouble() * 360d);
            vehiclesInIndex.add(vehicleDataMock);
            trafficObjectIndex.registerVehicleType(vehicleId, vehicleType);
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
