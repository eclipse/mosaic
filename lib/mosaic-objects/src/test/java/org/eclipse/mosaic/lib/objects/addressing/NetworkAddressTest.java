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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class NetworkAddressTest {

    private final static byte[] SOME_IP_ADDRESS = {(byte) 0, (byte) 0, (byte) 0, (byte) 1};
    private final static byte[] LONGER_IP_ADDRESS = {(byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0};
    private final static byte[] SHORTER_IP_ADDRESS = {(byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0};

    /**
     * Test of constructors of class NetworkAddress
     */
    @Test
    public void testConstructors() {
        //SETUP + RUN
        NetworkAddress networkAddress = new NetworkAddress(SOME_IP_ADDRESS);
        //ASSERT
        assertArrayEquals(networkAddress.getIPv4Address().getAddress(), SOME_IP_ADDRESS);

        //SETUP
        Inet4Address ipv4Address;
        try {
            ipv4Address = (Inet4Address) Inet4Address.getByAddress(SOME_IP_ADDRESS);
        } catch (UnknownHostException uhe) {
            throw new AssertionError("Could NOT create an Inet4Address from manually given CORRECT byte representation! This shouldn't have happened...");
        }
        //RUN
        NetworkAddress anotherNetworkAddress = new NetworkAddress(ipv4Address);
        //ASSERT
        assertArrayEquals(anotherNetworkAddress.getIPv4Address().getAddress(), SOME_IP_ADDRESS);
        assertTrue(networkAddress.isUnicast());
    }

    /**
     * Test creating a NetworkAddress instance from an ip address in its byte representation over the limit of IPv4.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLongerAddress() {
        //SETUP + RUN + ASSERT
        NetworkAddress networkAddress = new NetworkAddress(LONGER_IP_ADDRESS);
    }

    /**
     * Test creating a NetworkAddress instance from an ip address in its byte representation shorter than needed for IPv4.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testShorterAddress() {
        //SETUP + RUN + ASSERT
        NetworkAddress networkAddress = new NetworkAddress(SHORTER_IP_ADDRESS);
    }

    /**
     * Test of getIp method, of class Address.
     */
    @Test
    public void testBroadcastNetworkAddress() {
        //SETUP
        Inet4Address broadcastAddress;
        try {
            broadcastAddress = (Inet4Address) Inet4Address.getByAddress(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});
        } catch (UnknownHostException uhe) {
            throw new AssertionError("Could NOT create an Inet4Address from manually given CORRECT byte representation! This shouldn't have happened...");
        }

        //RUN
        NetworkAddress networkAddress = new NetworkAddress(broadcastAddress);
        //ASSERT
        assertTrue(networkAddress.isBroadcast());
    }

    /**
     * Test of getIp method, of class Address.
     */
    @Test
    public void testAnycastNetworkAddress() {
        //SETUP
        Inet4Address broadcastAddress;
        try {
            broadcastAddress = (Inet4Address) Inet4Address.getByAddress(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 254});
        } catch (UnknownHostException uhe) {
            throw new AssertionError("Could NOT create an Inet4Address from manually given CORRECT byte representation! This shouldn't have happened...");
        }

        //RUN
        NetworkAddress networkAddress = new NetworkAddress(broadcastAddress);
        //ASSERT
        assertTrue(networkAddress.isAnycast());
    }

}
