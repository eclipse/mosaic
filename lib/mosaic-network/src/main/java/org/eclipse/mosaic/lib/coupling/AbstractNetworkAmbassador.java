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

import org.eclipse.mosaic.interactions.communication.AdHocCommunicationConfiguration;
import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.coupling.ClientServerChannel.CMD;
import org.eclipse.mosaic.lib.coupling.ClientServerChannel.NodeDataContainer;
import org.eclipse.mosaic.lib.coupling.ClientServerChannel.ReceiveMessageContainer;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.UnitData;
import org.eclipse.mosaic.lib.objects.UnitNameComparator;
import org.eclipse.mosaic.lib.objects.addressing.DestinationAddressContainer;
import org.eclipse.mosaic.lib.objects.addressing.SourceAddressContainer;
import org.eclipse.mosaic.lib.objects.communication.AdHocConfiguration;
import org.eclipse.mosaic.lib.objects.mapping.RsuMapping;
import org.eclipse.mosaic.lib.objects.mapping.VehicleMapping;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.rti.api.AbstractFederateAmbassador;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.federatestarter.DockerFederateExecutor;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * The Ambassador for coupling a network simulator to MOSAIC RTI.
 */
public abstract class AbstractNetworkAmbassador extends AbstractFederateAmbassador {

    protected DockerFederateExecutor dockerFederateExecutor = null;

    private final static class VirtualNodeContainer {

        private AdHocCommunicationConfiguration configAdHoc;
        public CartesianPoint position;

        private VirtualNodeContainer(AdHocCommunicationConfiguration configAdHoc, CartesianPoint position) {
            this.configAdHoc = configAdHoc;
            this.position = position;
        }
    }

    /**
     * channel creating the abstraction of byte protocol for network federate.
     * This channel is for communication from the ambassador to the federate
     */
    ClientServerChannel ambassadorFederateChannel;

    /**
     * channel creating the abstraction of byte protocol for network federate.
     * This channel is for communication from the federate to the ambassador
     */
    ClientServerChannel federateAmbassadorChannel;

    /**
     * List of new virtual vehicles, which are only added when they have a position and enabled AdHoc Communication.
     * For a vehicle to be added, the ambassador must receive the following interactions:
     * 1. {@link VehicleRegistration}
     * 2. {@link VehicleUpdates}
     * 3. {@link AdHocCommunicationConfiguration}
     * The Add-message is expected to arrive first, then either the AdHoc configuration or the movement message.
     */
    private final Map<String, VirtualNodeContainer> newVirtualVehicles;

    /**
     * List of RSUs for which there was no configuration interaction yet.
     * For an RSU to be added, the ambassador must receive the following interactions:
     * 1. {@link RsuRegistration}
     * 2. {@link AdHocCommunicationConfiguration}
     * The registration interaction is expected to arrive first, then the AdHoc configuration
     */
    private final Map<String, VirtualNodeContainer> newVirtualRsus;

    /**
     * Holds a BiMap of internal (mosaic) and external (federate) unit ID's ({@code BiMap<String, Integer>}).
     * If simulated entity is in this map, the entity is present within the simulator.
     */
    private final NetworkEntityIdTransformer idTransformer;

    /**
     * The actual ambassadors name.
     */
    private final String ambassadorName;

    /**
     * The actual federates name.
     */
    private final String federateName;

    /**
     * A config object for whether to bypass federate destination type capability queries in
     * {@link #receiveTypedInteraction(V2xMessageTransmission interaction)} if needed.
     */
    protected CAbstractNetworkAmbassador config;

    /**
     * Creates a new AbstractNetworkAmbassador.
     *
     * @param ambassadorParameter parameters to configure the ambassador
     * @param ambassadorName      ambassador identifier
     * @param federateName        federate identifier
     */
    protected AbstractNetworkAmbassador(AmbassadorParameter ambassadorParameter, String ambassadorName, String federateName) {
        super(ambassadorParameter);
        this.ambassadorName = ambassadorName;
        this.federateName = federateName;
        this.newVirtualVehicles = new HashMap<>();
        this.newVirtualRsus = new HashMap<>();
        this.idTransformer = new NetworkEntityIdTransformer();

        try {
            config = new ObjectInstantiation<>(CAbstractNetworkAmbassador.class).readFile(ambassadorParameter.configuration);
        } catch (InstantiationException | NullPointerException e) {
            log.warn("Could not read ambassador configuration in '{}'. Using default one instead.", ambassadorParameter.configuration);
            config = new CAbstractNetworkAmbassador();
        }
    }

