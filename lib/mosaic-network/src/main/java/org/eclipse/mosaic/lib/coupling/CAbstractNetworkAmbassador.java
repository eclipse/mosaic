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

import org.eclipse.mosaic.lib.enums.DestinationType;
import org.eclipse.mosaic.lib.enums.ProtocolType;
import org.eclipse.mosaic.lib.objects.addressing.NetworkAddress;

import java.util.HashMap;
import java.util.Map;

public final class CAbstractNetworkAmbassador {

    public String federateConfigurationFile;

    private CMessages messages = new CMessages();

    /**
     * Returns {@code true}, if the network ambassador implementation supports the
     * given {@link DestinationType}.
     *
     * @param routingType the {@link DestinationType} to check
     * @return {@code true}, if the network ambassador implementation supports the given {@link DestinationType}.
     */
    boolean isRoutingTypeSupported(DestinationType routingType) {
        return messages.routingType.getOrDefault(routingType, false);
    }

    /**
     * Returns {@code true}, if the network ambassador implementation supports the
     * given {@link NetworkAddress}.
     *
     * @param destinationAddress the {@link NetworkAddress} to check
     * @return {@code true}, if the network ambassador implementation supports the given {@link NetworkAddress}.
     */
    boolean isAddressTypeSupported(NetworkAddress destinationAddress) {
        return destinationAddress != null
                && (destinationAddress.isUnicast() && messages.destinationAddress.ipv4UnicastAddress
                || destinationAddress.isBroadcast() && messages.destinationAddress.ipv4BroadcastAddress
                || destinationAddress.isAnycast() && messages.destinationAddress.ipv4AnycastAddress);
    }

    /**
     * Returns {@code true}, if the network ambassador implementation supports the given {@link ProtocolType}.
     *
     * @param protocolType the {@link ProtocolType} to check
     * @return {@code true}, if the network ambassador implementation supports the given {@link ProtocolType}.
     */
    boolean isProtocolSupported(ProtocolType protocolType) {
        return messages.protocolType.getOrDefault(protocolType, false);
    }

    static class CMessages {

        private CDestinationAdress destinationAddress = new CDestinationAdress();
        private Map<DestinationType, Boolean> routingType = new HashMap<>();
        private Map<ProtocolType, Boolean> protocolType = new HashMap<>();

        CMessages() {
            routingType.put(DestinationType.AD_HOC_GEOCAST, false);
            routingType.put(DestinationType.AD_HOC_TOPOCAST, true);
            routingType.put(DestinationType.CELL_GEOCAST, false);
            routingType.put(DestinationType.CELL_GEOCAST_MBS, false);
            routingType.put(DestinationType.CELL_TOPOCAST, false);

            protocolType.put(ProtocolType.UDP, true);
            protocolType.put(ProtocolType.TCP, false);
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    static class CDestinationAdress {
        private boolean ipv4UnicastAddress = false;
        private boolean ipv4BroadcastAddress = true;
        private boolean ipv4AnycastAddress = false;
    }
}
