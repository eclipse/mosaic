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

import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Static methods for handling the bandwidth calculation.
 */
@SuppressWarnings(value = {"NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"}, justification = "filled by GSON")
public final class RegionCapacityUtility {

    private static final Logger log = LoggerFactory.getLogger(RegionCapacityUtility.class);

    private static final double MINIMAL_BANDWIDTH_FACTOR = 0.1;

    /**
     * Checks if the message is deliverable for the given region depending on needed bandwidth, the available
     * capacity and the transmission mode.
     *
     * @param region               The region to be checked for available capacity
     * @param neededBandwidthInBps The capacity in bit/s needed for the transmission
     * @return true if capacity is sufficient, false if not
     */
    public static boolean isCapacitySufficient(TransmissionMode mode, CNetworkProperties region, long neededBandwidthInBps) {
        if (mode == null) {
            log.warn("Could not compute whether the capacity is sufficient because the mode is null.");
            return false;
        }
        if (region == null) {
            log.warn("Could not compute whether the capacity is sufficient because the region is null, the mode is {}", mode);
            return false;
        }
        if (mode.equals(TransmissionMode.UplinkUnicast)) { // uplink
            return region.uplink.capacity >= neededBandwidthInBps;
        } else { // downlink
            if (region.downlink.capacity >= neededBandwidthInBps) {
                if (mode.equals(TransmissionMode.DownlinkMulticast)) {
                    return region.downlink.maxCapacity * region.downlink.multicast.usableCapacity >= neededBandwidthInBps;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the message is deliverable for the given region in dependency of the needed bandwidth and the available
     * capacity in the uplink direction.
     *
     * @param region               The region to be checked for available capacity
     * @param neededBandwidthInBps The capacity in bit/s needed for the transmission
     * @return true if capacity is sufficient in uplink direction, false if not
     */
    public static boolean isCapacitySufficientUp(CNetworkProperties region, long neededBandwidthInBps) {
        return isCapacitySufficient(TransmissionMode.UplinkUnicast, region, neededBandwidthInBps);
    }

    /**
     * Checks if the message is deliverable for the given region in dependency of the needed bandwidth and the available
     * capacity in the downlink direction.
     *
     * @param region               The region to be checked for available capacity
     * @param neededBandwidthInBps The capacity in bit/s needed for the transmission
     * @return true if capacity is sufficient in downlink direction, false if not
     */
    public static boolean isCapacitySufficientDown(CNetworkProperties region, long neededBandwidthInBps) {
        return isCapacitySufficient(TransmissionMode.DownlinkUnicast, region, neededBandwidthInBps);
    }

    /**
     * Consumes the capacity in a region in the uplink direction.
     * The method should be called when a new transmission is started.
     *
     * @param region                  The region where the transmission takes place.
     * @param bandwidthToConsumeInBps The bandwidth in bit/s that is needed for the transmission and that is consumed.
     *                                If consume is larger than the available capacity, the available is set to 0.
     */
    public static void consumeCapacityUp(CNetworkProperties region, long bandwidthToConsumeInBps) {
        if (region == null) {
            log.warn("Could not consume the capacity in the region in the UL because the region is null");
            return;
        }
        if (region.uplink.capacity == Long.MAX_VALUE) { // Regions with unlimited capacity don't do bandwidth consuming/freeing process
            return;
        }

        if (bandwidthToConsumeInBps > 0) {
            region.uplink.capacity = (region.uplink.capacity - bandwidthToConsumeInBps);
        } else {
            log.warn("Could not consume the capacity in the region {} in the UL because the consume value of {} is negative or zero.", region, bandwidthToConsumeInBps);
        }
    }

    /**
     * Consumes the capacity in a region in the downlink direction.
     * The method should be called when a new transmission is started.
     *
     * @param region                  The region where the transmission takes place.
     * @param bandwidthToConsumeInBps The bandwidth in bit/s that is needed for the transmission and that is consumed.
     */
    public static void consumeCapacityDown(CNetworkProperties region, long bandwidthToConsumeInBps) {
        if (region == null) {
            log.warn("Could not consume the capacity in the region in the DL because the region is null");
            return;
        }
        if (region.downlink.capacity == Long.MAX_VALUE) { // Regions with unlimited capacity don't do bandwidth consuming/freeing process
            return;
        }
        if (bandwidthToConsumeInBps > 0) {
            region.downlink.capacity = (region.downlink.capacity - bandwidthToConsumeInBps);
        } else {
            log.warn("Could not consume the capacity in the region {} in the DL because the consume value of {} is negative or zero.", region, bandwidthToConsumeInBps);
        }
    }

    /**
     * Frees bandwidth for the given region in case the message was successfully delivered.
     *
     * @param region               The region that bandwidth is freed from
     * @param bandwidthToFreeInBps How many bit/s should be freed
     */
    public static void freeCapacityUp(@Nonnull CNetworkProperties region, long bandwidthToFreeInBps) {
        if (bandwidthToFreeInBps < 0) {
            log.warn("Could not free the region capacity in the uplink direction in region {} because the capacity to free is {}, which is smaller than 0.", region.id, bandwidthToFreeInBps);
            return;
        }
        if (region.uplink.capacity == Long.MAX_VALUE) { // Regions with unlimited capacity don't do bandwidth consuming/freeing process
            return;
        }
        if (region.uplink.capacity + bandwidthToFreeInBps <= region.uplink.maxCapacity) {
            region.uplink.capacity = region.uplink.capacity + bandwidthToFreeInBps;
            log.trace("Freeing {} bit/s in the uplink direction in region {} (capacity now: {} bit/s)",
                    bandwidthToFreeInBps, region.id, region.uplink.capacity);
        } else {
            region.uplink.capacity = region.uplink.maxCapacity;
            log.trace("Tried to free {} bit/s in region {} in uplink direction but this would exceed the maximum capacity."
                            + " The capacity is therefore set to {}, which is the maximum capacity.",
                    bandwidthToFreeInBps, region.id, region.uplink.capacity
            );
        }
    }

    /**
     * Frees bandwidth for the given region. This method should normally be called
     * when the message that consumed the bandwidth was successfully delivered.
     *
     * @param region               The region that bandwidth is freed from
     * @param bandwidthToFreeInBps How many bit/s should be freed
     */
    public static void freeCapacityDown(CNetworkProperties region, long bandwidthToFreeInBps) {
        if (region == null) {
            log.warn("Could not free the region capacity in the downlink direction, because the region is null");
            return;
        }
        if (bandwidthToFreeInBps < 0) {
            log.warn("Could not free the region capacity in the downlink direction in region {} because the capacity to free is {}, which is smaller than 0.", region.id, bandwidthToFreeInBps);
            return;
        }
        if (region.downlink.capacity == Long.MAX_VALUE) { // Regions with unlimited capacity don't do bandwidth consuming/freeing process
            return;
        }
        if (region.downlink.capacity + bandwidthToFreeInBps <= region.downlink.maxCapacity) {
            region.downlink.capacity = region.downlink.capacity + bandwidthToFreeInBps;
            log.trace("Freeing {} bit/s in the downlink direction in region {} (capacity now: {} bit/s)",
                    bandwidthToFreeInBps, region.id, region.downlink.capacity);
        } else {
            region.downlink.capacity = region.downlink.maxCapacity;
            log.trace("Tried to free {} bit/s in region {} in downlink direction but this would exceed the maximum capacity. The capacity is therefore set to {}, which is the maximum capacity.", bandwidthToFreeInBps, region.id, region.downlink.capacity);
        }
    }

    /**
     * This methods checks whether the region has enough capacity left to allow a new transmission in that region.
     * If the available capacity of the region is below a threshold value, a new transmission is not possible and the region
     * is not available for a new transmission.
     *
     * @param mode The requested transmission of the transmission
     * @return The method returns true, if the available capacity of the region is above the threshold for allowing a new
     *         transmission. Otherwise, the method returns false.
     */
    public static boolean isAvailable(TransmissionMode mode, CNetworkProperties region) {
        if (region == null) {
            log.warn("Could not compute isBusy because the region is null");
            return false;
        }
        if (mode == null) {
            log.warn("Could not compute isBusy for region {} because the mode is null", region.id);
            return false;
        }
        if (mode.isUplink()) {
            return region.uplink.capacity >= region.uplink.maxCapacity * MINIMAL_BANDWIDTH_FACTOR;
        } else if (mode.equals(TransmissionMode.DownlinkUnicast)) {
            return region.downlink.capacity >= region.downlink.maxCapacity * MINIMAL_BANDWIDTH_FACTOR;
        } else if (mode.equals(TransmissionMode.DownlinkMulticast)) {
            return region.downlink.capacity >= region.downlink.maxCapacity
                    * region.downlink.multicast.usableCapacity
                    * MINIMAL_BANDWIDTH_FACTOR;
        }
        return false;
    }
}
