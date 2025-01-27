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

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.junit.IpResolverRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class IpResolverTest {

    private final String testConfig = """
            {
                "netMask": "255.255.0.0",
                "vehicleNet": "10.1.0.0",
                "rsuNet": "10.2.0.0",
                "tlNet": "10.3.0.0",
                "csNet": "10.4.0.0",
                "agentNet": "10.10.0.0"
            }
            """;

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule(testConfig);

    @Test
    public void testCorrectSetup() {
        assertEquals(65534, IpResolver.getSingleton().getMaxRange());
    }

    @Test
    public void testRoundTripVEHs() {
        for (int i = 0; i < IpResolver.getSingleton().getMaxRange(); ++i) {
            final String name = "veh_" + i;
            Inet4Address nameToIp = IpResolver.getSingleton().nameToIp(name);
            String ipToName = IpResolver.getSingleton().ipToName(nameToIp);
            assertEquals(name, ipToName);
        }
    }

    @Test
    public void testRoundTripRSUs() {
        for (int i = 0; i < IpResolver.getSingleton().getMaxRange(); ++i) {
            final String name = "rsu_" + i;
            Inet4Address nameToIp = IpResolver.getSingleton().nameToIp(name);
            String ipToName = IpResolver.getSingleton().ipToName(nameToIp);
            assertEquals(name, ipToName);
        }
    }

    @Test
    public void testRoundTripTLs() {
        for (int i = 0; i < IpResolver.getSingleton().getMaxRange(); ++i) {
            final String name = "rsu_" + i;
            Inet4Address nameToIp = IpResolver.getSingleton().nameToIp(name);
            String ipToName = IpResolver.getSingleton().ipToName(nameToIp);
            assertEquals(name, ipToName);
        }
    }

    @Test
    public void testRoundTripCSs() {
        for (int i = 0; i < IpResolver.getSingleton().getMaxRange(); ++i) {
            final String name = "cs_" + i;
            Inet4Address nameToIp = IpResolver.getSingleton().nameToIp(name);
            String ipToName = IpResolver.getSingleton().ipToName(nameToIp);
            assertEquals(name, ipToName);
        }
    }

    @Test
    public void testRoundTripAgents() {
        for (int i = 0; i < IpResolver.getSingleton().getMaxRange(); ++i) {
            final String name = "agent_" + i;
            Inet4Address nameToIp = IpResolver.getSingleton().nameToIp(name);
            String ipToName = IpResolver.getSingleton().ipToName(nameToIp);
            assertEquals(name, ipToName);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIPv4AddressExhausted() {
        final String name = "cs_" + IpResolver.getSingleton().getMaxRange() + 1;
        // should throw an exception
        IpResolver.getSingleton().nameToIp(name);
    }

    @Test
    public void testArrayIntConversion() {
        try {
            Inet4Address testAddress = (Inet4Address) Inet4Address.getByName("10.1.0.1");
            byte testArray[] = testAddress.getAddress();
            assertEquals(167837697, IpResolver.getSingleton().addressArrayToFlat(testArray));

            byte testArray2[] = {10, 1, 0, 2};
            Assert.assertArrayEquals(testArray2, IpResolver.getSingleton().addressFlatToArray(167837698));
        } catch (UnknownHostException ex) {}
    }

    @Test
    public void testAddressAssignment() {
        Inet4Address ad1, ad2, ad3, ad4, ad5, ad6;
        ad1 = IpResolver.getSingleton().registerHost("veh_7");
        ad2 = IpResolver.getSingleton().registerHost("rsu_0");
        ad3 = IpResolver.getSingleton().registerHost("tl_100");
        ad4 = IpResolver.getSingleton().registerHost("cs_1000");
        ad5 = IpResolver.getSingleton().registerHost("tl_65534");
        boolean exceptionThrown = false;
        try {
            IpResolver.getSingleton().registerHost("tl_65535");
        } catch (RuntimeException ex) {
            exceptionThrown = true;
        }
        ad6 = IpResolver.getSingleton().registerHost("agent_1337");

        Assert.assertTrue(exceptionThrown);
        byte[] array1 = {10, 1, 0, 7};
        byte[] array2 = {10, 2, 0, 0};
        byte[] array3 = {10, 3, 0, 100};
        byte[] array4 = {10, 4, (byte) 3, (byte) 232};
        byte[] array5 = {10, 3, (byte) 255, (byte) 254};
        byte[] array6 = {10, 10, (byte) 5, (byte) 57};
        Assert.assertArrayEquals(array1, ad1.getAddress());
        Assert.assertArrayEquals(array2, ad2.getAddress());
        Assert.assertArrayEquals(array3, ad3.getAddress());
        Assert.assertArrayEquals(array4, ad4.getAddress());
        Assert.assertArrayEquals(array5, ad5.getAddress());
        Assert.assertArrayEquals(array6, ad6.getAddress());

        Inet4Address lookedUp = IpResolver.getSingleton().lookup("veh_7");
        assertEquals(ad1, lookedUp);
        lookedUp = IpResolver.getSingleton().lookup("veh_10");
        Assert.assertNull(lookedUp);
    }
}