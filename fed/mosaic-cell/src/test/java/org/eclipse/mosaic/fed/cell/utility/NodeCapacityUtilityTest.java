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

import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.lib.objects.communication.CellConfiguration;
import org.eclipse.mosaic.rti.DATA;

import org.junit.Before;
import org.junit.Test;

/**
 * Class that tests the bandwidth calculation of the cell.
 */
public class NodeCapacityUtilityTest {
    private CellConfiguration config;

    @Before
    public void initializeCellConfiguration() {
        config = new CellConfiguration("veh0", true, 1000 * DATA.BIT, 2000 * DATA.BIT);
    }

    @Test
    public void testCellConfigurationInitialisation() {
        assertEquals(config.getNodeId(), "veh0");
        assertEquals(config.getAvailableDlBitrate(), 1000 * DATA.BIT);
        assertEquals(config.getAvailableUlBitrate(), 2000 * DATA.BIT);
    }

    @Test
    public void testIsCapacitySufficient() {
        assertTrue(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.DownlinkUnicast, config, 500 * DATA.BIT));
        assertFalse(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.DownlinkUnicast, config, 1500 * DATA.BIT));
        assertTrue(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.UplinkUnicast, config, 1500 * DATA.BIT));
        assertFalse(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.UplinkUnicast, config, 2500 * DATA.BIT));
        assertTrue(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.DownlinkMulticast, config, 1500 * DATA.BIT));
        assertTrue(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.DownlinkMulticast, config, 500 * DATA.BIT));
        assertTrue(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.DownlinkMulticast, config, 4000 * DATA.BIT));

        assertFalse(NodeCapacityUtility.isCapacitySufficient(null, config, 1500 * DATA.BIT));
        assertFalse(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.UplinkUnicast, config, -20 * DATA.BIT));
        assertTrue(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.DownlinkMulticast, null, 1500 * DATA.BIT));
        assertFalse(NodeCapacityUtility.isCapacitySufficient(TransmissionMode.DownlinkUnicast, null, 1500 * DATA.BIT));
    }


    @Test
    public void testConsumeCapacityUp() {
        // Test behaviour when the consume value is larger than the available capacity
        NodeCapacityUtility.consumeCapacityUp(config, 3000 * DATA.BIT);
        assertEquals(-1000, config.getAvailableUlBitrate());
        // Test the behaviour when the consume value is smaller than the available capacity
        NodeCapacityUtility.freeCapacityUp(config, 3000 * DATA.BIT);
        NodeCapacityUtility.consumeCapacityUp(config, 200 * DATA.BIT);
        assertEquals(1800, config.getAvailableUlBitrate());
        NodeCapacityUtility.consumeCapacityUp(null, 200 * DATA.BIT);
        NodeCapacityUtility.consumeCapacityUp(config, -200 * DATA.BIT);
        assertEquals(1800, config.getAvailableUlBitrate());
    }

    @Test
    public void testConsumeCapacityDown() {
        // Test behaviour when the consume value is larger than the available capacity
        NodeCapacityUtility.consumeCapacityDown(config, 2000 * DATA.BIT);
        assertEquals(-1000, config.getAvailableDlBitrate());
        // Test behaviour when the consume value is smaller than the available capacity
        NodeCapacityUtility.freeCapacityDown(config, 2000 * DATA.BIT);
        NodeCapacityUtility.consumeCapacityDown(config, 300 * DATA.BIT);
        assertEquals(700, config.getAvailableDlBitrate());
        NodeCapacityUtility.consumeCapacityDown(null, 300 * DATA.BIT);
        NodeCapacityUtility.consumeCapacityDown(config, -300 * DATA.BIT);
        assertEquals(700, config.getAvailableDlBitrate());

    }

    /*
     * The maximum UL capacity of the nodeConfig is 2000.
     */
    @Test
    public void testFreeCapacityUp() {
        // free more capacity than available
        NodeCapacityUtility.freeCapacityUp(config, 200 * DATA.BIT);
        assertEquals(2000, config.getAvailableUlBitrate());
        // free available capacity
        NodeCapacityUtility.consumeCapacityUp(config, 600 * DATA.BIT);
        NodeCapacityUtility.freeCapacityUp(config, 200 * DATA.BIT);
        assertEquals(1600, config.getAvailableUlBitrate());
        // other tests
        NodeCapacityUtility.freeCapacityUp(null, 200 * DATA.BIT);
        NodeCapacityUtility.freeCapacityUp(config, -100 * DATA.BIT);
        assertEquals(1600, config.getAvailableUlBitrate());
    }

    /*
     * The maximum DL capacity of the nodeConfig is 1000
     */
    @Test
    public void testFreeCapacityDown() {
        // free more capacity than available
        NodeCapacityUtility.freeCapacityDown(config, 200 * DATA.BIT);
        assertEquals(1000, config.getAvailableDlBitrate());
        // free available capacity
        NodeCapacityUtility.consumeCapacityDown(config, 800 * DATA.BIT);
        NodeCapacityUtility.freeCapacityDown(config, 200 * DATA.BIT);
        assertEquals(400, config.getAvailableDlBitrate());
        // other tests
        NodeCapacityUtility.freeCapacityDown(null, 200 * DATA.BIT);
        NodeCapacityUtility.freeCapacityDown(config, -100 * DATA.BIT);
        assertEquals(400, config.getAvailableDlBitrate());

    }

    @Test
    public void testIsAvailable() {
        assertTrue(NodeCapacityUtility.isAvailable(TransmissionMode.DownlinkUnicast, config));
        assertTrue(NodeCapacityUtility.isAvailable(TransmissionMode.DownlinkMulticast, config));
        assertTrue(NodeCapacityUtility.isAvailable(TransmissionMode.UplinkUnicast, config));

        assertFalse(NodeCapacityUtility.isAvailable(null, config));
        assertTrue(NodeCapacityUtility.isAvailable(TransmissionMode.DownlinkMulticast, null));
        assertFalse(NodeCapacityUtility.isAvailable(TransmissionMode.UplinkUnicast, null));
        assertFalse(NodeCapacityUtility.isAvailable(TransmissionMode.DownlinkUnicast, null));
    }

    @Test
    public void testGetAvailableUlCapacity() {
        assertEquals(2000 * DATA.BIT, NodeCapacityUtility.getAvailableUlCapacity(config));
        assertEquals(0L, NodeCapacityUtility.getAvailableUlCapacity(null));
    }

    @Test
    public void testGetAvailableDlCapacity() {
        assertEquals(1000 * DATA.BIT, NodeCapacityUtility.getAvailableDlCapacity(config));
        assertEquals(0L, NodeCapacityUtility.getAvailableDlCapacity(null));
    }
}
