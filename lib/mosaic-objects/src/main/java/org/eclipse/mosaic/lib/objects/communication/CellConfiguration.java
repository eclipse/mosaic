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

package org.eclipse.mosaic.lib.objects.communication;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * This class represents the configuration of a vehicles' cellular communication interface.
 */
public class CellConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The MIN_BITRATE_PORTION is the share of the maximal bitrate that still needs
     * be available to allow a new transmission to this node.
     */
    private static final double MIN_BITRATE_PORTION = 0.1;

    private final String nodeId;

    private Long maxDownlinkBitrate;
    private Long maxUplinkBitrate;

    /**
     * The minimal bitrate are used to store how much bandwidth has to be
     * available at the node to allow a new transmission to this node.
     */
    private Long minUplinkBitrate;
    private Long minDownlinkBitrate;

    private Long availableDownlinkBitrate;
    private Long availableUplinkBitrate;

    private boolean enabled;

    /**
     * Creates a CellConfiguration for the node with the given {@param nodeId} and sets whether it is {@param enabled}.
     * The maximum download and upload rates are set to Long.MAX_VALUE for this node.
     *
     * @param nodeId  The id of the node
     * @param enabled Tells whether the node is enabled
     */
    public CellConfiguration(String nodeId, boolean enabled) {
        this(nodeId, enabled, Long.MAX_VALUE, Long.MAX_VALUE);
    }

    /**
     * Creates a CellConfiguration for the node with the given {@code nodeId} and sets whether it is {@code enabled}.
     * The maximum download rate for the node is as set with {@code maxDownlinkBitrate}.
     * The maximum upload rate for the node is as set with {@code maxUplinkBitrate}.
     *
     * @param nodeId       The id of the node
     * @param enabled      Tells whether the node is enabled
     * @param maxDownlinkBitrate The maximum download rate for this node
     * @param maxUplinkBitrate The maximum upload rate for this node
     */
    public CellConfiguration(String nodeId, boolean enabled, Long maxDownlinkBitrate, Long maxUplinkBitrate) {
        this.nodeId = nodeId;
        this.enabled = enabled;
        setBitrates(maxDownlinkBitrate, maxUplinkBitrate);
    }

    public void setBitrates(Long maxDownlinkBitrate, Long maxUplinkBitrate) {
        this.maxDownlinkBitrate = maxDownlinkBitrate;
        this.maxUplinkBitrate = maxUplinkBitrate;
        if (maxDownlinkBitrate != null) {
            if (maxDownlinkBitrate < 0) {
                throw new IllegalArgumentException("Maximum downlink bitrate is negative");
            }
            this.availableDownlinkBitrate = this.maxDownlinkBitrate;
            this.minDownlinkBitrate = (long) (this.maxDownlinkBitrate * MIN_BITRATE_PORTION);
            if (this.minDownlinkBitrate == 0) {
                throw new IllegalArgumentException("Maximum downlink bitrate too small");
            }
        }
        if (maxUplinkBitrate != null) {
            if (maxUplinkBitrate < 0) {
                throw new IllegalArgumentException("Maximum uplink bitrate is negative");
            }
            this.availableUplinkBitrate = this.maxUplinkBitrate;
            this.minUplinkBitrate = (long) (this.maxUplinkBitrate * MIN_BITRATE_PORTION);
            if (this.minUplinkBitrate == 0) {
                throw new IllegalArgumentException("Maximum uplink bitrate too small.");
            }
        }
    }

    public final String getNodeId() {
        return nodeId;
    }

    public final long getAvailableDownlinkBitrate() {
        return availableDownlinkBitrate;
    }

    public final long getAvailableUplinkBitrate() {
        return availableUplinkBitrate;
    }

    public Long getMaxDownlinkBitrate() {
        return maxDownlinkBitrate;
    }

    public Long getMaxUplinkBitrate() {
        return maxUplinkBitrate;
    }

    public void consumeUplink(long consume) {
        availableUplinkBitrate -= consume;
    }

    public void consumeDownlink(long consume) {
        availableDownlinkBitrate -= consume;
    }

    public void freeUplink(long free) {
        availableUplinkBitrate += free;
    }

    public void freeDownlink(long free) {
        availableDownlinkBitrate += free;
    }

    public boolean uplinkPossible() {
        return availableUplinkBitrate >= minUplinkBitrate;
    }

    public boolean downlinkPossible() {
        return availableDownlinkBitrate >= minDownlinkBitrate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CellConfiguration other = (CellConfiguration) o;
        return new EqualsBuilder()
                .append(maxDownlinkBitrate, other.maxDownlinkBitrate)
                .append(maxUplinkBitrate, other.maxUplinkBitrate)
                .append(minUplinkBitrate, other.minUplinkBitrate)
                .append(minDownlinkBitrate, other.minDownlinkBitrate)
                .append(availableDownlinkBitrate, other.availableDownlinkBitrate)
                .append(availableUplinkBitrate, other.availableUplinkBitrate)
                .append(enabled, other.enabled)
                .append(nodeId, other.nodeId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(nodeId)
                .append(maxDownlinkBitrate)
                .append(maxUplinkBitrate)
                .append(minUplinkBitrate)
                .append(minDownlinkBitrate)
                .append(availableDownlinkBitrate)
                .append(availableUplinkBitrate)
                .append(enabled)
                .toHashCode();
    }
}
