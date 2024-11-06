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

package org.eclipse.mosaic.lib.objects.addressing;

import org.eclipse.mosaic.lib.enums.DestinationType;
import org.eclipse.mosaic.lib.enums.ProtocolType;
import org.eclipse.mosaic.lib.geo.GeoArea;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.MessageStreamRouting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;

/**
 * Central API for obtaining {@link MessageRouting} for sending {@link org.eclipse.mosaic.lib.objects.v2x.V2xMessage}s via
 * cellular communication.
 */
public class CellMessageRoutingBuilder {

    private static final Logger log = LoggerFactory.getLogger(CellMessageRoutingBuilder.class);

    private final SourceAddressContainer sourceAddressContainer;

    private long streamDuration = -1;
    private long streamBandwidthInBitPs = -1;

    private NetworkAddress destination = null;
    private DestinationType routing = null;
    private GeoArea targetArea = null;

    private boolean destinationSet = false;
    private boolean routingSet = false;

    /**
     * The {@link ProtocolType} for the {@link MessageRouting}, on default this will be
     * {@link ProtocolType#UDP}.
     */
    private ProtocolType protocolType = ProtocolType.UDP;
    private boolean protocolSet = false;

    /**
     * Constructor for {@link CellMessageRoutingBuilder} to set required fields.
     *
     * @param hostName       name of host (source)
     * @param sourcePosition position of source
     */
    public CellMessageRoutingBuilder(String hostName, GeoPoint sourcePosition) {
        Inet4Address address = IpResolver.getSingleton().lookup(hostName);
        if (address == null) {
            throw new IllegalArgumentException("Given hostname " + hostName + " has no registered IP address");
        }

        this.sourceAddressContainer = new SourceAddressContainer(
                new NetworkAddress(address),
                hostName,
                sourcePosition
        );
    }

    private MessageRouting build() {
        checkNecessaryValues();
        MessageRouting messageRouting =  new MessageRouting(new DestinationAddressContainer(
                routing, destination, null, null, targetArea, protocolType),
                sourceAddressContainer);
        resetValues();
        return messageRouting;
    }

    private MessageRouting build(DestinationAddressContainer dac) {
        if (streamDuration < 0) {
            return new MessageRouting(dac, sourceAddressContainer);
        } else {
            return new MessageStreamRouting(dac, sourceAddressContainer, streamDuration, streamBandwidthInBitPs);
        }
    }

    /**
     * Defines stream properties for the message to send.
     *
     * @param streamDuration         The duration of the stream in ns.
     * @param streamBandwidthInBitPs The bandwidth of the stream in bits per second.
     */
    public CellMessageRoutingBuilder streaming(long streamDuration, long streamBandwidthInBitPs) {
        this.streamDuration = streamDuration;
        this.streamBandwidthInBitPs = streamBandwidthInBitPs;
        return this;
    }

    /**
     * Sets the {@link ProtocolType} for the routing.
     *
     * @param type the {@link ProtocolType} to be used
     * @return the {@link CellMessageRoutingBuilder}
     */
    public CellMessageRoutingBuilder protocol(ProtocolType type) {
        protocolType = type;
        protocolSet = true;
        return this;
    }

    /**
     * Sets the {@link ProtocolType} for the routing to {@link ProtocolType#TCP}.
     *
     * @return the {@link CellMessageRoutingBuilder}
     */
    public CellMessageRoutingBuilder tcp() {
        return protocol(ProtocolType.TCP);
    }


    /**
     * Sets the {@link ProtocolType} for the routing to {@link ProtocolType#UDP}.
     *
     * @return the {@link CellMessageRoutingBuilder}
     */
    public CellMessageRoutingBuilder udp() {
        return protocol(ProtocolType.UDP);
    }

    public CellMessageRoutingBuilder destination(NetworkAddress networkAddress) {
        assert !destinationSet : "Destination was already set! Using first setting.";
        this.destination = networkAddress;
        this.destinationSet = true;
        return this;
    }

    public CellMessageRoutingBuilder destination(String receiverName) {
        return destination(IpResolver.getSingleton().nameToIp(receiverName).getAddress());
    }

    public CellMessageRoutingBuilder destination(byte[] ipv4Address) {
        return destination(new NetworkAddress(ipv4Address));
    }

    public CellMessageRoutingBuilder broadcast() {
        return destination(new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS));

    }

    public CellMessageRoutingBuilder mbms(GeoArea area) {

        routing = DestinationType.CELL_GEOCAST_MBMS;
        targetArea = area;
        return this;
    }

    public CellMessageRoutingBuilder topological() {
        routing = DestinationType.CELL_TOPOCAST;
        return this;
    }

    public CellMessageRoutingBuilder topological(int maxHops) {
        routing = DestinationType.CELL_TOPOCAST;
        return this;
    }

    public CellMessageRoutingBuilder geographical(GeoArea area) {
        routing = DestinationType.CELL_GEOCAST;
        targetArea = area;
        return this;
    }

    private void checkNecessaryValues() {
        checkDestination();
        checkRouting();
    }

    private void checkDestination() {
        if (destination == null) {
            throw new IllegalArgumentException("No destination address was given! Aborting.");
        }
    }

    private void checkRouting() {
        if (routing == null) {
            throw new IllegalArgumentException("No routing protocol was given! Aborting.");
        }
    }

    private void resetValues() {
        this.destination = null;
        this.routing = null;
        this.targetArea = null;

        this.destinationSet = false;
        this.routingSet = false;
    }
}
