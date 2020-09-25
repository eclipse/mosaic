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
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoRectangle;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;

import java.net.Inet4Address;

/**
 * Central API for obtaining {@link MessageRouting} for sending {@link org.eclipse.mosaic.lib.objects.v2x.V2xMessage}s via
 * ad hoc communication.
 */
public class AdHocMessageRoutingBuilder {

    private final SourceAddressContainer sourceAddressContainer;
    private AdHocChannel channel = AdHocChannel.CCH;

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

    private MessageRouting build(DestinationAddressContainer dac) {
        return new MessageRouting(dac, sourceAddressContainer);
    }

    /**
     * Sets a specific {@link AdHocChannel} for the {@link MessageRouting}.
     *
     * @param adHocChannel specific ad hoc channel {@link AdHocChannel}
     * @return this builder
     */
    public AdHocMessageRoutingBuilder viaChannel(AdHocChannel adHocChannel) {
        this.channel = adHocChannel;
        return this;
    }

    /**
     * Creates geo broadcast to destination area.
     *
     * @param geoArea destination circle {@link GeoCircle} or destination rectangle {@link GeoRectangle}
     * @return MessageRouting
     */
    public MessageRouting geoBroadCast(GeoArea geoArea) {
        return build(new DestinationAddressContainer(
                DestinationType.AD_HOC_GEOCAST,
                new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS),
                channel,
                null,
                geoArea,
                ProtocolType.UDP
        ));
    }

    /**
     * Creates geo cast to specific IP address using a specific {@link AdHocChannel}.
     * Note: the SNS doesn't support explicit addressing when geoCasting
     *
     * @param geoArea   destination circle {@link GeoCircle} or destination rectangle {@link GeoRectangle}
     * @param ipAddress specific ip address in byte array representation
     * @return MessageRouting
     */
    public MessageRouting geoCast(GeoArea geoArea, byte[] ipAddress) {
        return build(new DestinationAddressContainer(
                DestinationType.AD_HOC_GEOCAST,
                new NetworkAddress(ipAddress),
                channel,
                null,
                geoArea,
                ProtocolType.UDP
        ));
    }

    /**
     * Creates a topological broadcast using {@link AdHocChannel} SCH1 and single hop.
     *
     * @return MessageRouting
     */
    public MessageRouting topoBroadCast() {
        return topoCast(NetworkAddress.BROADCAST_ADDRESS.getAddress(), 1);
    }

    /**
     * Creates a topological broadcast using a specific {@link AdHocChannel} and specific number of hops.
     * Note: The SNS will dismiss hop value, since it only allows for single-hop TopoCasts
     *
     * @param hops number of hops
     * @return MessageRouting
     */
    public MessageRouting topoBroadCast(int hops) {
        return topoCast(NetworkAddress.BROADCAST_ADDRESS.getAddress(), hops);
    }

    /**
     * Creates a topological cast using a specific destination host name, a specific {@link AdHocChannel} and specific number of hops.
     * Note: The SNS will dismiss hop value, since it only allows for single-hop TopoCasts, so if receiver can't be reached, within
     * one hop transmission will fail.
     *
     * @param receiverName destination host name
     * @param hops         number of hops
     * @return MessageRouting
     */
    public MessageRouting topoCast(String receiverName, int hops) {
        return topoCast(IpResolver.getSingleton().nameToIp(receiverName).getAddress(), hops);
    }

    /**
     * Creates a topological cast using a specific destination IP address, a specific {@link AdHocChannel} and specific number of hops.
     * Note: The SNS will dismiss hop value, since it only allows for single-hop TopoCasts, so if receiver can't be reached, within
     * one hop transmission will fail.
     *
     * @param ipAddress destination IP address
     * @param hops      number of hops
     * @return MessageRouting
     */
    public MessageRouting topoCast(byte[] ipAddress, int hops) {
        return build(new DestinationAddressContainer(
                DestinationType.AD_HOC_TOPOCAST,
                new NetworkAddress(ipAddress),
                channel,
                require8BitTtl(hops),
                null,
                ProtocolType.UDP
        ));
    }

    /**
     * The maximum time to live (TTL).
     */
    private final static int MAXIMUM_TTL = 255;

    private static int require8BitTtl(final int ttl) {
        if (ttl > MAXIMUM_TTL || ttl < 0) {
            throw new IllegalArgumentException("Passed time to live shouldn't exceed 8-bit limit!");
        }
        return ttl;
    }
}