    @Override
    public void connectToFederate(String host, InputStream in, InputStream err) throws InternalFederateException {
        try {
            final Scanner outputScanner = new Scanner(in);
            final String outPortPattern = "OutPort=\\d{1,5}";
            final String errorPattern = ".*Error:.*";
            String matchedOutPort;
            String matchedError = null;

            while ((matchedOutPort = outputScanner.findInLine(outPortPattern)) == null
                    && (matchedError = outputScanner.findInLine(errorPattern)) == null) {
                this.log.trace(outputScanner.nextLine());
            }

            // do not close outputScanner, as it would close the underlying stream.

            if (matchedOutPort != null) {
                log.debug("Found string \"{}\" in stdout", matchedOutPort);
                int port = Integer.parseInt(matchedOutPort.split("=")[1]);
                port = getHostPortFromDockerPort(port);
                this.connectToFederate(host, port); // Connection with the read port
            } else {
                log.error(matchedError);
                throw new InternalFederateException("Found error message in federate output while connecting: \n" + matchedError);
            }
        } catch (NumberFormatException ex) {
            throw new InternalFederateException("Could not parse port number output by federate", ex);
        } catch (IllegalStateException ex) {
            throw new InternalFederateException("Regex scanner was closed unexpectedly in connectToFederate", ex);
        } catch (NoSuchElementException ex) {
            throw new InternalFederateException("Could not find OutPort or error message in federate output", ex);
        }

        if (federateAmbassadorChannel == null || ambassadorFederateChannel == null) {
            throw new InternalFederateException("Could not establish connection to federate. The federate may not have started properly.");
        }

        log.debug("{} finished ConnectToFederate", ambassadorName);
    }

