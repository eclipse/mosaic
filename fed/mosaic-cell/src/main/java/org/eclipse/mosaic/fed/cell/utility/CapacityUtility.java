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

import org.eclipse.mosaic.fed.cell.config.CCell;
import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.fed.cell.data.ConfigurationData;
import org.eclipse.mosaic.lib.objects.UnitNameGenerator;
import org.eclipse.mosaic.lib.objects.communication.CellConfiguration;
import org.eclipse.mosaic.lib.objects.v2x.MessageStreamRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.rti.DATA;
import org.eclipse.mosaic.rti.TIME;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static methods for handling the bandwidth calculation.
 */
@SuppressWarnings(value = {"NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"}, justification = "filled by GSON")
public final class CapacityUtility {

    private static final Logger log = LoggerFactory.getLogger(CapacityUtility.class);

    /**
     * Calculates the needed bandwidth for one message according to the messageSize and the delay.
     * for the same size, a message with a short delay would consume more bandwidth during transmission.
     *
     * @param messageSizeInBit messageSize given in [bit]
     * @param delayInNs        delay as transmissionTime, given in [ns]
     * @return The needed bandwidth in [bit/s]
     * @throws IllegalArgumentException if delay or message size were set to invalid values
     */
    public static long calculateNeededCapacity(long messageSizeInBit, long delayInNs) throws IllegalArgumentException {
        if (delayInNs <= 0 || messageSizeInBit <= 0) {
            throw new IllegalArgumentException("Could not calculate the needed bandwidth because an argument was smaller"
                    + " than or equal to 0, messageSize:" + messageSizeInBit + ", delay: " + delayInNs);
        }
        double delayInS = (double) delayInNs / TIME.SECOND;
        final double bwInBps = messageSizeInBit / delayInS;
        return (long) bwInBps;
    }

    /**
     * Calculates the needed bandwidth for one message according to the messageSize and the available bandwidth.
     *
     * @param messageSizeInBit   messageSize given in [bit].
     * @param availableBandwidth Available bandwidth in [bit/s].
     * @return The needed delay in [ns]
     */
    public static long calculateNeededDelay(long messageSizeInBit, long availableBandwidth) {
        double delay;
        if (availableBandwidth <= 0 || messageSizeInBit <= 0) {
            throw new IllegalArgumentException("Could not calculate the needed delay because an argument was smaller"
                    + " than or equal to 0, messageSize:" + messageSizeInBit + ", available bandwidth: " + availableBandwidth);
        } else {
            delay = messageSizeInBit / (double) availableBandwidth;
        }
        return (long) (delay * TIME.SECOND);
    }

    /**
     * Checks if the bandwidth of the given region and of the given node
     * is sufficient to perform the transmission.
     *
     * @param region                The region to be checked for available bandwidth
     * @param mode                  The mode of the transmission that should be checked
     * @param nodeCellConfiguration The cell configuration of the given node that is checked
     * @param neededBandwidth       The needed bandwidth for the transmission
     * @return true if the bandwidth in the system is sufficient for the transmission, false if the bandwidth is not sufficient
     */
    public static boolean isCapacitySufficient(TransmissionMode mode, CNetworkProperties region,
                                               CellConfiguration nodeCellConfiguration, long neededBandwidth) {
        return NodeCapacityUtility.isCapacitySufficient(mode, nodeCellConfiguration, neededBandwidth)
                && RegionCapacityUtility.isCapacitySufficient(mode, region, neededBandwidth);
    }

    /**
     * Consumes some of the available bandwidth based on the given message.
     *
     * @param region                The region to consume the bandwidth from
     * @param mode                  Up or downstream
     * @param nodeCellConfiguration The cell configuration of the node that is involved in the communication
     * @param consume               The amount of consumed bandwidth in bit/s
     */
    public static void consumeCapacity(TransmissionMode mode, CNetworkProperties region,
                                       CellConfiguration nodeCellConfiguration, long consume) {
        if (mode == null) {
            log.warn("Could not consume capacity because the transmission mode was null");
            return;
        }
        if (region == null) {
            log.warn("Could not consume capacity because the region is null");
            return;
        }
        if (nodeCellConfiguration == null && !mode.equals(TransmissionMode.DownlinkMulticast)) {
            log.warn("Could not consume capacity because the cell configuration of the node is null");
            return;
        }
        if (consume <= 0) {
            log.warn("Could not consume capacity because the capacity to consume is {}, which is smaller than or equal to 0.", consume);
            return;
        }
        if (mode.equals(TransmissionMode.DownlinkMulticast)) {
            RegionCapacityUtility.consumeCapacityDown(region, consume);
        } else {
            if (mode.isUplink()) {
                RegionCapacityUtility.consumeCapacityUp(region, consume);
                NodeCapacityUtility.consumeCapacityUp(nodeCellConfiguration, consume);
            } else {
                RegionCapacityUtility.consumeCapacityDown(region, consume);
                NodeCapacityUtility.consumeCapacityDown(nodeCellConfiguration, consume);
            }
        }
    }

