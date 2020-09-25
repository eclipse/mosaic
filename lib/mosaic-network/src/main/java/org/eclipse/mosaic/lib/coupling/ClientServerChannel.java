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

package org.eclipse.mosaic.lib.coupling;

import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.CommandMessage;
import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.CommandMessage.CommandType;
import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.ConfigureRadioMessage;
import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.InitMessage;
import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.PortExchange;
import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.ReceiveMessage;
import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.SendMessageMessage;
import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.TimeMessage;
import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.UpdateNode;
import org.eclipse.mosaic.lib.coupling.ClientServerChannelProtos.UpdateNode.NodeData;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.geo.CartesianCircle;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoRectangle;
import org.eclipse.mosaic.lib.objects.addressing.DestinationAddressContainer;
import org.eclipse.mosaic.lib.objects.communication.AdHocConfiguration;
import org.eclipse.mosaic.lib.objects.communication.InterfaceConfiguration;
import org.eclipse.mosaic.lib.objects.v2x.V2xReceiverInformation;
import org.eclipse.mosaic.lib.util.objects.IdTransformer;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Abstraction of Ambassador->Federate Byte Protocol
 * for coupling of a network federate to MOSAIC.
 */
public class ClientServerChannel {

    public String getLastStatusMessage() {
        return lastStatusMessage;
    }

    public static final class CMD {
        /**
         * Undefined Message.
         */
        public final static int UNDEF = -1;

        /**
         * initialize the federate.
         */
        public final static int INIT = 1;

        /**
         * Stop simulation.
         */
        public final static int SHUT_DOWN = 4;

        /**
         * Update node properties.
         */
        public static final int UPDATE_NODE = 10;

        /**
         * Delete network nodes.
         */
        public static final int REMOVE_NODE = 11;

        /**
         * Advance simulation time.
         */
        public final static int ADVANCE_TIME = 20;

        /**
         * Scheduling request at the next event time.
         */
        public final static int NEXT_EVENT = 21;

        /**
         * A virtual node has received a message.
         */
        public final static int MSG_RECV = 22;

        /**
         * A virtual node has sent a message.
         */
        public static final int MSG_SEND = 30;

        /**
         * Configure radio.
         */
        public static final int CONF_RADIO = 31;

        /**
         * Termination of steps or lists.
         */
        public static final int END = 40;

        /**
         * Success message, returned by federate upon successful execution of command.
         */
        public final static int SUCCESS = 41;
    }

    /**
     * Allowed address types.
     */
    public static final class ADDRESSTYPE {

        /**
         * Topological address.
         */
        public final static int TOPOCAST = 1;

        /**
         * Geo address with circular shaped area.
         */
        public final static int GEOCIRCLE = 2;

        /**
         * Geo address with rectangular shaped area.
         */
        public final static int GEORECTANGLE = 3;
    }

    /**
     * Socket connected to the network federate.
     */
    public Socket socket;

    /**
     * Input stream from network federate.
     */
    final private InputStream in;

    /**
     * Output stream to network federate.
     */
    final private OutputStream out;

    /**
     * Logger
     *///TODO: implement usage
    final private Logger log;

    /**
     * Last message from the federate.
     */  //TODO: implement usage
    private String lastStatusMessage = "";

