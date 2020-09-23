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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Address network node (can be source or destination). Hold a 32-bit address (IPv4 address).
 */
@Immutable
public class NetworkAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Special IPv4 address for Broadcasts (IP address 255.255.255.255)
     */
    public final static Inet4Address BROADCAST_ADDRESS = createIPv4Address(255, 255, 255, 255);

    /**
     * Special IPv4 address for GeoAnycast (IP address 255.255.255.254)
     */
    final static Inet4Address ANYCAST_ADDRESS = createIPv4Address(255, 255, 255, 254);


    /**
     * IP address of a specific node or 255.255.255.255 if no specific node is addressed.
     */
    public final Inet4Address address;

    /**
     * Create an address object from the given raw IP address represented by a byte array.
     * The argument is in network byte order: the highest order byte of
     * the address is in ipv4Address[0].
     *
     * @param ipv4Address IPv4 address
     */
    public NetworkAddress(@Nonnull final byte[] ipv4Address) {
        Objects.requireNonNull(ipv4Address);
        require32BitAddress(ipv4Address.length);
        try {
            this.address = (Inet4Address) Inet4Address.getByAddress(ipv4Address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an address object given the {@link Inet4Address}.
     *
     * @param address IPv4 address
     */
    public NetworkAddress(@Nonnull final Inet4Address address) {
        Objects.requireNonNull(address);
        require32BitAddress(address.getAddress().length);
        this.address = address;
    }

    /**
     * Returns the actual IPv4 address of this {@link NetworkAddress}.
     *
     * @return the actual IPv4 address
     */
    @Nonnull
    public Inet4Address getIPv4Address() {
        return address;
    }

    @Nonnull
    private static Inet4Address createIPv4Address(int ip1, int ip2, int ip3, int ip4) {
        try {
            return (Inet4Address) InetAddress.getByAddress(
                    new byte[]{(byte) ip1, (byte) ip2, (byte) ip3, (byte) ip4}
            );
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns if this address is equal to the broadcast address 255.255.255.255
     *
     * @return if this address is equal to the broadcast address
     */
    public boolean isBroadcast() {
        return this.address.equals(BROADCAST_ADDRESS);
    }

    /**
     * Returns if this address is equal to the anycast address 255.255.255.254
     *
     * @return if this address is equal to the anycast address
     */
    public boolean isAnycast() {
        return address.equals(ANYCAST_ADDRESS);
    }

    /**
     * An address can only be of unicast type if it's not equal to broadcast OR anycast address.
     */
    public boolean isUnicast() {
        return !isBroadcast() && !isAnycast();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 61)
                .append(address)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        NetworkAddress other = (NetworkAddress) obj;
        return new EqualsBuilder()
                .append(this.address, other.address)
                .isEquals();
    }

    @Override
    public String toString() {
        return "NetworkAddress{" + "address=" + address + '}';
    }

    private static void require32BitAddress(final int length) {
        if (length != 4) {
            throw new IllegalArgumentException("Not a 32-bit address! Given network address must match the requirements for IPv4 addresses.");
        }
    }
}

