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



import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

import java.lang.Integer;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;


public class SocketZeromq extends AbstractOutputGenerator {

    // private final SocketZeromqServer // socketZeromqServer;
    private final ZContext context = new ZContext();
    private final Socket publisher = context.createSocket(SocketType.PUB);

    public SocketZeromq(Integer port) {
        String address = "tcp://127.0.0.1:" + port.toString();
        publisher.connect(address);
        publisher.setSndHWM(100000);
        System.out.println("Testestst");
    }

    private void zmqPublish(Socket publisher, MessageLite message, String topic){
        publisher.send(topic, ZMQ.SNDMORE);
        publisher.send(message.toByteArray(), 0);
    }

    private MessageLite returnMessage(ZUtilityMini utility){
        return utility.createZMessageLite();
    }

    private byte[] returnPubTopicByteArray(ZUtilityMini utility){
        return utility.createPubTopicByteArray();
    }

    @Handle
    public void messageInteraction(VehicleUpdates interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }

    @Handle
    public void messageInteraction(V2xMessageTransmission interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }

    @Handle
    public void messageInteraction(V2xMessageReception interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }

    @Handle
    public void messageInteraction(VehicleRegistration interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }

    @Handle
    public void messageInteraction(RsuRegistration interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }

    @Handle
    public void messageInteraction(TrafficLightRegistration interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }


    @Handle
    public void messageInteraction(ChargingStationRegistration interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }

    @Handle
    public void messageInteraction(ChargingStationUpdate interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }

    @Handle
    public void messageInteraction(TrafficDetectorUpdates interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }

    @Handle
    public void messageInteraction(TmcRegistration interaction) throws Exception {
        ZUtilityMini utility = new ZUtilityMini(interaction);
        utility.process(interaction);
        MessageLite message = returnMessage(utility);
        String topic = utility.createPubTopic();
        zmqPublish(publisher, message, topic);
    }

}
