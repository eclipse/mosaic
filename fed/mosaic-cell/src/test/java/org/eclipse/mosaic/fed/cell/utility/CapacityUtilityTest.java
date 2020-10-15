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

package org.eclipse.mosaic.fed.cell.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.fed.cell.config.CNetwork;
import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.fed.cell.junit.CellConfigurationRule;
import org.eclipse.mosaic.lib.objects.communication.CellConfiguration;
import org.eclipse.mosaic.rti.DATA;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Class that tests the bandwidth calculation of the cell.
 */
public class CapacityUtilityTest {
    private CellConfiguration nodeConfig;
    private CellConfiguration nodeConfig2;

    @Rule
    public CellConfigurationRule configRule = new CellConfigurationRule()
            .withNetworkConfig("configs/sample_network.json");

    @Before
    public void initializeCellConfiguration() {
        nodeConfig = new CellConfiguration("veh0", true, 1000, 2000);
        nodeConfig2 = new CellConfiguration("veh1", true, 44000, 25000);
    }

    @Test
    public void testCalculateNeededCapacity() {
        assertEquals(200 * DATA.BIT, CapacityUtility.calculateNeededCapacity(600, 3 * TIME.SECOND));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateNeededDelay() {
        assertEquals(200 * TIME.SECOND, CapacityUtility.calculateNeededDelay(4000, 20));
        assertEquals(0L, CapacityUtility.calculateNeededCapacity(0, 20));

        CapacityUtility.calculateNeededCapacity(2, 0);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateNeededDelayNegativeSize() {
        CapacityUtility.calculateNeededCapacity(-200, 300);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateNeededDelayNegativeDelay() {
        CapacityUtility.calculateNeededCapacity(200, -300);
    }

    @Test
    public void testIsCapacitySufficient() {
        // public static boolean isCapacitySufficient(TransmissionMode mode,
        // CNetworkProperties region, CellConfiguration nodeCellConfiguration, long neededBandwidth)
        CNetwork networkConfig = configRule.getNetworkConfig();

        // Downlink Unicast
        // capacity is sufficient
        assertTrue(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.DownlinkUnicast,
                        networkConfig.globalNetwork, nodeConfig,
                        (long) 100 * DATA.BIT)
        );
        // needed bandwidth exceeds the capacity of the region
        assertFalse(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.DownlinkUnicast,
                        networkConfig.globalNetwork, nodeConfig,
                        (long) 46000 * DATA.BIT)
        );
        // needed bandwidth exceeds the capacity of the node is
        assertFalse(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.DownlinkUnicast,
                        networkConfig.globalNetwork, nodeConfig,
                        (long) 4000 * DATA.BIT)
        );
        // needed bandwidth exceeds the capacity of the region
        assertFalse(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.DownlinkUnicast,
                        networkConfig.globalNetwork,
                        nodeConfig2,
                        (long) 44000 * DATA.BIT)
        );

