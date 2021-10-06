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

package org. eclipse.mosaic.fed.output.generator.websocket;

import static org.junit.Assert.assertNull;

import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.enums.DriveDirection;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.road.SimpleRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleSensors;
import org.eclipse.mosaic.lib.objects.vehicle.sensor.DistanceSensor;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.java_websocket.WebSocket;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public class WebsocketVisualizerServerTest {

    private WebsocketVisualizerServer websocketVisualizer;
    private WebSocket socketMock;
    private AtomicReference<String> sentString;

    @Before
    public void setup() {
        websocketVisualizer = new WebsocketVisualizerServer(Mockito.mock(InetSocketAddress.class));
        sentString = new AtomicReference<>(null);

        socketMock = Mockito.mock(WebSocket.class);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            sentString.set(invocation.getArgument(0));
            return null;
        }).when(socketMock).send(ArgumentMatchers.anyString());
    }

    /**
     * Tests if the vehicle updates are reduced to only necessary data before sending them to the client.
     */
    @Test
    public void collectRemovedVehiclesBeforeTransport() {
        // setup
        websocketVisualizer.updateVehicleUpdates(
                new VehicleUpdates(0, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList("veh_0"))
        );
        websocketVisualizer.updateVehicleUpdates(
                new VehicleUpdates(0, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList())
        );
        websocketVisualizer.updateVehicleUpdates(
                new VehicleUpdates(0, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList("veh_1", "veh_2"))
        );

        // all vehicles to be removed should be sent to the client
        websocketVisualizer.onMessage(socketMock, (String) null);
        Assert.assertEquals("{\"VehiclesRemove\":[\"veh_0\",\"veh_1\",\"veh_2\"]}", sentString.get());

        // send another vehicle to be removed -> only this vehicle is expected in the message to be sent
        websocketVisualizer.updateVehicleUpdates(
                new VehicleUpdates(0, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList("veh_4"))
        );
        websocketVisualizer.onMessage(socketMock, (String) null);
        Assert.assertEquals("{\"VehiclesRemove\":[\"veh_4\"]}", sentString.get());

        // send no more removed vehicles -> expect no message to be sent
        sentString.set(null);
        websocketVisualizer.updateVehicleUpdates(
                new VehicleUpdates(0, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList())
        );
        websocketVisualizer.onMessage(socketMock, (String) null);
        assertNull(sentString.get());
    }

    /**
     * Tests if the vehicle updates are reduced to only necessary data before sending them to the client.
     */
    @Test
    public void reduceVehicleDataBeforeTransport() {
        // setup
        // no vehicle in front, 50m behind, no sensors left/right available
        final DistanceSensor distanceSensor = new DistanceSensor(Double.POSITIVE_INFINITY, 50d, -1, -1);
        final VehicleSensors vehSensors = new VehicleSensors(distanceSensor, null);
        final VehicleData vehicleDataToSend = new VehicleData.Builder(111, "1")
                .position(GeoPoint.lonLat(11, 10), null)
                .movement(10, 0d, 0d)
                .orientation(DriveDirection.UNAVAILABLE, -18.0, 0d)
                .road(new SimpleRoadPosition("prev", "upcoming", 0, 1d))
                .sensors(vehSensors)
                .create();
        final VehicleUpdates vehMovementsToSend =
                new VehicleUpdates(0, Lists.newArrayList(), Lists.newArrayList(vehicleDataToSend), Lists.newArrayList());
        websocketVisualizer.updateVehicleUpdates(vehMovementsToSend);

        // run
        websocketVisualizer.onMessage(socketMock, (String) null);

        // read sent json back to object 
        final Gson gson = new Gson();
        final JsonElement jsonElement = gson.fromJson(sentString.get(), JsonElement.class);
        final VehicleUpdates actualVehMovementsReceived =
                gson.fromJson(jsonElement.getAsJsonObject().get(VehicleUpdates.TYPE_ID), VehicleUpdates.class);

        // assert
        final VehicleData vehicleDataToReceive = new VehicleData.Builder(111, "1")
                .position(GeoPoint.lonLat(11, 10), null)
                .create();
        final VehicleUpdates vehMovementsToReceive =
                new VehicleUpdates(0, Lists.newArrayList(), Lists.newArrayList(vehicleDataToReceive), Lists.newArrayList());

        Assert.assertEquals(vehMovementsToReceive, actualVehMovementsReceived);
    }

}