    /**
     * Connects the incoming channel with the federate, waits for INIT message and a port number,
     * connects the outgoing channel to the received port number.
     * <br>
     * This method is called by the federation management service
     *
     * @param host host on which the federate is listening
     * @param port port on which the federate is listening
     */
    @Override
    public void connectToFederate(String host, int port) {
        try {   // Connect to the network federate for reading
            this.federateAmbassadorChannel = new ClientServerChannel(host, port, log);
            this.log.info("Connected to {} for reading on port {}", federateName, port);
        } catch (UnknownHostException ex) {
            this.log.error("Unknown host: " + ex.toString());
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            this.log.error(ex.toString());
            throw new RuntimeException(ex);
        }
        try { // Read the initial command and the port number to connect incoming channel
            int cmd = this.federateAmbassadorChannel.readCommand();
            if (cmd == CMD.INIT) {
                // This is the port the federate listens on for the second channel
                int remotePort = this.federateAmbassadorChannel.readPortBody();
                remotePort = getHostPortFromDockerPort(remotePort);
                // Connect the second channel
                ambassadorFederateChannel = new ClientServerChannel(federateAmbassadorChannel.socket.getInetAddress(), remotePort, log);
                this.log.info("Connected to {} for commands on port {}", federateName, remotePort);
            } else {
                throw new RuntimeException("Could not connect to federate. Federate response is " + cmd);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not connect " + ambassadorName + " to " + federateName, e);
        }
    }

    /**
     * Since docker requires a binding of ports between container and host, we need to translate the ports used in
     * the container to the ports provided by the docker engine.
     *
     * @param port the container port
     * @return Returns, if a dockerFederateExecutor is set, the host port which is connected to the container port.
     *         Otherwise, returns the given port.
     */
    private int getHostPortFromDockerPort(int port) {
        if (dockerFederateExecutor != null && dockerFederateExecutor.getRunningContainer() != null) {
            for (Pair<Integer, Integer> binding : dockerFederateExecutor.getRunningContainer().getPortBindings()) {
                if (binding.getRight() == port) {
                    return binding.getLeft();
                }
            }
        }
        return port;
    }

    @Override
    public void initialize(long startTime, long endTime) throws InternalFederateException {
        super.initialize(startTime, endTime);   // Set times in the super class
        try {
            // Handshake:(1) Ambassador sends INIT (2) Ambassador sends times, (3)federate sends SUCCESS
            if (CMD.SUCCESS != this.ambassadorFederateChannel.writeInitBody(startTime, endTime)) {
                this.log.error("Could not initialize: " + this.federateAmbassadorChannel.getLastStatusMessage());
                throw new InternalFederateException(
                        "Error in " + this.federateName + ": " + this.federateAmbassadorChannel.getLastStatusMessage()
                );
            }
            this.log.info("Init simulation with startTime={}, stopTime={}", startTime, endTime);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize " + ambassadorName, e);
        }
    }

    // HEA: Add priorities here?
    @Override
    protected void processInteraction(Interaction interaction) throws InternalFederateException {
        this.log.debug("ProcessInteraction {} at time={}", interaction.getTypeId(), interaction.getTime());
        // 2nd step of time management cycle (deliver interactions to the federate)
        if (interaction.getTypeId().equals(VehicleRegistration.TYPE_ID)) {
            this.receiveTypedInteraction((VehicleRegistration) interaction);
        } else if (interaction.getTypeId().equals(RsuRegistration.TYPE_ID)) {
            this.receiveTypedInteraction((RsuRegistration) interaction);
        } else if (interaction.getTypeId().equals(TrafficLightRegistration.TYPE_ID)) {
            this.receiveTypedInteraction((TrafficLightRegistration) interaction);
        } else if (interaction.getTypeId().equals(VehicleUpdates.TYPE_ID)) {
            this.receiveTypedInteraction((VehicleUpdates) interaction);
        } else if (interaction.getTypeId().equals(V2xMessageTransmission.TYPE_ID)) {
            this.receiveTypedInteraction((V2xMessageTransmission) interaction);
        } else if (interaction.getTypeId().equals(AdHocCommunicationConfiguration.TYPE_ID)) {
            this.receiveTypedInteraction((AdHocCommunicationConfiguration) interaction);
        }
    }

    @Override
    protected void processTimeAdvanceGrant(long time) throws InternalFederateException {
        this.log.debug("ProcessTimeAdvanceGrant at time={}", time);
        try {
            // Last step of cycle (allow events up to current time in network simulator scheduler)
            ambassadorFederateChannel.writeAdvanceTimeMessage(time);
            // Wait until next event request to start time management cycle
            // read while end of step is signalled
            command_loop:
            while (true) { // While the federate is advancing time we are receiving messages from it
                log.trace("Reading Command in TimeAdvanceGrant");
                int cmd = this.federateAmbassadorChannel.readCommand(); // Which message does the federate send?
                switch (cmd) {
                    case CMD.NEXT_EVENT: // The federate has scheduled an event
                        long nextTime = this.federateAmbassadorChannel.readTimeBody();
                        log.debug("Requested next_event at {} ", nextTime);
                        // If the federates event is beyond our allowed time we have to request time advance from the RTI
                        if (nextTime > time) {
                            this.rti.requestAdvanceTime(nextTime);
                        }
                        break;
                    case CMD.MSG_RECV:  // A simulated node has received a V2X message
                        ReceiveMessageContainer rcvMsgContainer = this.federateAmbassadorChannel.readMessage(idTransformer);
                        // read message body
                        // The receiver may have been removed from the simulation while message was on air
                        if (rcvMsgContainer.receiverName != null) {
                            V2xMessageReception msg = new V2xMessageReception(
                                    rcvMsgContainer.time,
                                    rcvMsgContainer.receiverName,
                                    rcvMsgContainer.msgId,
                                    rcvMsgContainer.receiverInformation
                            );
                            log.info("Receive V2XMessage : Id({}) on Node {} at Time={}", msg.getMessageId(), msg.getReceiverName(), msg.getTime());
                            this.rti.triggerInteraction(msg);  // Hand the received message to the RTI and thus the other federates
                        }
                        break;
                    case CMD.END:       // The federate has terminated the current time advance -> we are done here
                        long termTime = federateAmbassadorChannel.readTimeBody();
                        log.debug("End ProcessTimeAdvanceGrant at: {}", termTime);
                        break command_loop; // break out of the infinite loop
                    default:
                        throw new InternalFederateException("Unknown command from federate at processTimeAdvanceGrant");
                }
            }
        } catch (IOException | IllegalValueException | InternalFederateException e) {
            throw new InternalFederateException(e);
        }
    }

    @Override
    public void finishSimulation() throws InternalFederateException {
        try {
            this.ambassadorFederateChannel.writeCommand(CMD.SHUT_DOWN);
            this.ambassadorFederateChannel.close();
            this.federateAmbassadorChannel.close();
        } catch (IOException e) {
            this.log.error("Could not close socket.");
            throw new InternalFederateException(e);
        }
        log.info("Finished simulation");
    }

    /**
     * Store nodes for later adding based on received vehicle mappings.
     * <br>
     * The unique mapping of RTI string-ids to federate integer-ids
     * is also done in the handling of the vehicle movements
     *
     * @param interaction interaction containing a mapping of added vehicles
     */
    private synchronized void receiveTypedInteraction(VehicleRegistration interaction) {
        this.log.debug(
                "Received VehicleRegistration for vehicle {} at simulation time {} ",
                interaction.getMapping().getName(),
                interaction.getTime()
        );
        VehicleMapping av = interaction.getMapping();
        // We have got this Vehicle already
        if (idTransformer.containsInternalId(av.getName()) || newVirtualVehicles.containsKey(av.getName())) {
            this.log.warn("A vehicle with ID {} was already added. Ignoring message.", av.getName());
            return;
        }
        // Add new virtual vehicle-container without config message or position
        newVirtualVehicles.put(av.getName(), new VirtualNodeContainer(null, null));
    }

    /**
     * Add nodes based on received rsu mappings.
     *
     * @param interaction interaction containing a mapping of added rsu
     */
    private synchronized void receiveTypedInteraction(RsuRegistration interaction) {
        this.log.debug(
                "Received AddedRSU for RSU {} at simulation time {} ",
                interaction.getMapping().getName(),
                interaction.getTime()
        );
        RsuMapping ar = interaction.getMapping();
        if (idTransformer.containsInternalId(ar.getName()) || newVirtualRsus.containsKey(ar.getName())) {  // We have got this RSU already
            this.log.warn("A RSU with ID {} was already added. Ignoring message.", ar.getName());
            return;
        }
        // Put the new RSU into our list of virtually added RSUs with no AdHoc configuration yet
        newVirtualRsus.put(ar.getName(), new VirtualNodeContainer(null, ar.getPosition().toCartesian()));
    }

    /**
     * Add nodes based on received traffic light mappings.
     * The method checks if the TLs are already present.
     * If not, the positions are converted and a list of TLs and their positions is kept for later adding if an AdHocMessage is received.
     *
     * @param interaction interaction containing a mapping of added traffic light
     */
    private synchronized void receiveTypedInteraction(TrafficLightRegistration interaction) {
        this.log.debug(
                "Add traffic light RSU for TL {} at simulation time {} ",
                interaction.getMapping().getName(),
                interaction.getTime()
        );
        // ApplicationTrafficLight at = msg.getApplicationTrafficLight();
        TrafficLightGroup group = interaction.getTrafficLightGroup();
        // We have got this TL already
        if (idTransformer.containsInternalId(group.getGroupId()) || newVirtualRsus.containsKey(group.getGroupId())) {
            this.log.warn("A TL with ID {} was already added. Ignoring message.", group.getGroupId());
            return;
        }
        // Put the new TL RSU into our list of virtually added RSUs with no AdHoc configuration yet
        newVirtualRsus.put(group.getGroupId(), new VirtualNodeContainer(null, group.getFirstPosition().toCartesian()));
    }

    /**
     * 1) Adds Vehicles on their first movement
     * 2) Updates node positions based on received vehicle movements.
     * 3) Removes nodes from the simulation
     * <br>
     * In the first case vehicles whose first movement is simulated will be added to the simulation in the federate.
     * The vehicles that are added will be sorted and verified to be also listed in the list of virtual vehicles.
     * If so, an external ID is generated for every vehicle and the id is added to the BiMap idMap. This map contains all simulated
     * vehicles and their corresponding external ID.
     * Next the vehicles position is converted and the vehicle is added to the list, which will be sent to the federate.
     * <br>
     * Second case: Vehicles moved, so it is checked if they are currently simulated and if so their new positions are converted.
     * All positions are then put into a list and handed to the channel for sending to the federate.
     * <br>
     * If vehicles shall be removed, they are verified to be simulated, their IDs are handed to the federate and are
     * erased from the idMap.
     *
     * @param interaction interaction containing vehicle movements
     * @throws InternalFederateException thrown when nodes could not be updated
     */
    private synchronized void receiveTypedInteraction(VehicleUpdates interaction) throws InternalFederateException {
        try {
            // Process VehicleRegistrations in VehicleUpdates interaction
            // Sort vehicles alphanumerical just in case there are multiple ones in the msg
            // (this helps for a better human-readable mapping of RTI string-ids and federate int-ids)
            if (!interaction.getAdded().isEmpty()) {
                log.debug("Add Vehicles at first movement");
                List<VehicleData> addedVehicles = interaction.getAdded();
                Comparator<UnitData> comp = new UnitNameComparator();
                addedVehicles.sort(comp);
                for (VehicleData vi : addedVehicles) {
                    if (idTransformer.containsInternalId(vi.getName())) {
                        log.warn("Vehicle with ID {} was already added, ignoring entry.", vi.getName());
                        continue;
                    } else if (!newVirtualVehicles.containsKey(vi.getName())) {
                        log.warn("Vehicle with ID {} is not in the virtual list, ignoring entry.", vi.getName());
                        continue;
                    }
                    VirtualNodeContainer nc = newVirtualVehicles.get(vi.getName());
                    nc.position = vi.getProjectedPosition();
                    if (nc.configAdHoc != null) {
                        addVehicleToSimulation(vi.getName(), interaction.getTime());
                    } else {
                        log.debug("Saving vehicle for later insertion as no AdHoc is configuration present");
                    }
                }
            }

            // Process UpdatePositions in VehicleUpdates interaction
            if (!interaction.getUpdated().isEmpty()) {
                this.log.debug("Update vehicle positions.");
                long time = interaction.getTime();
                List<VehicleData> nodes = interaction.getUpdated();
                List<NodeDataContainer> nodesToUpdate = new ArrayList<>();
                for (VehicleData vi : nodes) {
                    GeoPoint geoPosition = vi.getPosition();
                    CartesianPoint projectedPosition = vi.getProjectedPosition();
                    if (idTransformer.containsInternalId(vi.getName())) { // if the vehicle is already present in the simulation
                        Integer id = idTransformer.toExternalId(vi.getName());
                        if (this.log.isDebugEnabled()) {
                            log.debug("UpdateNode : ID[int={}, ext={}]", vi.getName(), id);
                            log.debug("Pos: x({}) y({}) Geo: {}", projectedPosition.getX(), projectedPosition.getY(), geoPosition);
                        }
                        nodesToUpdate.add(new NodeDataContainer(id, projectedPosition));
                    } else if (newVirtualVehicles.containsKey(vi.getName())) {
                        // Node was not yet added to simulation, so update its entry in the virtual node list
                        newVirtualVehicles.get(vi.getName()).position = projectedPosition;
                        if (this.log.isDebugEnabled()) {
                            log.debug("UpdateNode (still virtual) : ID[int={}]", vi.getName());
                            log.debug("Pos: x({}) y({}) Point2D.Double: {}", projectedPosition.getX(), projectedPosition.getY(), geoPosition);
                        }
                    } else {
                        this.log.warn("Node ID[int={}] is not simulated", vi.getName());
                    }
                }
                if (CMD.SUCCESS != this.ambassadorFederateChannel.writeUpdatePositionsMessage(time, nodesToUpdate)) {
                    LoggerFactory.getLogger(this.getClass()).error(
                            "Could not update nodes: " + this.federateAmbassadorChannel.getLastStatusMessage()
                    );
                    throw new InternalFederateException("Could not update nodes: " + this.federateAmbassadorChannel.getLastStatusMessage());
                }
            }

            // Process RemoveVehicles in VehicleUpdates interaction
            if (!interaction.getRemovedNames().isEmpty()) {
                this.log.debug("Remove Vehicles");
                List<Integer> nodesToRemove = new ArrayList<>();
                long time = interaction.getTime();
                for (String id : interaction.getRemovedNames()) {
                    // verify the vehicles are simulated in the current simulation
                    Integer externalId = idTransformer.containsInternalId(id) ? idTransformer.toExternalId(id) : null;
                    if (externalId != null) {
                        this.log.info("removeNode ID[int={}, ext={}] time={}", id, idTransformer.toExternalId(id), time);
                        nodesToRemove.add(externalId); // If simulated, add to the list, which will be handed to the channel
                        idTransformer.removeUsingInternalId(id); // remove the vehicle from our internal list
                    } else if (newVirtualVehicles.containsKey(id)) {
                        this.log.info("removeNode (still virtual) ID[int={}] time={}", id, time);
                        newVirtualVehicles.remove(id);
                    } else {
                        this.log.warn("Node ID[int={}] is not simulated", id);
                    }
                }
                // send data to federate and wait for ack
                if (CMD.SUCCESS != this.ambassadorFederateChannel.writeRemoveNodesMessage(time, nodesToRemove)) {
                    throw new InternalFederateException(
                            "Could not remove vehicles: " + this.federateAmbassadorChannel.getLastStatusMessage()
                    );
                }
                this.log.debug("Movements were successfully handed to the federate");
            }
        } catch (IOException | InternalFederateException e) {
            this.log.error(e.getMessage(), e);
            throw new InternalFederateException("Could not update positions or remove vehicles.", e);
        }
    }

    /**
     * Insert a V2X message based on received {@link V2xMessageTransmission} interaction.
     *
     * @param interaction interaction containing a V2X message
     */
    private synchronized void receiveTypedInteraction(V2xMessageTransmission interaction) throws InternalFederateException {
        final SourceAddressContainer sac = interaction.getMessage().getRouting().getSource();
        final DestinationAddressContainer dac = interaction.getMessage().getRouting().getDestination();

        if (!config.isRoutingTypeSupported(dac.getType())) {
            log.warn(
                    "This V2XMessage requires a destination type ({}) currently not supported by this network simulator."
                            + " Skip this message. Sender={}, Receiver={}, V2XMessage.id={}",
                    dac.getType().toString(),
                    sac.getSourceName(),
                    dac.getAddress().toString(),
                    interaction.getMessage().getId()
            );
            return;
        }
        if (!config.isAddressTypeSupported(dac.getAddress())) {
            log.warn(
                    "This V2XMessage requires a routing scheme currently not supported by this network simulator."
                            + " Skip this message. V2XMessage.id={}",
                    interaction.getMessage().getId()
            );
            return;
        }
        if (!config.isProtocolSupported(dac.getProtocolType())) {
            log.warn(
                    "This V2XMessage requires a transport protocol ({})"
                            + " currently not supported by this network simulator. Skip this message. V2XMessage.id={}",
                    dac.getProtocolType().toString(),
                    interaction.getMessage().getId()
            );
            return;
        }
        log.debug("This V2XMessage is applicable for this network simulator. Send this message. V2XMessage.id={}", interaction.getMessage().getId());

        try {
            Integer sourceId = idTransformer.containsInternalId(sac.getSourceName())
                    ? idTransformer.toExternalId(sac.getSourceName())
                    : null;

            if (sourceId != null) {
                log.info(
                        "insertV2XMessage: id={} from node ID[int={} , ext={}] channel:{} time={}",
                        interaction.getMessageId(),
                        sac.getSourceName(), sourceId, dac.getAdhocChannelId(), interaction.getTime()
                ); // Write the message onto the channel and to the federate
                // Then wait for ack
                int ack = ambassadorFederateChannel.writeSendMessage(
                        interaction.getTime(),
                        sourceId,
                        interaction.getMessage().getId(),
                        interaction.getMessage().getPayLoad().getEffectiveLength(),
                        dac
                );
                if (CMD.SUCCESS != ack) {
                    this.log.error(
                            "Could not insert V2X message into network: {}",
                            this.federateAmbassadorChannel.getLastStatusMessage()
                    );
                    throw new InternalFederateException(
                            "Error in " + this.federateName + this.federateAmbassadorChannel.getLastStatusMessage()
                    );
                }
            } else {
                throw new IllegalValueException("Node not simulated: " + sac.getSourceName());
            }
        } catch (IOException | InternalFederateException | IllegalValueException e) {
            this.log.error(e.getMessage());
            throw new InternalFederateException("Could not insert V2X message into network.", e);
        }
    }

    /**
     * Receive an {@link AdHocCommunicationConfiguration} and send it to the federate if the corresponding node is simulated.
     * If the node is not simulated (only added but did not move yet) the configuration interaction will be saved for later.
     *
     * @param interaction the AdHoc configuration interaction
     */
    protected synchronized void receiveTypedInteraction(AdHocCommunicationConfiguration interaction) throws InternalFederateException {
        final String nodeId = interaction.getConfiguration().getNodeId();
        log.debug("Received AdHoc configuration for node {}", interaction.getConfiguration().getNodeId());
        if (idTransformer.containsInternalId(nodeId)) { // node is simulated -> send interaction
            log.debug("Sending Configuration now");
            sendAdHocCommunicationConfiguration(interaction, interaction.getTime());
        } else if (newVirtualVehicles.containsKey(nodeId)) {
            VirtualNodeContainer nc = newVirtualVehicles.get(nodeId);
            nc.configAdHoc = interaction;
            if (nc.position != null) { // If we now have AdHoc config and position add the vehicle
                addVehicleToSimulation(nodeId, interaction.getTime());
            } else {
                log.debug("Saving Configuration for later insertion as vehicle has not moved yet");
            }
        } else if (newVirtualRsus.containsKey(nodeId)) {
            newVirtualRsus.get(nodeId).configAdHoc = interaction;
            addRsuToSimulation(nodeId, interaction.getTime()); // RSUs get a position and add time, so we can add it now
        } else {
            log.warn("Got AdHoc configuration for a node neither already simulated nor only added. Ignoring.");
        }
    }

    private synchronized void addRsuToSimulation(String nodeId, long time) throws InternalFederateException {
        List<NodeDataContainer> nodesToAdd = new ArrayList<>(); // Prepare a list for adding multiple RSUs
        try {
            VirtualNodeContainer nc = newVirtualRsus.get(nodeId);
            if (nc == null || nc.configAdHoc == null || nc.position == null) {
                log.warn("Could not add RSU {} as it has incomplete information (AdHoc config or position)", nodeId);
                return;
            }
            log.debug("Inserting RSU {} into simulation at position {} ", nodeId, nc.position);

            if (idTransformer.containsInternalId(nodeId)) {
                this.log.warn("RSU with id (internal={}) couldn't be added: name already exists ", nodeId);
            } else {
                int id = idTransformer.toExternalId(nodeId);
                nodesToAdd.add(new NodeDataContainer(id, nc.position));  // Add TL to the list
                // Let channel send list and get an acknowledgement
                if (CMD.SUCCESS != ambassadorFederateChannel.writeAddRsuNodeMessage(time, nodesToAdd)) {
                    this.log.error("Could not add new RSU: {}", this.federateAmbassadorChannel.getLastStatusMessage());
                    throw new InternalFederateException(
                            "Error in " + this.federateName + ": " + this.federateAmbassadorChannel.getLastStatusMessage()
                    );
                }
                this.log.info(
                        "Added RSU ID[int= {}, ext={}] at projected position= {} time={}",
                        idTransformer.fromExternalId(id), id, nc.position, time
                );
                log.debug("Sending saved AdHocCommunicationConfiguration message");
                sendAdHocCommunicationConfiguration(nc.configAdHoc, time);
            }
        } catch (IOException | InternalFederateException e) {
            this.log.error(e.getMessage(), e);
            throw new InternalFederateException("Could not add new rsu.", e);
        }
    }

    private synchronized void addVehicleToSimulation(String nodeId, long time) throws InternalFederateException {
        List<NodeDataContainer> nodesToAdd = new ArrayList<>();
        try {
            VirtualNodeContainer nc = newVirtualVehicles.get(nodeId);
            if (nc == null || nc.configAdHoc == null || nc.position == null) {
                log.warn("Could not add Vehicle {} as it has incomplete information (AdHoc config or position)", nodeId);
                return;
            }
            log.debug("Inserting vehicle into simulation at position {} ", nc.position);

            if (idTransformer.containsInternalId(nodeId)) {
                this.log.warn("Vehicle with id(int={}) couldn't be added: name already exists ", nodeId);
                return;
            } else {
                int id = idTransformer.toExternalId(nodeId);
                nodesToAdd.add(new NodeDataContainer(id, nc.position));
                this.log.info(
                        "Adding vehicle ID[int={}, ext={}] at projected_position={} time={}",
                        idTransformer.fromExternalId(id), id, nc.position, time
                );
            }
            this.newVirtualVehicles.remove(nodeId);

            if (CMD.SUCCESS != ambassadorFederateChannel.writeAddNodeMessage(time, nodesToAdd)) {
                this.log.error("Could not add new vehicles: {}", this.federateAmbassadorChannel.getLastStatusMessage());
                throw new InternalFederateException(
                        "Error in " + this.federateName + ": " + this.federateAmbassadorChannel.getLastStatusMessage()
                );
            }
            this.log.info("All vehicles were successfully added");
            log.debug("Sending a saved AdHocCommunicationConfiguration message...");
            sendAdHocCommunicationConfiguration(nc.configAdHoc, time);
        } catch (IOException | InternalFederateException e) {
            this.log.error(e.getMessage(), e);
            throw new InternalFederateException("Could not add new vehicle.", e);
        }
    }

    /**
     * Send a configuration interaction to the vehicle.
     * <br>
     * Hands the configuration interaction data to the channel and logs the event
     *
     * @param interaction interaction containing an AdHocConfiguration
     * @param time        workaround for wrong timestamps when retaining configuration interactions
     */
    private synchronized void sendAdHocCommunicationConfiguration(AdHocCommunicationConfiguration interaction, long time) {
        this.log.debug("Sending radio configuration interaction {} to {}", interaction.getId(), federateName);
        try {
            int interactionId = interaction.getId();
            AdHocConfiguration configuration = interaction.getConfiguration();
            Integer externalId = idTransformer.containsInternalId(configuration.getNodeId())
                    ? idTransformer.toExternalId(configuration.getNodeId())
                    : null;
            if (externalId != null) {   // If the node is simulated
                if (this.log.isDebugEnabled()) {
                    log.debug(
                            "AdHocCommunicationConfiguration: from node ID[int={}, ext={}], at time = {} channels: [{},{}|{},{}]",
                            configuration.getNodeId(), externalId, time,
                            (configuration.getConf0() != null ? configuration.getConf0().getChannel0() : "null"),
                            (configuration.getConf0() != null ? configuration.getConf0().getChannel1() : "null"),
                            (configuration.getConf1() != null ? configuration.getConf1().getChannel0() : "null"),
                            (configuration.getConf1() != null ? configuration.getConf1().getChannel1() : "null")
                    );
                }
                if (log.isTraceEnabled()) {
                    log.trace("AdHocCommunicationConfiguration: Number of radios: {}", configuration.getRadioMode());
                    if (configuration.getRadioMode() != AdHocConfiguration.RadioMode.OFF) {
                        log.trace("AdHocCommunicationConfiguration: radio0: IP: {}", configuration.getConf0().getNewIP());
                        log.trace("AdHocCommunicationConfiguration: radio0: Subnet: {}", configuration.getConf0().getNewSubnet());
                        log.trace("AdHocCommunicationConfiguration: radio0: Mode: {}", configuration.getConf0().getMode());
                        log.trace("AdHocCommunicationConfiguration: radio0: Channel0: {}", configuration.getConf0().getChannel0());
                        log.trace("AdHocCommunicationConfiguration: radio0: Channel1: {}", configuration.getConf0().getChannel1());
                        if (configuration.getConf0().getNewPower() == -1) {
                            log.trace("AdHocCommunicationConfiguration: radio0: Power set by federate");
                        } else {
                            log.trace("AdHocCommunicationConfiguration: radio0: Power: {} mW", configuration.getConf0().getNewPower());
                        }
                    }
                    if (configuration.getRadioMode() == AdHocConfiguration.RadioMode.DUAL) {
                        log.trace("AdHocCommunicationConfiguration: radio1: IP: {}", configuration.getConf1().getNewIP());
                        log.trace("AdHocCommunicationConfiguration: radio1: Subnet: {}", configuration.getConf1().getNewSubnet());
                        log.trace("AdHocCommunicationConfiguration: radio1: Mode: {}", configuration.getConf1().getMode());
                        log.trace("AdHocCommunicationConfiguration: radio1: Channel0: {}", configuration.getConf1().getChannel0());
                        log.trace("AdHocCommunicationConfiguration: radio1: Channel1: {}", configuration.getConf1().getChannel1());
                        if (configuration.getConf1().getNewPower() == -1) {
                            log.trace("AdHocCommunicationConfiguration: radio1: Power set by federate");
                        } else {
                            log.trace("AdHocCommunicationConfiguration: radio1: Power: {} mW", configuration.getConf1().getNewPower());
                        }
                    }
                }
                // actually write the data to the federate
                if (CMD.SUCCESS != this.ambassadorFederateChannel.writeConfigMessage(time, interactionId, externalId, configuration)) {
                    LoggerFactory.getLogger(this.getClass()).error(
                            "Could not configure node {}s radio: " + this.federateAmbassadorChannel.getLastStatusMessage(),
                            configuration.getNodeId()
                    );
                    throw new InternalFederateException(
                            "Error in " + this.federateName + this.federateAmbassadorChannel.getLastStatusMessage()
                    );
                }
            } else {
                throw new IllegalValueException("Node not simulated: " + configuration.getNodeId());
            }
        } catch (IOException | InternalFederateException | IllegalValueException ex) {
            this.log.error("{} could not configure the radio", ambassadorName);
        }
    }

    @Override
    public boolean isTimeConstrained() {
        return true;
    }

    @Override
    public boolean isTimeRegulating() {
        return true;
    }
}