    /**
     * Helper-function to get the effective message length in bits.
     *
     * @param msg V2X message.
     * @return The length of the V2X message.
     */
    public static long getMessageLengthWithHeaders(V2xMessage msg, String senderOrReceiver) {
        final CCell.CHeaderLengths headerLengths = ConfigurationData.INSTANCE.getCellConfig().headerLengths;
        final long linkLayerHeader;
        if (UnitNameGenerator.isServer(senderOrReceiver) || UnitNameGenerator.isTmc(senderOrReceiver)) {
            // let's assume everything is connected via cellular link, except servers and tmcs which are connected with the backbone
            linkLayerHeader = headerLengths.ethernetHeader;
        } else {
            linkLayerHeader = headerLengths.cellularHeader;
        }
        switch (msg.getRouting().getDestination().getProtocolType()) {
            case UDP:
                return linkLayerHeader
                        + headerLengths.ipHeader
                        + headerLengths.udpHeader
                        + msg.getPayLoad().getEffectiveLength() * DATA.BYTE;
            case TCP:
            default:
                return linkLayerHeader
                        + headerLengths.ipHeader
                        + headerLengths.tcpHeader
                        + msg.getPayLoad().getEffectiveLength() * DATA.BYTE;
        }
    }

    /**
     * Checks whether a new transmission is possible or whether the minimal bandwidths are undershot.
     * This method is used to check whether the delay should be extended artificially or
     * whether the packet is dropped. When too much bandwidth is already used (the region or the node is not available) it
     * might be better to drop/queue the packet and wait until the bandwidth becomes available again.
     *
     * @param region                The current region of the node
     * @param mode                  The desired transmission mode
     * @param nodeCellConfiguration The cell configuration of the node that is involved in the communication
     * @return Returns true when the available bandwidth is above the minimal threshold,
     * returns false when the available bandwidth is not above the minimal threshold.
     */
    public static boolean isAvailable(TransmissionMode mode, CNetworkProperties region, CellConfiguration nodeCellConfiguration) {
        return RegionCapacityUtility.isAvailable(mode, region) && NodeCapacityUtility.isAvailable(mode, nodeCellConfiguration);
    }

    /**
     * The method returns the available bandwidth for a node in a region. The
     * available bandwidth depends on the left capacity in the region and the
     * left bandwidth of the node.
     *
     * @param mode                  The mode of the transmission
     * @param region                The region where the transmission will take place
     * @param nodeCellConfiguration The cell configuration of the node whose bandwidth is checked
     * @return The available bandwidth for a transmission of a node in the region.
     */
    public static long availableCapacity(TransmissionMode mode, CNetworkProperties region, CellConfiguration nodeCellConfiguration) {
        if (mode == null) {
            log.warn("Could not return the available capacity because the mode is null");
            return 0;
        }
        if (region == null) {
            log.warn("Could not return the available capacity because the region is null");
            return 0;
        }
        if (nodeCellConfiguration == null && mode != TransmissionMode.DownlinkMulticast) {
            log.warn("Could not return the available capacity because the cell configuration of the node is null");
            return 0;
        }

        // Depending on the transmission mode the two variables have different meanings.
        // Hence, the names are so generic.
        long bandwidth1;
        long bandwidth2;
        switch (mode) {
            case UplinkUnicast:
                bandwidth1 = nodeCellConfiguration.getAvailableUplinkBitrate();
                bandwidth2 = region.uplink.capacity;
                break;
            case DownlinkUnicast:
                bandwidth1 = nodeCellConfiguration.getAvailableDownlinkBitrate();
                bandwidth2 = region.downlink.capacity;
                break;
            case DownlinkMulticast:
                bandwidth1 = (long) (region.downlink.multicast.usableCapacity * region.downlink.maxCapacity);
                bandwidth2 = region.downlink.capacity;
                break;
            default:
                log.warn("The detected transmission mode {} is unknown while checking the available capacity", mode);
                return 0;
        }

        return Math.min(bandwidth1, bandwidth2);
    }

    /**
     * Get the bandwidth for the stream.
     *
     * @param v2xMessage V2X message.
     * @return Bandwidth for the streaming in [bit/s].
     */
    public static long getStreamingBandwidth(V2xMessage v2xMessage) {
        if (v2xMessage == null || !(v2xMessage.getRouting() instanceof MessageStreamRouting)) {
            return 0;
        }
        MessageStreamRouting routing = (MessageStreamRouting) v2xMessage.getRouting();
        return routing.getStreamingBandwidth();
    }
}
