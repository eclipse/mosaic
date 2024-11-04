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

import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.enums.DestinationType;
import org.eclipse.mosaic.lib.enums.ProtocolType;
import org.eclipse.mosaic.lib.geo.GeoArea;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;

/**
 * Central API for obtaining {@link MessageRouting} for sending {@link org.eclipse.mosaic.lib.objects.v2x.V2xMessage}s via
 * ad hoc communication.
 */
public class AdHocMessageRoutingBuilder {

    private static final Logger log = LoggerFactory.getLogger(AdHocMessageRoutingBuilder.class);
    private final SourceAddressContainer sourceAddressContainer;
    private AdHocChannel channel = AdHocChannel.CCH;
    private NetworkAddress destination = null;
    private Integer hops = null;
    private DestinationType routing = null;
    private GeoArea targetArea = null;

    /**
     * The constructor for {@link AdHocMessageRoutingBuilder}.
     *
     * @param hostName       name of the sending entity
     * @param sourcePosition position of the sending entity
     */
    public AdHocMessageRoutingBuilder(String hostName, GeoPoint sourcePosition) {
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
                routing, destination, channel, hops, targetArea, ProtocolType.UDP
        ), sourceAddressContainer);
    }

    public AdHocMessageRoutingBuilder channel(AdHocChannel adHocChannel) {
        this.channel = adHocChannel;
        return this;
    }

    public AdHocMessageRoutingBuilder.RoutingSelector destination(NetworkAddress networkAddress) {
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

    public DestinationSelector singlehop() {
        routing = DestinationType.AD_HOC_TOPOCAST;
        hops = 1;
        return new DestinationSelector();
    }

    public DestinationSelector topological() {
        routing = DestinationType.AD_HOC_TOPOCAST;
        hops = 1;
        return new DestinationSelector();
    }

    public DestinationSelector topological(int maxHops) {
        require8BitTtl(maxHops);
        routing = DestinationType.AD_HOC_TOPOCAST;
        hops = maxHops;
        return new DestinationSelector();
    }

    public DestinationSelector geographical(GeoArea area) {
        routing = DestinationType.AD_HOC_GEOCAST;
        targetArea = area;
        return new DestinationSelector();
    }

    public final class RoutingSelector {

        public MessageRouting singlehop() {
            AdHocMessageRoutingBuilder.this.singlehop();
            return build();
        }

        public MessageRouting topological() {
            AdHocMessageRoutingBuilder.this.topological();
            return build();
        }

        public MessageRouting topological(int maxHops) {
            AdHocMessageRoutingBuilder.this.topological(maxHops);
            return build();
        }

        public MessageRouting geographical(GeoArea area) {
            AdHocMessageRoutingBuilder.this.geographical(area);
            return build();
        }

        public RoutingSelector channel(AdHocChannel channel) {
            AdHocMessageRoutingBuilder.this.channel(channel);
            return this;
        }
    }

    public final class DestinationSelector {

        public MessageRouting destination(NetworkAddress networkAddress) {
            AdHocMessageRoutingBuilder.this.destination(networkAddress);
            return build();
        }

        public MessageRouting destination(String receiverName) {
            AdHocMessageRoutingBuilder.this.destination(receiverName);
            return build();
        }

        public MessageRouting destination(byte[] ipv4Address) {
            AdHocMessageRoutingBuilder.this.destination(ipv4Address);
            return build();
        }

        public MessageRouting broadcast() {
            AdHocMessageRoutingBuilder.this.broadcast();
            return build();
        }

        public DestinationSelector channel(AdHocChannel channel) {
            AdHocMessageRoutingBuilder.this.channel(channel);
            return this;
        }
    }


//    private MessageRouting build(DestinationAddressContainer dac) {
//        return new MessageRouting(dac, sourceAddressContainer);
//    }
//
//    /**
//     * Sets a specific {@link AdHocChannel} for the {@link MessageRouting}.
//     *
//     * @param adHocChannel specific ad hoc channel {@link AdHocChannel}
//     * @return this builder
//     */
//    public AdHocMessageRoutingBuilder viaChannel(AdHocChannel adHocChannel) {
//        this.channel = adHocChannel;
//        return this;
//    }
//
//    public AdHocMessageRoutingBuilder destination(byte[] ipAddress) {
//        assert destination == null;
//        this.destination = new NetworkAddress(ipAddress);
//        return this;
//    }
//
//    public AdHocMessageRoutingBuilder destination(Inet4Address ipAddress) {
//        assert destination == null;
//        this.destination = new NetworkAddress(ipAddress);
//        return this;
//    }
//
//    public AdHocMessageRoutingBuilder destination(NetworkAddress ipAddress) {
//        assert destination == null;
//        this.destination = ipAddress;
//        return this;
//    }
//
//    public AdHocMessageRoutingBuilder destination(String receiverName) {
//        assert destination == null;
//        this.destination = new NetworkAddress(IpResolver.getSingleton().nameToIp(receiverName));
//        return this;
//    }
//
//    public AdHocMessageRoutingBuilder broadcast() {
//        assert destination == null;
//        this.destination = new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS);
//        // TODO Check that destination is only set once
//        // TODO Create a create method
//        return this;
//    }
//
//    public MessageRouting topological(int hops) {
//        checkDestination();
//        return build(new DestinationAddressContainer(
//                DestinationType.AD_HOC_TOPOCAST,
//                destination,
//                channel,
//                require8BitTtl(hops),
//                null,
//                ProtocolType.UDP
//        ));
//    }
//
//    public MessageRouting topological() {
//        return topological(1);
//    }
//
//    public MessageRouting singlehop() {
//        return topological(1);
//    }
//
//    public MessageRouting geographical(GeoArea area) {
//        checkDestination();
//        return build(new DestinationAddressContainer(
//                DestinationType.AD_HOC_GEOCAST,
//                destination,
//                channel,
//                null,
//                area,
//                ProtocolType.UDP
//        ));
//    }

    private void checkDestination() {
        if (destination == null) {
            log.warn("Destination address not set. Using broadcast as default.");
            destination = new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS);
        }
    }

    /**
     * The maximum time to live (TTL).
     */
    private final static int MAXIMUM_TTL = 255;

    private static int require8BitTtl(final int ttl) {
        if (ttl > MAXIMUM_TTL || ttl <= 0) {
            throw new IllegalArgumentException("Passed time to live shouldn't exceed 8-bit limit!");
        }
        return ttl;
    }
}
