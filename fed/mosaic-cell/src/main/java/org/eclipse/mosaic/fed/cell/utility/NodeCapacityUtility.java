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

import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.lib.objects.communication.CellConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static methods for handling the bandwidth calculation.
 */
public final class NodeCapacityUtility {

    private static final Logger log = LoggerFactory.getLogger(NodeCapacityUtility.class);

    /**
     * Checks whether the bandwidth is sufficient enough to perform the transmission.
     *
     * @param mode                  The mode of the transmission
     * @param nodeCellConfiguration The cell configuration of the node that is checked
     * @param neededBandwidthInBps  The needed bandwidth in bit/s for the transmission
     * @return Returns true if the bandwidth of the node is sufficient for the transmission,
     *         returns false if the bandwidth of the node is not sufficient for the transmission
     */
    public static boolean isCapacitySufficient(TransmissionMode mode, CellConfiguration nodeCellConfiguration, long neededBandwidthInBps) {
        if (neededBandwidthInBps < 0 || mode == null) {
            return false;
        }

        if (mode.equals(TransmissionMode.DownlinkMulticast)) {
            // In multicast mode the Node Bandwidth is not considered therefore the transmission is possible
            return true;
        } else {
            if (nodeCellConfiguration == null) {
                return false;
            } else if (mode.isUplink()) {
                return nodeCellConfiguration.getAvailableUlBitrate() >= neededBandwidthInBps;
            } else if (mode.isDownlink()) {
                return nodeCellConfiguration.getAvailableDlBitrate() >= neededBandwidthInBps;
            } else {
                // Error Handling
                return false;
            }
        }
    }

    /**
     * This method consumes the capacity of a node in the uplink direction.
     *
     * @param nodeCellConfiguration The cell configuration of the node whose capacity is consumed
     * @param consumeInBps          The capacity to consume in bit/s. The capacity has to be larger than zero to actually consume bandwidth.
     *                              If the value for consume is negative, the cell configuration remains unchanged.
     */
    public static void consumeCapacityUp(CellConfiguration nodeCellConfiguration, long consumeInBps) {
        if (nodeCellConfiguration == null) {
            log.warn("Could not consume the Ul bandwidth because the cell configuration of the the node is null");
            return;
        }
        if (consumeInBps > 0) {
            nodeCellConfiguration.consumeUl(consumeInBps);
        } else {
            log.warn(String.format("Could not consume the UL capacity because the value to consume is not larger than 0, it is %d", consumeInBps));
        }
    }

    /**
     * This method consumes the capacity of a node in the downlink direction.
     *
     * @param nodeCellConfiguration   The cell configuration of the node whose capacity is consumed
     * @param bandwidthToConsumeInBps The capacity to consume in bit/s. The capacity has to be larger
     *                                than zero to actually consume bandwidth. If the value for consume
     *                                is negative, the cell configuration remains unchanged.
     */
    public static void consumeCapacityDown(CellConfiguration nodeCellConfiguration, long bandwidthToConsumeInBps) {
        if (nodeCellConfiguration == null) {
            log.warn("Could not consume the Dl bandwidth because the cell configuration of the node is null");
            return;
        }

        if (bandwidthToConsumeInBps > 0) {
            nodeCellConfiguration.consumeDl(bandwidthToConsumeInBps);
        } else {
            log.warn(String.format("Could not consume the DL capacity because the value to consume is not larger than 0, it is %d", bandwidthToConsumeInBps));
        }
    }

    /**
     * Frees the bandwidth for a given node in the uplink direction.
     * This method should normally be called when a message that consumed the
     * bandwidth was successfully delivered.
     *
     * @param nodeCellConfiguration The cell configuration of the node whose bandwidth should be freed.
     * @param bandwidthToFreeInBps  The capacity that should be freed in bit/s. The capacity has to be
     *                              larger than zero to actually consume bandwidth. If the value for
     *                              free is negative, the cell configuration remains unchanged.
     */
    public static void freeCapacityUp(CellConfiguration nodeCellConfiguration, long bandwidthToFreeInBps) {
        if (nodeCellConfiguration == null) {
            log.warn("Could not free the Ul bandwidth because the cell configuration of the node is null");
            return;
        }

        if (bandwidthToFreeInBps < 0) {
            log.warn("Could not free the Ul bandwidth because the free value was smaller than 0, it was: {}", bandwidthToFreeInBps);
            return;
        }

        if (nodeCellConfiguration.getAvailableUlBitrate() + bandwidthToFreeInBps <= nodeCellConfiguration.getMaxUlBitrate()) {
            nodeCellConfiguration.freeUl(bandwidthToFreeInBps);
        } else {
            nodeCellConfiguration.freeUl(nodeCellConfiguration.getMaxUlBitrate() - nodeCellConfiguration.getAvailableUlBitrate());
            log.warn("Could not free the whole free value because it would exceed the maximum UL capacity of {} bit/s of the node {}.", nodeCellConfiguration.getMaxUlBitrate(), nodeCellConfiguration.getNodeId());
        }
        log.trace("Freeing {} bit/s upstream for node {} (capacity now: {} bit/s)",
                bandwidthToFreeInBps, nodeCellConfiguration.getNodeId(), nodeCellConfiguration.getAvailableUlBitrate());
    }

