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

import org.eclipse.mosaic.fed.output.ambassador.AbstractOutputGenerator;
import org.eclipse.mosaic.fed.output.ambassador.Handle;

import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.electricity.ChargingStationUpdates;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;

import java.net.InetSocketAddress;

public class WebsocketVisualizer extends AbstractOutputGenerator {

    private final WebsocketVisualizerServer websocketVisualizerServer;

    public WebsocketVisualizer(int port) {
        websocketVisualizerServer = new WebsocketVisualizerServer(new InetSocketAddress(port));
        websocketVisualizerServer.start();
    }

    @Handle
    public void visualizeInteraction(VehicleUpdates interaction) throws Exception {
        websocketVisualizerServer.updateVehicleUpdates(interaction);
    }

    @Handle
    public void visualizeInteraction(V2xMessageTransmission interaction) throws Exception {
        websocketVisualizerServer.sendV2xMessage(interaction);
    }

    @Handle
    public void visualizeInteraction(V2xMessageReception interaction) throws Exception {
        websocketVisualizerServer.receiveV2xMessage(interaction);
    }

    @Handle
    public void visualizeInteraction(VehicleRegistration interaction) throws Exception {
        websocketVisualizerServer.addVehicle(interaction);
    }

    @Handle
    public void visualizeInteraction(RsuRegistration interaction) throws Exception {
        websocketVisualizerServer.addRoadsideUnit(interaction);
    }

    @Handle
    public void visualizeInteraction(TrafficLightRegistration interaction) throws Exception {
        websocketVisualizerServer.addTrafficLight(interaction);
    }

    @Handle
    public void visualizeInteraction(ChargingStationRegistration interaction) throws Exception {
        websocketVisualizerServer.addChargingStation(interaction);
    }

    @Handle
    public void visualizeInteraction(ChargingStationUpdates interaction) throws Exception {
        websocketVisualizerServer.updateChargingStation(interaction);
    }

}
