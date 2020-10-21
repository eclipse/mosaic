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

package org.eclipse.mosaic.fed.sumo.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.sumo.traci.TraciClient;
import org.eclipse.mosaic.fed.sumo.traci.facades.TraciPoiFacade;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.geo.UtmZone;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.objects.trafficsign.LaneAssignment;
import org.eclipse.mosaic.lib.objects.trafficsign.SpeedLimit;
import org.eclipse.mosaic.lib.objects.trafficsign.TrafficSignLaneAssignment;
import org.eclipse.mosaic.lib.objects.trafficsign.TrafficSignSpeed;
import org.eclipse.mosaic.lib.util.objects.Position;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TrafficSignManagerTest {

    @Rule
    public final GeoProjectionRule transformRule = new GeoProjectionRule(
            UtmPoint.eastNorth(
                    UtmZone.from(GeoPoint.lonLat(13.0, 52.0)),
                    -385281.94,
                    -5817994.50
            )
    );

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private TraciPoiFacade poiControlMock = Mockito.mock(TraciPoiFacade.class);

    private TrafficSignManager trafficSignManager;

    @Before
    public void setup() throws IOException {
        final TraciClient traciClient = Mockito.mock(TraciClient.class);
        when(traciClient.getPoiControl()).thenReturn(poiControlMock);

        trafficSignManager = new TrafficSignManager(3.2);
        trafficSignManager.configure(traciClient, temporaryFolder.getRoot());
    }

    @Test
    public void addAndGenerateSpeedSign() throws InternalFederateException, IOException {
        //SETUP
        List<SpeedLimit> speedLimits = Lists.newArrayList(
                new SpeedLimit(0, 30 / 3.6),
                new SpeedLimit(1, 50 / 3.6),
                new SpeedLimit(2, 150 / 3.6)
        );
        TrafficSignSpeed speedSign = new TrafficSignSpeed("speed01", new Position(GeoPoint.lonLat(13.5, 52.2)), "edge1", speedLimits);
        speedSign.setAngle(30);

        //RUN
        trafficSignManager.addSpeedSign(speedSign);

        //ASSERT
        Path base = Paths.get("trafficsigns");
        verify(poiControlMock).addImagePoi(
                eq("speed01_lane0"), isA(CartesianPoint.class),
                eq(base.resolve("30-lane0.png").toString()),
                eq(3.2), eq(3.2), eq(30d)
        );

        verify(poiControlMock).addImagePoi(
                eq("speed01_lane1"), isA(CartesianPoint.class),
                eq(base.resolve("50-lane1.png").toString()),
                eq(3.2), eq(3.2), eq(30d)
        );

        verify(poiControlMock).addImagePoi(
                eq("speed01_lane2"), isA(CartesianPoint.class),
                eq(base.resolve("150-lane2.png").toString()),
                eq(3.2), eq(3.2), eq(30d)
        );

        long fileSize0 = Files.size(temporaryFolder.getRoot().toPath().resolve(base.resolve("30-lane0.png")));
        assertEquals(70d, fileSize0 / 1000d, 20d);

        long fileSize1 = Files.size(temporaryFolder.getRoot().toPath().resolve(base.resolve("50-lane1.png")));
        assertEquals(70d, fileSize1 / 1000d, 20d);

        long fileSize2 = Files.size(temporaryFolder.getRoot().toPath().resolve(base.resolve("150-lane2.png")));
        assertEquals(70d, fileSize2 / 1000d, 20d);
    }

    @Test
    public void addAndGenerateLaneAssignment() throws InternalFederateException, IOException {
        //SETUP
        List<LaneAssignment> laneAssignments = Lists.newArrayList(
                new LaneAssignment(0, Lists.newArrayList(VehicleClass.AutomatedVehicle)),
                new LaneAssignment(1, Lists.newArrayList(VehicleClass.values())),
                new LaneAssignment(2, Lists.newArrayList())
        );
        TrafficSignLaneAssignment laneAssignmentSign = new TrafficSignLaneAssignment("laneAssignment1", new Position(GeoPoint.lonLat(13.5, 52.2)), "edge1", laneAssignments);
        laneAssignmentSign.setAngle(30);

        //RUN
        trafficSignManager.addLaneAssignmentSign(laneAssignmentSign);

        //ASSERT
        Path base = Paths.get("trafficsigns");
        verify(poiControlMock).addImagePoi(
                eq("laneAssignment1_lane0"), isA(CartesianPoint.class),
                eq(base.resolve("AV-lane0.png").toString()),
                eq(3.2), eq(3.2), eq(30d)
        );

        verify(poiControlMock).addImagePoi(
                eq("laneAssignment1_lane1"), isA(CartesianPoint.class),
                eq(base.resolve("ALL-lane1.png").toString()),
                eq(3.2), eq(3.2), eq(30d)
        );

        verify(poiControlMock).addImagePoi(
                eq("laneAssignment1_lane2"), isA(CartesianPoint.class),
                eq(base.resolve("EMPTY-lane2.png").toString()),
                eq(3.2), eq(3.2), eq(30d)
        );

        long fileSize0 = Files.size(temporaryFolder.getRoot().toPath().resolve(base.resolve("AV-lane0.png")));
        assertEquals(15d, fileSize0 / 1000d, 5d);

        long fileSize1 = Files.size(temporaryFolder.getRoot().toPath().resolve(base.resolve("ALL-lane1.png")));
        assertEquals(15d, fileSize1 / 1000d, 5d);

        long fileSize2 = Files.size(temporaryFolder.getRoot().toPath().resolve(base.resolve("EMPTY-lane2.png")));
        assertEquals(70d, fileSize2 / 1000d, 5d);
    }

}