    /**
     * Frees the bandwidth for a given node in the downlink direction.
     * This method should normally be called when a message that consumed the
     * bandwidth was successfully delivered.
     *
     * @param nodeCellConfiguration The cell configuration of the node whose bandwidth should be freed.
     * @param bandwidthToFreeInBps  The capacity that should be freed in bit/s. The capacity has to be
     *                              larger than zero to actually consume bandwidth. If the value for
     *                              free is negative, the cell configuration remains unchanged.
     */
    public static void freeCapacityDown(CellConfiguration nodeCellConfiguration, long bandwidthToFreeInBps) {
        if (nodeCellConfiguration == null) {
            log.warn("Could not free the Dl bandwidth because the cell configuration of the node is null");
            return;
        }
        if (bandwidthToFreeInBps < 0) {
            log.warn("Could not free the Dl bandwidth because the free value was smaller than 0, it was: {}", bandwidthToFreeInBps);
            return;
        }

        if (nodeCellConfiguration.getAvailableDlBitrate() + bandwidthToFreeInBps <= nodeCellConfiguration.getMaxDlBitrate()) {
            nodeCellConfiguration.freeDl(bandwidthToFreeInBps);
        } else {
            nodeCellConfiguration.freeDl(nodeCellConfiguration.getMaxDlBitrate() - nodeCellConfiguration.getAvailableDlBitrate());
            log.warn("Could not free the whole free value because it would exceed the maximum DL capacity of {} bit/s of the node {}.", nodeCellConfiguration.getMaxDlBitrate(), nodeCellConfiguration.getNodeId());
        }
        log.trace("Freeing {} bit/s downstream for node {} (capacity now: {} bit/s)",
                bandwidthToFreeInBps, nodeCellConfiguration.getNodeId(), nodeCellConfiguration.getAvailableDlBitrate());
    }

    /**
     * This methods checks whether the node has enough capacity left to allow a new transmission with this node.
     * If the available capacity of the node is below a threshold value, a new transmission is not possible and the node
     * is not available for a new transmission.
     *
     * @param mode                  The requested transmission of the transmission that the node should participate in.
     * @param nodeCellConfiguration The cellConfiguration of the node that is checked for availability.
     * @return The method returns true, if the available capacity of the node is above the threshold for
     *         allowing a new transmission. Otherwise, the method returns false.
     */

    public static boolean isAvailable(TransmissionMode mode, CellConfiguration nodeCellConfiguration) {
        if (mode == null) {
            log.warn("Could not check whether a node is busy because the transmission mode is null");
            return false;
        }
        if (mode.equals(TransmissionMode.DownlinkMulticast)) {
            // Do not consider the node bandwidth in the multicast case
            return true;
        }
        if (nodeCellConfiguration == null) {
            log.warn("Could not check whether a node is busy because the cell configuration of the node is null");
            return false;
        }

        if (mode.isUplink()) {
            return nodeCellConfiguration.ulPossible();
        } else if (mode.equals(TransmissionMode.DownlinkUnicast)) {
            return nodeCellConfiguration.dlPossible();
        } else {
            log.debug("Mode {} is not supported", mode);
            return false;
        }
    }

    /**
     * Returns the available bandwidth of a node in the uplink direction.
     *
     * @param nodeCellConfiguration The cell configuration of the node whose bandwidth is requested.
     * @return The available uplink bandwidth of the node with the id in bit/s.
     *         If the node is not known, the returned value is 0.
     */
    public static long getAvailableUlCapacity(CellConfiguration nodeCellConfiguration) {
        if (nodeCellConfiguration == null) {
            log.warn("Could not return the available Ul capacity because the cell configuration of the node is null");
            return 0;
        }
        return nodeCellConfiguration.getAvailableUlBitrate();
    }

    /**
     * Returns the available bandwidth of a node in the downlink direction.
     *
     * @param nodeCellConfiguration The cell configuration of the node whose bandwidth is requested.
     * @return The available downlink bandwidth of the node with the id in bit/s.
     *         If the node is not known, the returned value is 0.
     */
    public static long getAvailableDlCapacity(CellConfiguration nodeCellConfiguration) {
        if (nodeCellConfiguration == null) {
            log.warn("The available Dl capacity could not be found for node because the cell configuration was null");
            return 0;
        }
        return nodeCellConfiguration.getAvailableDlBitrate();
    }
}
