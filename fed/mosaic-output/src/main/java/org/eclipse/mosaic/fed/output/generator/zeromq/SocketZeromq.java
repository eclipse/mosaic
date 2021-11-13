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

package org. eclipse.mosaic.fed.output.generator.zeromq;

import org.eclipse.mosaic.fed.output.ambassador.AbstractOutputGenerator;
import org.eclipse.mosaic.fed.output.ambassador.Handle;

import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.electricity.ChargingStationUpdate;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;

import java.net.InetSocketAddress;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

import java.lang.Integer;

public class SocketZeromq extends AbstractOutputGenerator {

    private final SocketZeromqServer socketZeromqServer;

    public SocketZeromq(Integer port) {
        socketZeromqServer = new SocketZeromqServer(port);
        socketZeromqServer.start();
    }

    @Handle
    public void messageInteraction(VehicleUpdates interaction) throws Exception {
        socketZeromqServer.updateVehicleUpdates(interaction);
    }

    @Handle
    public void messageInteraction(V2xMessageTransmission interaction) throws Exception {
        socketZeromqServer.sendV2xMessage(interaction);
    }

    @Handle
    public void messageInteraction(V2xMessageReception interaction) throws Exception {
        socketZeromqServer.receiveV2xMessage(interaction);
    }

    @Handle
    public void messageInteraction(VehicleRegistration interaction) throws Exception {
        socketZeromqServer.addVehicle(interaction);
    }

    @Handle
    public void messageInteraction(RsuRegistration interaction) throws Exception {
        socketZeromqServer.addRoadsideUnit(interaction);
    }

    @Handle
    public void messageInteraction(TrafficLightRegistration interaction) throws Exception {
        socketZeromqServer.addTrafficLight(interaction);
    }

    @Handle
    public void messageInteraction(ChargingStationRegistration interaction) throws Exception {
        socketZeromqServer.addChargingStation(interaction);
    }

    @Handle
    public void messageInteraction(ChargingStationUpdate interaction) throws Exception {
        socketZeromqServer.updateChargingStation(interaction);
    }

}
