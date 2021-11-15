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

package org.eclipse.mosaic.fed.output.generator.zeromq;

import org.eclipse.mosaic.fed.output.ambassador.AbstractOutputGenerator;
import org.eclipse.mosaic.fed.output.ambassador.Handle;

import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.electricity.ChargingStationUpdate;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.TrafficDetectorUpdates;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.fed.output.generator.zeromq.ZmqMosaicInterface;


import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

import java.lang.Integer;

import com.google.protobuf.MessageLite;

public class SocketZeromq extends AbstractOutputGenerator {

    // private final SocketZeromqServer // socketZeromqServer;
    private final ZContext context = new ZContext();
    private final Socket publisher = context.createSocket(SocketType.XPUB);
    private final ZmqMosaicInterface zmqMosaicInterface = new ZmqMosaicInterface();

    public SocketZeromq(Integer port) {
        String address = "tcp://127.0.0.1:" + port.toString();
        publisher.bind(address);
        publisher.setSndHWM(100);
        // // socketZeromqServer = new SocketZeromqServer(port);
    }

    @Handle
    public void messageInteraction(VehicleUpdates interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);
        // publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        // publisher.send(interaction.toString(), 0);
        // socketZeromqServer.updateVehicleUpdates(interaction);
    }

    @Handle
    public void messageInteraction(V2xMessageTransmission interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);
        // publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        // publisher.send(interaction.toString(), 0);
        // socketZeromqServer.sendV2xMessage(interaction);
    }

    @Handle
    public void messageInteraction(V2xMessageReception interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);

        //publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        //publisher.send(interaction.toString(), 0);
        // socketZeromqServer.receiveV2xMessage(interaction);
    }

    @Handle
    public void messageInteraction(VehicleRegistration interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);

        //publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        //publisher.send(interaction.toString(), 0);
        // socketZeromqServer.addVehicle(interaction);
    }

    @Handle
    public void messageInteraction(RsuRegistration interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);

        //publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        //publisher.send(interaction.toString(), 0);
        // socketZeromqServer.addRoadsideUnit(interaction);
    }

    @Handle
    public void messageInteraction(TrafficLightRegistration interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);

        //publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        //publisher.send(interaction.toString(), 0);
        // socketZeromqServer.addTrafficLight(interaction);
    }


    @Handle
    public void messageInteraction(ChargingStationRegistration interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);

        //publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        //publisher.send(interaction.toString(), 0);
        // socketZeromqServer.addChargingStation(interaction);
    }

    @Handle
    public void messageInteraction(ChargingStationUpdate interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);

        //publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        //publisher.send(interaction.toString(), 0);
        // socketZeromqServer.updateChargingStation(interaction);
    }

    @Handle
    public void messageInteraction(TrafficDetectorUpdates interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);

        //publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        //publisher.send(interaction.toString(), 0);
        // socketZeromqServer.updateTrafficDetectors(interaction);
    }

    @Handle
    public void messageInteraction(TmcRegistration interaction) throws Exception {
        MessageLite message = zmqMosaicInterface.createMessageLite(interaction);

        
        //publisher.send(String.format(interaction.getSenderId()), ZMQ.SNDMORE);
        //publisher.send(interaction.toString(), 0);
        // socketZeromqServer.addTrafficManagementCenter(interaction);
    }

}