        // Uplink Unicast
        // capacity is sufficient
        assertTrue(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.UplinkUnicast,
                        networkConfig.globalNetwork,
                        nodeConfig,
                        (long) 100 * DATA.BIT)
        );
        // needed bandwidth exceeds the capacity of the region and the node
        assertFalse(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.UplinkUnicast,
                        networkConfig.globalNetwork,
                        nodeConfig,
                        (long) 30000 * DATA.BIT)
        );
        // needed bandwidth exceeds the capacity of the node
        assertFalse(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.UplinkUnicast,
                        networkConfig.globalNetwork,
                        nodeConfig,
                        (long) 4000 * DATA.BIT)
        );
        // needed bandwidth exceeds the capacity of the region
        assertFalse(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.UplinkUnicast,
                        networkConfig.globalNetwork,
                        nodeConfig2,
                        (long) 24000 * DATA.BIT)
        );

        // Downlink Multicast
        // capacity is sufficient
        assertTrue(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.DownlinkMulticast,
                        networkConfig.globalNetwork,
                        nodeConfig,
                        (long) 100 * DATA.BIT)
        );
        // needed bandwidth exceeds the multicast capacity of the region and the node
        assertFalse(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.DownlinkMulticast,
                        networkConfig.globalNetwork,
                        nodeConfig,
                        (long) 26000 * DATA.BIT)
        );
        // the case the needed bandwidth exceeds the multicast capacity is
        // not considered because the node capacity is not considered for the multicast mode

        // check null handling
        assertFalse(
                CapacityUtility.isCapacitySufficient(null,
                        networkConfig.globalNetwork,
                        nodeConfig,
                        (long) 100 * DATA.BIT)
        );
        assertFalse(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.DownlinkUnicast,
                        null, nodeConfig,
                        (long) 100 * DATA.BIT)
        );
        assertFalse(
                CapacityUtility.isCapacitySufficient(
                        TransmissionMode.DownlinkUnicast,
                        networkConfig.globalNetwork,
                        null,
                        (long) 44000 * DATA.BIT)
        );
    }

    @Test
    public void testConsumeCapacity() {
        CNetwork networkConfig = configRule.getNetworkConfig();

        CapacityUtility.consumeCapacity(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, nodeConfig, 200 * DATA.BIT);
        assertEquals(1800 * DATA.BIT, nodeConfig.getAvailableUlBitrate());
        assertEquals(22800 * DATA.BIT, networkConfig.globalNetwork.uplink.capacity);

        CapacityUtility.consumeCapacity(TransmissionMode.DownlinkUnicast, networkConfig.globalNetwork, nodeConfig, 300 * DATA.BIT);
        assertEquals(700 * DATA.BIT, nodeConfig.getAvailableDlBitrate());
        assertEquals(41700 * DATA.BIT, networkConfig.globalNetwork.downlink.capacity);

        CapacityUtility.consumeCapacity(TransmissionMode.DownlinkMulticast, networkConfig.globalNetwork, nodeConfig, 400 * DATA.BIT);
        assertEquals(700 * DATA.BIT, nodeConfig.getAvailableDlBitrate());
        assertEquals(41300 * DATA.BIT, networkConfig.globalNetwork.downlink.capacity);

        // consume negative capacity (should not change the actual capacity)
        CapacityUtility.consumeCapacity(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, nodeConfig, -200 * DATA.BIT);
        assertEquals(1800 * DATA.BIT, nodeConfig.getAvailableUlBitrate());
        assertEquals(22800 * DATA.BIT, networkConfig.globalNetwork.uplink.capacity);

        CapacityUtility.consumeCapacity(TransmissionMode.DownlinkUnicast, networkConfig.globalNetwork, nodeConfig, -200 * DATA.BIT);
        assertEquals(700 * DATA.BIT, nodeConfig.getAvailableDlBitrate());
        assertEquals(41300 * DATA.BIT, networkConfig.globalNetwork.downlink.capacity);

        CapacityUtility.consumeCapacity(TransmissionMode.DownlinkMulticast, networkConfig.globalNetwork, nodeConfig, -200 * DATA.BIT);
        assertEquals(700 * DATA.BIT, nodeConfig.getAvailableDlBitrate());
        assertEquals(41300 * DATA.BIT, networkConfig.globalNetwork.downlink.capacity);

        // check null
        CapacityUtility.consumeCapacity(null, networkConfig.globalNetwork, nodeConfig, 200 * DATA.BIT);
        assertEquals(700 * DATA.BIT, nodeConfig.getAvailableDlBitrate());
        assertEquals(41300 * DATA.BIT, networkConfig.globalNetwork.downlink.capacity);

        CapacityUtility.consumeCapacity(TransmissionMode.DownlinkUnicast, null, nodeConfig, 200 * DATA.BIT);
        assertEquals(700 * DATA.BIT, nodeConfig.getAvailableDlBitrate());
        assertEquals(41300 * DATA.BIT, networkConfig.globalNetwork.downlink.capacity);

        CapacityUtility.consumeCapacity(TransmissionMode.DownlinkUnicast, networkConfig.globalNetwork, null, 200 * DATA.BIT);
        assertEquals(700 * DATA.BIT, nodeConfig.getAvailableDlBitrate());
        assertEquals(41300 * DATA.BIT, networkConfig.globalNetwork.downlink.capacity);

        // consume more capacity as available
        CapacityUtility.consumeCapacity(TransmissionMode.DownlinkUnicast, networkConfig.globalNetwork, nodeConfig, 800 * DATA.BIT);
        assertEquals(-100 * DATA.BIT, nodeConfig.getAvailableDlBitrate());

        CapacityUtility.consumeCapacity(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, nodeConfig, 23000 * DATA.BIT);
        assertEquals(-200 * DATA.BIT, networkConfig.globalNetwork.uplink.capacity);
    }

    @Test
    public void testFreeCapacityUp() {
        // public static void freeCapacityUp(CNetworkProperties region, CellConfiguration nodeCellConfiguration, long freed)
        CNetwork networkConfig = configRule.getNetworkConfig();
        CapacityUtility.consumeCapacity(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, nodeConfig, 800 * DATA.BIT);
        CapacityUtility.freeCapacityUp(networkConfig.globalNetwork, nodeConfig, 500 * DATA.BIT);

        assertEquals(1700 * DATA.BIT, nodeConfig.getAvailableUlBitrate());
        assertEquals(22700 * DATA.BIT, networkConfig.globalNetwork.uplink.capacity);
        try {
            //noinspection ConstantConditions
            CapacityUtility.freeCapacityUp(null, nodeConfig, 800 * DATA.BIT);
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
        CapacityUtility.freeCapacityUp(networkConfig.globalNetwork, null, 800 * DATA.BIT);
        CapacityUtility.freeCapacityUp(networkConfig.globalNetwork, nodeConfig, 500 * DATA.BIT);
        assertEquals(2000 * DATA.BIT, nodeConfig.getAvailableUlBitrate());
        assertEquals(23000 * DATA.BIT, networkConfig.globalNetwork.uplink.capacity);
    }

    @Test
    public void testIsAvailableUplink() {
        // public static boolean isBusy(TransmissionMode mode, CNetworkProperties region, CellConfiguration nodeCellConfiguration)
        CNetwork networkConfig = configRule.getNetworkConfig();
        assertTrue(CapacityUtility.isAvailable(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, nodeConfig));
        NodeCapacityUtility.consumeCapacityUp(nodeConfig, 1950 * DATA.BIT);
        assertFalse(CapacityUtility.isAvailable(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, nodeConfig));
        NodeCapacityUtility.freeCapacityUp(nodeConfig, 1950 * DATA.BIT);
        RegionCapacityUtility.consumeCapacityUp(networkConfig.globalNetwork, 22500 * DATA.BIT);
        assertFalse(CapacityUtility.isAvailable(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, nodeConfig));
        assertFalse(CapacityUtility.isAvailable(null, networkConfig.globalNetwork, nodeConfig));
        assertFalse(CapacityUtility.isAvailable(TransmissionMode.UplinkUnicast, null, nodeConfig));
        assertFalse(CapacityUtility.isAvailable(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, null));
    }

    @Test
    public void testIsBusyDownlink() {
        CNetwork networkConfig = configRule.getNetworkConfig();
        assertTrue(CapacityUtility.isAvailable(TransmissionMode.DownlinkUnicast, networkConfig.globalNetwork, nodeConfig));
        NodeCapacityUtility.consumeCapacityDown(nodeConfig, 950 * DATA.BIT);
        // check when node is busy
        assertFalse(CapacityUtility.isAvailable(TransmissionMode.DownlinkUnicast, networkConfig.globalNetwork, nodeConfig));
        NodeCapacityUtility.freeCapacityDown(nodeConfig, 950 * DATA.BIT);
        // check when region is busy
        RegionCapacityUtility.consumeCapacityDown(networkConfig.globalNetwork, 41500 * DATA.BIT);
        assertFalse(CapacityUtility.isAvailable(TransmissionMode.DownlinkUnicast, networkConfig.globalNetwork, nodeConfig));
        // check multicast when region is busy
        assertFalse(CapacityUtility.isAvailable(TransmissionMode.DownlinkMulticast, networkConfig.globalNetwork, nodeConfig));
        // check multicast when neither region nor node are busy
        RegionCapacityUtility.freeCapacityDown(networkConfig.globalNetwork, 41500 * DATA.BIT);
        assertTrue(CapacityUtility.isAvailable(TransmissionMode.DownlinkMulticast, networkConfig.globalNetwork, nodeConfig));
        // check multicast when only the node is busy (should return true because the node is considered in the multicast cast)
        NodeCapacityUtility.consumeCapacityDown(nodeConfig, 950 * DATA.BIT);
        assertTrue(CapacityUtility.isAvailable(TransmissionMode.DownlinkMulticast, networkConfig.globalNetwork, nodeConfig));
    }

    @Test
    public void testIsAvailableCapacity() {
        // check null as arguments
        CNetwork networkConfig = configRule.getNetworkConfig();
        assertEquals(0L, CapacityUtility.availableCapacity(null, networkConfig.globalNetwork, nodeConfig));
        assertEquals(0L, CapacityUtility.availableCapacity(TransmissionMode.UplinkUnicast, null, nodeConfig));
        assertEquals(0L, CapacityUtility.availableCapacity(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, null));
        // case where the node has less available capacity compared to the capacity of the region
        assertEquals(
                1000 * DATA.BIT,
                CapacityUtility.availableCapacity(TransmissionMode.DownlinkUnicast, networkConfig.globalNetwork, nodeConfig)
        );
        assertEquals(
                2000 * DATA.BIT,
                CapacityUtility.availableCapacity(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, nodeConfig)
        );
        // case where the region has less available capacity compared to the capacity of the node
        RegionCapacityUtility.consumeCapacityUp(networkConfig.globalNetwork, 22500 * DATA.BIT);
        assertEquals(
                500 * DATA.BIT,
                CapacityUtility.availableCapacity(TransmissionMode.UplinkUnicast, networkConfig.globalNetwork, nodeConfig)
        );
        RegionCapacityUtility.consumeCapacityDown(networkConfig.globalNetwork, 41500 * DATA.BIT);
        assertEquals(
                500 * DATA.BIT,
                CapacityUtility.availableCapacity(TransmissionMode.DownlinkUnicast, networkConfig.globalNetwork, nodeConfig)
        );
    }
}
