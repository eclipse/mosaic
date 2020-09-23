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
 */

package org.eclipse.mosaic.lib.objects.addressing;

import org.eclipse.mosaic.lib.enums.DestinationType;
import org.eclipse.mosaic.lib.enums.ProtocolType;
import org.eclipse.mosaic.lib.geo.GeoArea;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoRectangle;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.MessageStreamRouting;

import java.net.Inet4Address;

/**
 * Central API for obtaining {@link MessageRouting} for sending {@link org.eclipse.mosaic.lib.objects.v2x.V2xMessage}s via
 * cellular communication.
 */
public class CellMessageRoutingBuilder {

    private final SourceAddressContainer sourceAddressContainer;

    private long streamDuration = -1;
    private long streamBandwidthInBitPs = -1;

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

    /**
     * Creates geo Broadcast (application layer) based on Unicast (network layer) for geo area.
     *
     * @param geoArea destination area as {@link GeoRectangle} or as {@link GeoCircle}
     * @return MessageRouting
     */
    public MessageRouting geoBroadcastBasedOnUnicast(GeoArea geoArea) {
        return build(new DestinationAddressContainer(
                DestinationType.CELL_GEOCAST,
                new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS.getAddress()),
                null,
                null,
                geoArea,
                ProtocolType.UDP //TODO change to TCP (requires revise of ack/nack handling in cell2)
        ));
    }

    /**
     * Creates geoBroadCast for destination area using mbms method.
     *
     * @param geoArea destination area as {@link GeoRectangle} or as {@link GeoCircle}
     * @return MessageRouting
     */
    public MessageRouting geoBroadcastMbms(GeoArea geoArea) {
        return build(new DestinationAddressContainer(
                DestinationType.CELL_GEOCAST_MBMS,
                new NetworkAddress(NetworkAddress.BROADCAST_ADDRESS.getAddress()),
                null,
                null,
                geoArea,
                ProtocolType.UDP //TODO change to TCP (requires revise of ack/nack handling in cell2)
        ));
    }

    /**
     * Creates topoCast to specified ip address.
     *
     * @param ipAddress recipient's ip address
     * @return MessageRouting
     */
    public MessageRouting topoCast(byte[] ipAddress) {
        return build(new DestinationAddressContainer(
                DestinationType.CELL_TOPOCAST,
                new NetworkAddress(ipAddress),
                null,
                null,
                null,
                ProtocolType.UDP //TODO change to TCP (requires revise of ack/nack handling in cell2)
        ));
    }

    /**
     * Creates topological cast to specified host name.
     *
     * @param name recipient's name
     * @return MessageRouting
     */
    public MessageRouting topoCast(String name) {
        return topoCast(IpResolver.getSingleton().nameToIp(name).getAddress());
    }


}
