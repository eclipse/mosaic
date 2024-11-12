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

    /**
     * The maximum time to live (TTL).
     */
    private final static int MAXIMUM_TTL = 255;

    private static final Logger log = LoggerFactory.getLogger(AdHocMessageRoutingBuilder.class);

    private final SourceAddressContainer sourceAddressContainer;

    private AdHocChannel channel = AdHocChannel.CCH;
    private NetworkAddress destination = null;
    private Integer hops = MAXIMUM_TTL;
    private DestinationType routing = null;
    private GeoArea targetArea = null;

    private boolean channelSet = false;
    private boolean destinationSet = false;
    private boolean routingSet = false;
    private boolean hopsSet = false;

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

    public MessageRouting build() {
        checkNecessaryValues();
        MessageRouting messageRouting = new MessageRouting(new DestinationAddressContainer(
                routing, destination, channel, hops, targetArea, ProtocolType.UDP),
                sourceAddressContainer);
        resetValues();
        return messageRouting;
    }

    public MessageRouting build(DestinationAddressContainer dac) {
        checkNecessaryValues();
        MessageRouting messageRouting = new MessageRouting(dac, sourceAddressContainer);
        resetValues();
        return messageRouting;
    }

    /**
     * Sets a specific {@link AdHocChannel} for the {@link MessageRouting}.
     *
     * @param adHocChannel specific ad hoc channel {@link AdHocChannel}
     * @return this builder
     */
    public AdHocMessageRoutingBuilder channel(AdHocChannel adHocChannel) {
        assert !channelSet: "Channel was already set! Using first setting.";
        this.channel = adHocChannel;
        this.channelSet = true;
        return this;
    }

    public AdHocMessageRoutingBuilder destination(byte[] ipAddress) {
        return destination(new NetworkAddress(ipAddress));
    }

    public AdHocMessageRoutingBuilder destination(Inet4Address ipAddress) {
        return destination(new NetworkAddress(ipAddress));
    }

    public AdHocMessageRoutingBuilder destination(String receiverName) {
        return destination(IpResolver.getSingleton().nameToIp(receiverName));
    }

    public AdHocMessageRoutingBuilder destination(NetworkAddress ipAddress) {
        assert !destinationSet: "Destination was already set! Using first setting.";
        this.destination = ipAddress;
        this.destinationSet = true;
        return this;
    }

    public AdHocMessageRoutingBuilder broadcast() {
        return destination(new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS));
    }

    public AdHocMessageRoutingBuilder topological() {
        assert !routingSet: "Routing was already set! Using first setting";
        this.routing = DestinationType.AD_HOC_TOPOCAST;
        this.routingSet = true;
        return this;
    }

    public AdHocMessageRoutingBuilder singlehop() {
        hops(1);
        return topological();
    }

    public AdHocMessageRoutingBuilder geographical(GeoArea area) {
        assert !routingSet: "Routing was already set! Using first setting";
        this.routing = DestinationType.AD_HOC_GEOCAST;
        this.targetArea = area;
        routingSet = true;
        return this;
    }

    public AdHocMessageRoutingBuilder hops(int hops) {
        assert !hopsSet: "Number of hops was already set! Using first setting.";
        this.hops = require8BitTtl(hops);
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

    private static int require8BitTtl(final int ttl) {
        if (ttl > MAXIMUM_TTL || ttl <= 0) {
            throw new IllegalArgumentException("Passed time to live shouldn't exceed 8-bit limit!");
        }
        return ttl;
    }

    private void resetValues() {
        this.channel = AdHocChannel.CCH;
        this.destination = null;
        this.hops = MAXIMUM_TTL;
        this.routing = null;
        this.targetArea = null;

        this.channelSet = false;
        this.destinationSet = false;
        this.routingSet = false;
        this.hopsSet = false;
    }
}
