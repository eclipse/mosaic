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

    private MessageRouting build() {
        return new MessageRouting(new DestinationAddressContainer(
                routing, destination, null, null, targetArea, protocolType
        ), sourceAddressContainer);
    }

    /**
     * Sets the {@link ProtocolType} for the routing.
     *
     * @param type the {@link ProtocolType} to be used
     * @return the {@link CellMessageRoutingBuilder}
     */
    public CellMessageRoutingBuilder protocol(ProtocolType type) {
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

    public RoutingSelector destination(NetworkAddress networkAddress) {
        this.destination = networkAddress;
        return new RoutingSelector();
    }

    public RoutingSelector destination(String receiverName) {
        return destination(IpResolver.getSingleton().nameToIp(receiverName).getAddress());
    }

    public RoutingSelector destination(byte[] ipv4Address) {
        this.destination = new NetworkAddress(ipv4Address);
        return new RoutingSelector();
    }

    public RoutingSelector broadcast() {
        this.destination = new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS);
        return new RoutingSelector();
    }

    public DestinationSelector mbms(GeoArea area) {
        routing = DestinationType.CELL_GEOCAST_MBMS;
        targetArea = area;
        return new DestinationSelector();
    }

    public DestinationSelector topological() {
        routing = DestinationType.CELL_TOPOCAST;
        return new DestinationSelector();
    }

    public DestinationSelector topological(int maxHops) {
        routing = DestinationType.CELL_TOPOCAST;
        return new DestinationSelector();
    }

    public DestinationSelector geographical(GeoArea area) {
        routing = DestinationType.CELL_GEOCAST;
        targetArea = area;
        return new DestinationSelector();
    }

    public final class RoutingSelector {

        public MessageRouting mbms(GeoArea area) {
            CellMessageRoutingBuilder.this.mbms(area);
            return build();
        }

        public MessageRouting topological() {
            CellMessageRoutingBuilder.this.topological();
            return build();
        }

        public MessageRouting topological(int maxHops) {
            CellMessageRoutingBuilder.this.topological(maxHops);
            return build();
        }

        public MessageRouting geographical(GeoArea area) {
            CellMessageRoutingBuilder.this.geographical(area);
            return build();
        }

        public RoutingSelector protocol(ProtocolType type) {
            if (protocolChanged) {
                log.warn("Protocol type has been set twice. First given type has been chosen.");
                return this;
            }
            CellMessageRoutingBuilder.this.protocol(type);
            return this;
        }

    }

    public final class DestinationSelector {
        public MessageRouting destination(String receiverName) {
            CellMessageRoutingBuilder.this.destination(receiverName);
            return build();
        }

        public MessageRouting destination(byte[] ipv4Address) {
            CellMessageRoutingBuilder.this.destination(ipv4Address);
            return build();
        }

        public MessageRouting broadcast() {
            CellMessageRoutingBuilder.this.broadcast();
            return build();
        }

        public DestinationSelector protocol(ProtocolType type) {
            if (protocolChanged) {
                log.warn("Protocol type has been set twice. First given type has been chosen.");
                return this;
            }
            CellMessageRoutingBuilder.this.protocol(type);
            return this;
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

    private MessageRouting build(DestinationAddressContainer dac) {
        if (streamDuration < 0) {
            return new MessageRouting(dac, sourceAddressContainer);
        } else {
            return new MessageStreamRouting(dac, sourceAddressContainer, streamDuration, streamBandwidthInBitPs);
        }
    }

    private void checkDestination() {
        if (destination == null) {
            log.warn("Destination address not set. Using broadcast as default.");
            destination = new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS);
        }
    }

    private void checkDestinationType() {
        if (routing == null) {
            log.info("Destination type not specified. Using unicast.");
            routing = DestinationType.CELL_GEOCAST;
        }
    }
}
