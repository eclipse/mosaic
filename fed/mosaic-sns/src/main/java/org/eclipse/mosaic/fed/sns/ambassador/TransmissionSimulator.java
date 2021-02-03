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

package org.eclipse.mosaic.fed.sns.ambassador;

import org.eclipse.mosaic.fed.sns.config.CSns;
import org.eclipse.mosaic.fed.sns.model.AdhocTransmissionModel;
import org.eclipse.mosaic.fed.sns.model.TransmissionParameter;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.geo.Area;
import org.eclipse.mosaic.lib.geo.CartesianCircle;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoArea;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.transmission.TransmissionResult;
import org.eclipse.mosaic.lib.objects.addressing.DestinationAddressContainer;
import org.eclipse.mosaic.lib.objects.addressing.IpResolver;
import org.eclipse.mosaic.lib.objects.addressing.NetworkAddress;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TransmissionSimulator {

    private final static Logger log = LoggerFactory.getLogger(TransmissionSimulator.class);

    /**
     * TTL for 1 (ONE) hop.
     */
    private static final int SINGLE_HOP_TTL = 1;

    /**
     * Reference to config data.
     */
    private final CSns config;

    /**
     * An {@link AdhocTransmissionModel} defined in the configuration. If not defined
     * a {@link org.eclipse.mosaic.fed.sns.model.SimpleAdhocTransmissionModel} will be used.
     */
    private final AdhocTransmissionModel transmissionModel;

    /**
     * {@link RandomNumberGenerator} used for transmission calculation.
     */
    private final RandomNumberGenerator randomNumberGenerator;


    /**
     * Constructor for {@link TransmissionSimulator}, sets the configuration, transmission models & RNG.
     *
     * @param randomNumberGenerator the {@link RandomNumberGenerator} to be used for transmission calculations
     * @throws InternalFederateException if config couldn't be read or was malformed
     */
    @SuppressWarnings(value = "BC_IMPOSSIBLE_INSTANCEOF", justification = "delay field can be serialized with different delay types")
    public TransmissionSimulator(RandomNumberGenerator randomNumberGenerator, CSns config) throws InternalFederateException {
        this.config = config;
        if (config == null) {
            throw new InternalFederateException("Illegal state: TransmissionSimulator initialized before configuration was set.");
        }
        // setup rng
        this.randomNumberGenerator = randomNumberGenerator;
        // set transmission model from configuration
        this.transmissionModel = config.adhocTransmissionModel;
    }

    Map<String, TransmissionResult> preProcessInteraction(V2xMessageTransmission interaction) {
        final String senderName = interaction.getSourceName();
        if (!isValidSender(senderName)) {
            return null;
        }

        DestinationAddressContainer dac = interaction.getMessage().getRouting().getDestination();
        Map<String, TransmissionResult> transmissionResults = null;
        switch (dac.getType()) {
            case AD_HOC_TOPOCAST:
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Send v2xMessage.id={} from node={} as Topocast (singlehop) @time={}",
                            interaction.getMessage().getId(), senderName, TIME.format(interaction.getTime())
                    );
                }
                transmissionResults = sendMessageAsTopocast(senderName, dac);
                break;
            case AD_HOC_GEOCAST:
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Send v2xMessage.id={} from={} as Geocast (geo routing) @time={}",
                            interaction.getMessage().getId(), senderName, TIME.format(interaction.getTime())
                    );
                }
                transmissionResults = sendMessageAsGeocast(senderName, dac);
                break;
            default:
                log.debug(
                        "V2XMessage is not an ad hoc message. Skip this message. V2XMessage.id={}",
                        interaction.getMessage().getId()
                );
        }
        return transmissionResults;
    }

    /**
     * This method will check if a potential sender qualifies as one.
     *
     * @param senderName name of the sending unit
     * @return {@code true} if sender is valid, else {@code false}
     */
    private boolean isValidSender(String senderName) {
        SimulationNode sender = SimulationEntities.INSTANCE.getOnlineNode(senderName);

        if (sender == null) {
            log.warn("Unit {} is not known, skipping", senderName);
            return false;
        }
        if (sender.getPosition() == null) {
            log.warn("position of the unit is null");
            return false;
        }
        return true;
    }

    /**
     * Simulates topolocically-scoped Unicast or Broadcast as direct singlehop transmission.
     * (NO multi-hops are implemented by now)
     * <ol>
     * <li>Verify if configured transmission can be send using the topocast logic
     * <li>Differentiate between Unicast and Broadcast
     * <li>Simulate transmission with one hop
     * </ol>
     *
     * @param senderName The Sender of the message.
     * @param dac        {@link DestinationAddressContainer} containing information about the destination for the message.
     * @return a Map containing the summarized transmission results
     */
    protected Map<String, TransmissionResult> sendMessageAsTopocast(String senderName, DestinationAddressContainer dac) {
        NetworkAddress destinationAddress = dac.getAddress();
        if (destinationAddress.isAnycast()) { // check for valid destination address
            log.warn(
                    "The SNS only supports SingleHopBroadCasts or SingleHopUniCasts when using TopoCasts."
                            + " The given destination address {} is not valid. No message will be send.",
                    destinationAddress
            );
            return null;
        }
        if (dac.getTimeToLive() != SINGLE_HOP_TTL) { // inform about dismissed TTL
            log.debug("TTL {} will be dismissed and 1 will be used instead. For Topocast, SNS only supports SingleHopBroadCasts.", dac.getTimeToLive());
        }

        // accumulate all potential receivers in direct communication range
        SimulationNode sender = SimulationEntities.INSTANCE.getOnlineNode(senderName);
        Map<String, SimulationNode> allPotentialReceivers;
        if (destinationAddress.isBroadcast()) { // SingleHopBroadCast
            allPotentialReceivers = getPotentialBroadcastReceivers(getTopocastDestinationArea(sender));
            // remove sender as single radios could not transmit and receive at the same time
            allPotentialReceivers.remove(senderName);
        } else { // SingleHopUniCast
            allPotentialReceivers = getAddressedReceiver(destinationAddress, getTopocastDestinationArea(sender));
        }
        log.debug("Addressed nodes in destination area={}", allPotentialReceivers);

        // perform actual transmission
        TransmissionParameter transmissionParameter = new TransmissionParameter(
                randomNumberGenerator,
                config.singlehopDelay,
                config.singlehopTransmission,
                SINGLE_HOP_TTL
        );
        return transmissionModel.simulateTopocast(
                senderName, allPotentialReceivers, transmissionParameter, SimulationEntities.INSTANCE.getAllOnlineNodes()
        );
    }

    /**
     * Simulates geocast routing transmission.
     * (ONLY Broadcasts are implemented by now)
     * <ol>
     * <li>Verify if configured transmission can be send using the topocast logic
     * <li>determine all potential receiver nodes in the destination area
     * (including original sender due to re-broadcasting in geocast)</li>
     * <li>simulate message transmission via
     * <ol type="a">
     * <li>simplified Multihop mode (possibly needed hops to reach destination not regarded)
     * <li>with more elaborated approaching and flooding modes
     * </ol>
     * </ol>
     *
     * @param senderName The Sender of the message.
     * @param dac        {@link DestinationAddressContainer} containing information about the destination for the message.
     * @return a Map containing the summarized transmission results
     */
    protected Map<String, TransmissionResult> sendMessageAsGeocast(String senderName, DestinationAddressContainer dac) {
        if (dac.getGeoArea() == null) {
            return Collections.EMPTY_MAP;
        }

        Area<CartesianPoint> destinationArea = dac.getGeoArea().toCartesian();
        Map<String, SimulationNode> allReceivers = getPotentialBroadcastReceivers(destinationArea);
        log.debug("Addressed nodes in destination area={}", allReceivers);

        // get ttl value, this will be ignored for the simple transmission model
        int ttl;
        if (dac.getTimeToLive() == -1) {
            ttl = config.maximumTtl; // ttl was null, which is interpreted as maximum
        } else {
            ttl = Math.min(dac.getTimeToLive(), config.maximumTtl); // ttl can't be higher than maximumTtl
        }
        TransmissionParameter transmissionParameter = new TransmissionParameter(
                randomNumberGenerator,
                config.singlehopDelay,
                config.singlehopTransmission,
                ttl
        );
        return transmissionModel.simulateGeocast(
                senderName, allReceivers, transmissionParameter, SimulationEntities.INSTANCE.getAllOnlineNodes()
        );
    }

    private Area<CartesianPoint> getTopocastDestinationArea(SimulationNode nodeData) {
        return new CartesianCircle(nodeData.getPosition(), nodeData.getRadius());
    }

    /**
     * Collects all nodes within the specified destination area.
     *
     * @param destinationArea destination area for transmission
     * @return a map containing the
     */
    private Map<String, SimulationNode> getPotentialBroadcastReceivers(Area<CartesianPoint> destinationArea) {
        return getEntitiesInArea(SimulationEntities.INSTANCE.getAllOnlineNodes(), destinationArea);
    }

    /**
     * This method collects all entities, that are within the given {@link GeoArea}.
     * It is static because it is also required in some of the {@link AdhocTransmissionModel}s.
     *
     * @param relevantEntities a map of all entities and their names, which should be checked against the area
     * @param range            the {@link GeoArea} within which the entities should be.
     *                         It is called "range" because it reflects the communication range
     * @return A map of the given entities, which are in the destination area.
     */
    public static Map<String, SimulationNode> getEntitiesInArea(
            Map<String, SimulationNode> relevantEntities, Area<CartesianPoint> range) {
        Map<String, SimulationNode> results = new HashMap<>();

        for (Map.Entry<String, SimulationNode> entityEntry : relevantEntities.entrySet()) {
            if (range.contains(entityEntry.getValue().getPosition())) {
                results.put(entityEntry.getKey(), entityEntry.getValue());
            }
        }
        return results;
    }

    /**
     * Returns the addressed receiver, if it is known inside the destination area (more specific check compared to broadcast).
     * Note: The resulting Map will always contain 0 or 1 elements.
     *
     * @param destinationAddress address of the potential receiver
     * @param reachableArea      area, that can be reached with one hop by the sender
     * @return if receiver was found single element map, else empty map
     */
    private Map<String, SimulationNode> getAddressedReceiver(NetworkAddress destinationAddress, Area<CartesianPoint> reachableArea) {
        final Map<String, SimulationNode> receiver = new HashMap<>();

        final String destinationNodeId = IpResolver.getSingleton().reverseLookup(destinationAddress.getIPv4Address());
        SimulationNode entity = SimulationEntities.INSTANCE.getOnlineNode(destinationNodeId);
        if (entity != null && isNodeInArea(entity.getPosition(), reachableArea)) {
            receiver.put(destinationNodeId, entity);
        }
        return receiver;
    }

    private boolean isNodeInArea(CartesianPoint nodePosition, Area<CartesianPoint> destinationArea) {
        if (nodePosition == null) {
            log.warn("position of the unit is null");
            return false;
        }
        return destinationArea.contains(nodePosition);
    }
}
