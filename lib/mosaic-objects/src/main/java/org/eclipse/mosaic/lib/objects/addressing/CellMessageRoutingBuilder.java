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

import org.apache.commons.lang3.Validate;

import java.net.Inet4Address;

/**
 * Central API for obtaining {@link MessageRouting} for sending {@link org.eclipse.mosaic.lib.objects.v2x.V2xMessage}s via
 * cellular communication.
 */
public class CellMessageRoutingBuilder {

    private final SourceAddressContainer sourceAddressContainer;

    private long streamDuration = -1;
    private long streamBandwidthInBitPs = -1;

    private NetworkAddress destination = null;
    private DestinationType routing = null;
    private GeoArea targetArea = null;

    private boolean destinationChanged = false;
    private boolean routingChanged = false;
    private boolean mbsChanged = false;

    /**
     * The {@link ProtocolType} for the {@link MessageRouting}, on default this will be
     * {@link ProtocolType#UDP}.
     */
    private ProtocolType protocolType = ProtocolType.UDP;
    private boolean protocolChanged = false;

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

    public MessageRouting build() {
        checkNecessaryValues();
        return this.build(new DestinationAddressContainer(
                routing, destination, null, null, targetArea, protocolType)
        );
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
        Validate.isTrue(!protocolChanged, "Protocol was already set!");
        protocolType = type;
        protocolChanged = true;
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
        Validate.isTrue(!destinationChanged, "Destination was already set!");
        this.destination = networkAddress;
        this.destinationChanged = true;
        return this;
    }

    public CellMessageRoutingBuilder destination(String receiverName) {
        return destination(IpResolver.getSingleton().nameToIp(receiverName).getAddress());
    }

    public CellMessageRoutingBuilder destination(Inet4Address ipAddress) {
        return destination(new NetworkAddress(ipAddress));
    }

    public CellMessageRoutingBuilder destination(byte[] ipv4Address) {
        return destination(new NetworkAddress(ipv4Address));
    }

    public CellMessageRoutingBuilder broadcast() {
        return destination(new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS));
    }

    public CellMessageRoutingBuilder mbs() {
        Validate.isTrue(!mbsChanged, "MBS was already chosen!");
        Validate.isTrue(!(routing == DestinationType.CELL_TOPOCAST), "MBS can not be enabled for topological routing!");
        routing = DestinationType.CELL_GEOCAST_MBMS;
        mbsChanged = true;
        return this;
    }

    public CellMessageRoutingBuilder topological() {
        Validate.isTrue(!routingChanged, "Routing was already set!");
        Validate.isTrue(!mbsChanged, "MBS can not be enabled for topological routing!");
        routing = DestinationType.CELL_TOPOCAST;
        routingChanged = true;
        return this;
    }

    public CellMessageRoutingBuilder geographical(GeoArea area) {
        Validate.isTrue(!routingChanged, "Routing was already set!");
        if (!mbsChanged) {
            routing = DestinationType.CELL_GEOCAST;
        }
        targetArea = area;
        routingChanged = true;
        return this;
    }

    private void checkNecessaryValues() {
        checkDestination();
        checkRouting();
        checkArea();
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

    private void checkArea() {
        if (routing == DestinationType.CELL_GEOCAST || routing == DestinationType.CELL_GEOCAST_MBMS) {
            if (targetArea == null) {
                throw new IllegalArgumentException("No target area was given for geographical routing! Aborting.");
            }
        }
    }
}