    /**
     * Constructor.
     *
     * @param host the remote host address as a String
     * @param port the remote port number
     * @param log  logger to log on
     * @throws IOException if the streams cannot be opened.
     */
    public ClientServerChannel(String host, int port, Logger log) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        this.in = new BufferedInputStream(socket.getInputStream());
        this.out = new BufferedOutputStream(socket.getOutputStream());
        this.log = log;
    }

    /**
     * Constructor.
     *
     * @param host the remote host address as an InetAddress
     * @param port the remote port number
     * @param log  logger to log on
     * @throws IOException if the streams cannot be opened.
     */
    public ClientServerChannel(InetAddress host, int port, Logger log) throws IOException {
        this.socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.log = log;
    }

    /**
     * Closes the channel.
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        this.socket.close();
    }

    //####################################################################
    //                  Reading methods
    //####################################################################

    private ReceiveMessage readMessage() throws IOException {
        return Validate.notNull(ReceiveMessage.parseDelimitedFrom(in), "Could not read message body.");
    }

    /**
     * Reads a message from the incoming channel.
     *
     * @return The read message.
     * @throws java.io.IOException
     */ //TODO: ChannelID (and length) not yet treated
    public ReceiveMessageContainer readMessage(IdTransformer<Integer, String> idTransformer) throws IOException {
        ReceiveMessage receiveMessage = this.readMessage();
        V2xReceiverInformation recInfo = new V2xReceiverInformation(receiveMessage.getTime()).signalStrength(receiveMessage.getRssi());
        return new ReceiveMessageContainer(
                receiveMessage.getTime(),
                idTransformer.fromExternalId(receiveMessage.getNodeId()),
                receiveMessage.getMessageId(), recInfo
        );
    }

    /**
     * Reads a port from the incoming stream.
     *
     * @return the read port as int
     * @throws java.io.IOException
     */
    public int readPortBody() throws IOException {
        PortExchange portExchange = Validate.notNull(PortExchange.parseDelimitedFrom(in), "Could not read port.");
        return portExchange.getPortNumber();
    }

    /**
     * Reads a time from the incoming stream.
     *
     * @return the read time as long
     * @throws java.io.IOException
     */
    public long readTimeBody() throws IOException {
        TimeMessage timeMessage = Validate.notNull(TimeMessage.parseDelimitedFrom(in), "Could not read time.");
        return timeMessage.getTime();
    }

    /**
     * Reads a single command from the input stream
     * blocking
     *
     * @return the read command
     * @throws java.io.IOException
     */
    public int readCommand() throws IOException {
        CommandMessage commandMessage = Validate.notNull(CommandMessage.parseDelimitedFrom(in), "Could not read command.");
        return ProtobufCMDToCMD(commandMessage.getCommandType());
    }

    //####################################################################
    //               Writing methods
    //####################################################################

    /**
     * Initialize scheduler with given times.
     *
     * @param startTime the first point in time simulated by the simulator
     * @param endTime   the last timestep simulated by the simulator
     * @return command returned by the federate
     * @throws IOException Communication error.
     */
    public int writeInitBody(long startTime, long endTime) throws IOException {
        writeCommand(CMD.INIT);                                     //Announce INIT message
        InitMessage.Builder initMessage = InitMessage.newBuilder(); //Builder for the protobuf message
        initMessage.setStartTime(startTime).setEndTime(endTime);    //Hand times to builder
        initMessage.build().writeDelimitedTo(out);                  //Build object and write it (delimited!) to stream
        return readCommand();                                       //Return the command that the federate sent as ack
    }

    /**
     * Command: Add nodes.
     *
     * @param time  time at which the node is added
     * @param nodes a list of ids and positions
     * @return command returned by the federate
     * @throws IOException Communication error.
     */
    public int writeAddNodeMessage(long time, List<NodeDataContainer> nodes) throws IOException {
        writeCommand(CMD.UPDATE_NODE);                                  //Announce UPDATE_NODE message
        UpdateNode.Builder updateNode = UpdateNode.newBuilder();        //Create builder
        updateNode.setUpdateType(UpdateNode.UpdateType.ADD_VEHICLE).setTime(time);  //Set the type of the update message
        for (NodeDataContainer cont : nodes) {                           //Fill the given nodes into the builder
            NodeData.Builder tmpBuilder = NodeData.newBuilder();        //Every node data is another protobuf object, thus new builder
            tmpBuilder.setId(cont.id).setX(cont.pos.getX()).setY(cont.pos.getY());  //Set coordinates
            updateNode.addProperties(tmpBuilder.build());               //Add node data to message
        }
        updateNode.build().writeDelimitedTo(out);                       //Build message and write to stream
        return readCommand();                                           //Read command (hopefully a success)
    }

    /**
     * Command: Add rsu nodes.
     *
     * @param time the time at which he RSU is added
     * @param rsus list of ids and positions
     * @return command returned by the federate
     * @throws IOException Communication error.
     */
    public int writeAddRsuNodeMessage(long time, List<NodeDataContainer> rsus) throws IOException {
        writeCommand(CMD.UPDATE_NODE);
        UpdateNode.Builder updateNode = UpdateNode.newBuilder();
        updateNode.setUpdateType(UpdateNode.UpdateType.ADD_RSU).setTime(time);
        for (NodeDataContainer cont : rsus) {
            NodeData.Builder tmpBuilder = NodeData.newBuilder();
            tmpBuilder.setId(cont.id).setX(cont.pos.getX()).setY(cont.pos.getY());
            updateNode.addProperties(tmpBuilder.build());
        }
        updateNode.build().writeDelimitedTo(out);

        return readCommand();
    }

    /**
     * Command: Update nodes.
     *
     * @param time  time at which the positions are updated
     * @param nodes a list of ids and positions
     * @return command returned by the federate
     * @throws IOException Communication error.
     */
    public int writeUpdatePositionsMessage(long time, List<NodeDataContainer> nodes) throws IOException {
        writeCommand(CMD.UPDATE_NODE);
        UpdateNode.Builder updateNode = UpdateNode.newBuilder();
        updateNode.setUpdateType(UpdateNode.UpdateType.MOVE_NODE).setTime(time);
        for (NodeDataContainer cont : nodes) {
            NodeData.Builder tmpBuilder = NodeData.newBuilder();
            tmpBuilder.setId(cont.id).setX(cont.pos.getX()).setY(cont.pos.getY());
            updateNode.addProperties(tmpBuilder.build());
        }
        updateNode.build().writeDelimitedTo(out);
        return readCommand();
    }

    /**
     * Command: Remove nodes.
     *
     * @param time time at which the nodes are removed
     * @param ids  list of IDs to remove
     * @return command returned by the federate
     * @throws IOException Communication error.
     */
    public int writeRemoveNodesMessage(long time, List<Integer> ids) throws IOException {
        writeCommand(CMD.UPDATE_NODE);
        UpdateNode.Builder updateNode = UpdateNode.newBuilder();
        updateNode.setUpdateType(UpdateNode.UpdateType.REMOVE_NODE).setTime(time);
        for (int id : ids) {
            NodeData.Builder tmpBuilder = NodeData.newBuilder();  //like add and move but coordinates are ignored
            tmpBuilder.setId(id).setX(0).setY(0);
            updateNode.addProperties(tmpBuilder.build());
        }
        updateNode.build().writeDelimitedTo(out);

        return readCommand();
    }

    // @param channelId the channelID               //TODO:make enum from
    /**
     * Write send message header to stream.
     * Not used in eWorld visualizer.
     *
     * @param time      simulation time at which the message is sent
     * @param srcNodeId ID of the sending node      //TODO: maybe make this an IP? -> nodes would have a bilinear mapping to IP addresses
     * @param msgId     the ID of the message
     * @param msgLength length of the message
     * @param dac       DestinationAddressContainer with the destination address of the sender and additional information
     * @return command returned by the federate
     * @throws IOException Communication error.
     */
    public int writeSendMessage(long time, int srcNodeId,
                                int msgId, long msgLength, DestinationAddressContainer dac) throws IOException {
        writeCommand(CMD.MSG_SEND);
        SendMessageMessage.Builder sendMess = SendMessageMessage.newBuilder();  //Add message details to the builder
        sendMess.setTime(time).setNodeId(srcNodeId).setChannelId(translateChannel(dac.getAdhocChannelId())).setMessageId(msgId).setLength(msgLength);

        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
        buffer.put(dac.getAddress().getIPv4Address().getAddress()); //make an int32 out of the byte array
        buffer.position(0);

        if (dac.isGeocast()) { //Geocasts
            if (dac.getGeoArea() instanceof GeoRectangle) {   //Rectangular area
                SendMessageMessage.GeoRectangleAddress.Builder rectangleAddress = SendMessageMessage.GeoRectangleAddress.newBuilder();
                //builder for rectangular addresses
                rectangleAddress.setIpAddress(buffer.getInt()); //write the ip address as flat integer into the builder
                //convert coordinates etc.
                CartesianRectangle projectedRectangle = ((GeoRectangle)dac.getGeoArea()).toCartesian();
                //write the coordinates of the area into the builder
                rectangleAddress.setAX(projectedRectangle.getA().getX());
                rectangleAddress.setAY(projectedRectangle.getA().getY());
                rectangleAddress.setBX(projectedRectangle.getB().getX());
                rectangleAddress.setBY(projectedRectangle.getB().getY());
                //add address to the message
                sendMess.setRectangleAddress(rectangleAddress);
            } else if (dac.getGeoArea() instanceof GeoCircle) {
                SendMessageMessage.GeoCircleAddress.Builder circleAddress = SendMessageMessage.GeoCircleAddress.newBuilder();
                circleAddress.setIpAddress(buffer.getInt());

                CartesianCircle projectedCircle = ((GeoCircle)dac.getGeoArea()).toCartesian();
                //write area into the address object
                circleAddress.setCenterX(projectedCircle.getCenter().getX());
                circleAddress.setCenterY(projectedCircle.getCenter().getY());
                circleAddress.setRadius(projectedCircle.getRadius());
                //add address to the message
                sendMess.setCircleAddress(circleAddress);
            } else {
                throw new IllegalArgumentException("Addressing does support GeoCircle and GeoRectangle only.");
            }
        } else if (dac.getTimeToLive() > -1){  //Topocast addresses
            SendMessageMessage.TopoAddress.Builder topoAddress = SendMessageMessage.TopoAddress.newBuilder();
            topoAddress.setIpAddress(buffer.getInt());  //Add IP as flat int
            topoAddress.setTtl(dac.getTimeToLive());    //add time to live
            sendMess.setTopoAddress(topoAddress);   //set address in message
        } //TODO: create else case and throw exception
        sendMess.build().writeDelimitedTo(out); //write message onto channel        
        return readCommand();
    }

    /**
     * Takes a configuration message and inserts it via the wrapped protobuf channel
     * Configuration is then sent to the federate.
     *
     * @param time          the logical time at which the configuration happens
     * @param msgID         the ID of the configuration message
     * @param externalId    the external (federate-internal) ID of the node
     * @param configuration the actual configuration
     * @return command returned by the federate
     * @throws IOException
     */
    public int writeConfigMessage(long time, int msgID, int externalId, AdHocConfiguration configuration) throws IOException {
        writeCommand(CMD.CONF_RADIO);
        ConfigureRadioMessage.Builder configRadio = ConfigureRadioMessage.newBuilder();
        configRadio.setTime(time).setMessageId(msgID).setExternalId(externalId);
        switch (configuration.getRadioMode()) {
            case OFF:
                configRadio.setRadioNumber(ConfigureRadioMessage.RadioNumber.NO_RADIO);
                break;
            case SINGLE:
                configRadio.setRadioNumber(ConfigureRadioMessage.RadioNumber.SINGLE_RADIO);
                break;
            case DUAL:
                configRadio.setRadioNumber(ConfigureRadioMessage.RadioNumber.DUAL_RADIO);
                break;
            default:
                throw new RuntimeException("Illegal number of radios in configuration: " + configuration.getRadioMode().toString());
        }
        if (configuration.getRadioMode() == AdHocConfiguration.RadioMode.SINGLE || configuration.getRadioMode() == AdHocConfiguration.RadioMode.DUAL) {
            ConfigureRadioMessage.RadioConfiguration.Builder radioConfig1 = ConfigureRadioMessage.RadioConfiguration.newBuilder();
            radioConfig1.setReceivingMessages(false);                                     //!!Semantic in Java: true -> only routing
            radioConfig1.setIpAddress(inet4ToInt(configuration.getConf0().getNewIP()));   //Semantic in federates: false -> only routing
            radioConfig1.setSubnetAddress(inet4ToInt(configuration.getConf0().getNewSubnet()));
            radioConfig1.setTransmissionPower(configuration.getConf0().getNewPower());
            radioConfig1.setPrimaryRadioChannel(translateChannel(configuration.getConf0().getChannel0()));
            if (configuration.getConf0().getMode() == InterfaceConfiguration.MultiChannelMode.ALTERNATING) {
                radioConfig1.setSecondaryRadioChannel(translateChannel(configuration.getConf0().getChannel1()));
                radioConfig1.setRadioMode(ConfigureRadioMessage.RadioConfiguration.RadioMode.DUAL_CHANNEL);
            } else {
                radioConfig1.setRadioMode(ConfigureRadioMessage.RadioConfiguration.RadioMode.SINGLE_CHANNEL);
            }
            configRadio.setPrimaryRadioConfiguration(radioConfig1);
        }
        if (configuration.getRadioMode() == AdHocConfiguration.RadioMode.DUAL) {
            ConfigureRadioMessage.RadioConfiguration.Builder radioConfig2 = ConfigureRadioMessage.RadioConfiguration.newBuilder();
            radioConfig2.setReceivingMessages(false); //!!Semantic in Java: true -> only routing
            radioConfig2.setIpAddress(inet4ToInt(configuration.getConf1().getNewIP()));   //Semantic in federates: false -> only routing
            radioConfig2.setSubnetAddress(inet4ToInt(configuration.getConf1().getNewSubnet()));
            radioConfig2.setTransmissionPower(configuration.getConf1().getNewPower());
            radioConfig2.setPrimaryRadioChannel(translateChannel(configuration.getConf1().getChannel0()));
            if (configuration.getConf1().getMode() == InterfaceConfiguration.MultiChannelMode.ALTERNATING) {
                radioConfig2.setSecondaryRadioChannel(translateChannel(configuration.getConf1().getChannel1()));
                radioConfig2.setRadioMode(ConfigureRadioMessage.RadioConfiguration.RadioMode.DUAL_CHANNEL);
            } else {
                radioConfig2.setRadioMode(ConfigureRadioMessage.RadioConfiguration.RadioMode.SINGLE_CHANNEL);
            }
            configRadio.setSecondaryRadioConfiguration(radioConfig2);
        }
        configRadio.build().writeDelimitedTo(out);
        return readCommand();
    }

    /**
     * Command: advance time.
     *
     * @param time point in time up to which advance is granted
     * @throws IOException Communication error.
     */
    public void writeAdvanceTimeMessage(long time) throws IOException {
        writeCommand(CMD.ADVANCE_TIME);
        TimeMessage.Builder timeMessage = TimeMessage.newBuilder();
        timeMessage.setTime(time);
        timeMessage.build().writeDelimitedTo(out);
    }

    /**
     * Write a command.
     *
     * @param cmd the command to write onto the channel
     * @throws IOException Communication error.
     */
    public void writeCommand(int cmd) throws IOException {
        CommandType protobufCMD = cmdToProtobufCMD(cmd);
        if (protobufCMD == CommandType.UNDEF) {
            return;
        }
        CommandMessage.Builder commandMessage = CommandMessage.newBuilder();
        commandMessage.setCommandType(protobufCMD);
        commandMessage.build().writeDelimitedTo(out);
    }

    //####################################################################
    //   Helper methods and classes
    //####################################################################

    /**
     * Converts a Inet4Address to an int.
     * TODO: make sure ints are handled bitwise when handing to protobuf
     *
     * @param ip The Inet4Address
     * @return an int representing the IP address
     */
    private int inet4ToInt(Inet4Address ip) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
        buffer.put(ip.getAddress());
        buffer.position(0);
        return buffer.getInt();
    }

    private int ProtobufCMDToCMD(CommandType protoCMD) {
        switch (protoCMD) {
            case INIT:
                return CMD.INIT;
            case SHUT_DOWN:
                return CMD.SHUT_DOWN;

            case UPDATE_NODE:
                return CMD.UPDATE_NODE;
            case REMOVE_NODE:
                return CMD.REMOVE_NODE;

            case ADVANCE_TIME:
                return CMD.ADVANCE_TIME;
            case NEXT_EVENT:
                return CMD.NEXT_EVENT;
            case MSG_RECV:
                return CMD.MSG_RECV;

            case MSG_SEND:
                return CMD.MSG_SEND;
            case CONF_RADIO:
                return CMD.CONF_RADIO;

            case END:
                return CMD.END;
            case SUCCESS:
                return CMD.SUCCESS;
            default:
                return CMD.UNDEF;
        }
    }

    private CommandType cmdToProtobufCMD(int cmd) {
        switch (cmd) {
            case CMD.INIT:
                return CommandType.INIT;
            case CMD.SHUT_DOWN:
                return CommandType.SHUT_DOWN;

            case CMD.UPDATE_NODE:
                return CommandType.UPDATE_NODE;
            case CMD.REMOVE_NODE:
                return CommandType.REMOVE_NODE;

            case CMD.ADVANCE_TIME:
                return CommandType.ADVANCE_TIME;
            case CMD.NEXT_EVENT:
                return CommandType.NEXT_EVENT;
            case CMD.MSG_RECV:
                return CommandType.MSG_RECV;

            case CMD.MSG_SEND:
                return CommandType.MSG_SEND;
            case CMD.CONF_RADIO:
                return CommandType.CONF_RADIO;

            case CMD.END:
                return CommandType.END;
            case CMD.SUCCESS:
                return CommandType.SUCCESS;
            default:
                return CommandType.UNDEF;
        }
    }

    /**
     * Returns the corresponding {@link AdHocChannel} type to a given Protobuf channel enum type.
     *
     * @param channel the internal channel object
     * @return the protobuf-channel object
     */
    private ClientServerChannelProtos.RadioChannel translateChannel(AdHocChannel channel) {
        switch (channel) {
            case SCH1:
                return ClientServerChannelProtos.RadioChannel.PROTO_SCH1;
            case SCH2:
                return ClientServerChannelProtos.RadioChannel.PROTO_SCH2;
            case SCH3:
                return ClientServerChannelProtos.RadioChannel.PROTO_SCH3;
            case CCH:
                return ClientServerChannelProtos.RadioChannel.PROTO_CCH;
            case SCH4:
                return ClientServerChannelProtos.RadioChannel.PROTO_SCH4;
            case SCH5:
                return ClientServerChannelProtos.RadioChannel.PROTO_SCH5;
            case SCH6:
                return ClientServerChannelProtos.RadioChannel.PROTO_SCH6;
            default:
                throw new RuntimeException("Channel " + channel.toString() + " does not exist in MOSAIC");
        }
    }

    public static class NodeDataContainer {
        public final int id;
        public final CartesianPoint pos;

        public NodeDataContainer(int id, CartesianPoint pos) {
            this.id = id;
            this.pos = pos;
        }
    }

    public static class ReceiveMessageContainer {

        public final long time;
        public final String receiverName;
        public final int msgId;
        public final V2xReceiverInformation receiverInformation;

        public ReceiveMessageContainer(final long time, @Nonnull final String receiverName, final int msgId,
                                       @Nonnull final V2xReceiverInformation receiverInformation) {

            this.time = time;
            this.receiverName = receiverName;
            this.msgId = msgId;
            this.receiverInformation = receiverInformation;
        }
    }
}
