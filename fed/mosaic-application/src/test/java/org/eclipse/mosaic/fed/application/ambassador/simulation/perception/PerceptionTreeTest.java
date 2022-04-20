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

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.PerceptionTree;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.geo.UtmZone;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PerceptionTreeTest {

    private static final String EGO_VEHICLE = "veh_5019";
    private final EventManager eventManagerMock = mock(EventManager.class);
    private final CentralPerceptionComponent cpcMock = mock(CentralPerceptionComponent.class);

    @Rule
    public GeoProjectionRule wgs84transform = new GeoProjectionRule(
            UtmPoint.eastNorth(UtmZone.from(GeoPoint.latLon(41.974579, 2.78203)), 439896.18, 4601971.19)
    );

    @Rule
    @InjectMocks
    public SimulationKernelRule simulationKernelRule = new SimulationKernelRule(eventManagerMock, null, null, cpcMock);

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    private SpatialVehicleIndex vehicleIndex = new PerceptionTree(new CartesianRectangle(
            new MutableCartesianPoint(0, 0, 0), new MutableCartesianPoint(58451.02, 95461.87, 0)),
            20, 12);

    @Mock
    public VehicleData egoVehicleData;

    private SimplePerceptionModule simplePerceptionModule;

    private BufferedReader reader;
    private VehicleUnit egoVehicleUnit;

    @Before
    public void setup() throws IOException, URISyntaxException {
        reader = getOutputReader("/output.csv");
        when(cpcMock.getVehicleIndex()).thenReturn(vehicleIndex);

        when(cpcMock.getVehicleIndex()).thenReturn(vehicleIndex);
        // setup perception module
        egoVehicleUnit = spy(new VehicleUnit(EGO_VEHICLE, mock(VehicleType.class), null));
        doReturn(egoVehicleData).when(egoVehicleUnit).getVehicleData();
        simplePerceptionModule = spy(new SimplePerceptionModule(egoVehicleUnit, mock(Logger.class)));
        simplePerceptionModule.enable(new SimplePerceptionConfiguration(120d, 300d));
    }

    @Test
    public void noEmptyPerception() throws IOException {
        String line;
        List<VehicleData> vehicleUpdates = new ArrayList<>();
        long previousTime = 0;
        while ((line = reader.readLine()) != null) {
            line = line.replace("\n", "");
            String[] fields = line.split(";");
            long currentTime = Long.parseLong(fields[1]);

            if (fields[0].equals("VEHICLE_UPDATES")) {
                if (currentTime > previousTime) {
                    if (!vehicleUpdates.isEmpty()) {
                        vehicleIndex.updateVehicles(vehicleUpdates);
                        vehicleUpdates.clear();
                        System.out.println(currentTime + " : " + simplePerceptionModule.getPerceivedVehicles().size());
                        assertNotEquals(0, simplePerceptionModule.getPerceivedVehicles().size());
                    }
                } else {
                    VehicleData currVehicleData = mock(VehicleData.class);
                    when(currVehicleData.getName()).thenReturn(fields[2]);
                    when(currVehicleData.getProjectedPosition())
                            .thenReturn(GeoPoint.latLon(Double.parseDouble(fields[5]), Double.parseDouble(fields[6])).toCartesian());
                    when(currVehicleData.getHeading()).thenReturn(Double.parseDouble(fields[4]));
                    when(currVehicleData.getSpeed()).thenReturn(Double.parseDouble(fields[3]));
                    vehicleUpdates.add(currVehicleData);
                    if (currVehicleData.getName().equals(EGO_VEHICLE)) {
                        doReturn(currVehicleData).when(egoVehicleUnit).getVehicleData();
                    }
                }
            }
            previousTime = currentTime;
        }
    }

    private BufferedReader getOutputReader(String filename) throws IOException, URISyntaxException {
        InputStream fileStream = new FileInputStream(new File(this.getClass().getResource(filename).toURI()));
        Reader decoder = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
        return new BufferedReader(decoder);
    }
}
