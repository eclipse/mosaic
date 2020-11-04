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

    private final long maxDlBitrate;
    private final long maxUlBitrate;

    /**
     * The minimal bitrate are used to store how much bandwidth has to be
     * available at the node to allow a new transmission to this node.
     */
    private final long minUlBitrate;
    private final long minDlBitrate;

    private long availableDlBitrate;
    private long availableUlBitrate;
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
     * Creates a CellConfiguration for the node with the given {@param nodeId} and sets whether it is {@param enabled}.
     * The maximum download rate for the node is as set with {@param maxDlBitrate}.
     * The maximum upload rate for the node is as set with {@param maxUlBitrate}.
     *
     * @param nodeId       The id of the node
     * @param enabled      Tells whether the node is enabled
     * @param maxDlBitrate The maximum download rate for this node
     * @param maxUlBitrate The maximum upload rate for this node
     */
    public CellConfiguration(String nodeId, boolean enabled, long maxDlBitrate, long maxUlBitrate) {
        this.nodeId = nodeId;
        this.enabled = enabled;

        if (maxDlBitrate < 0) {
            // FIXME: Remove sysout; throw meaningful exception and handle it
            System.out.println("The maximum Dl bitrate is too small");
            this.maxDlBitrate = Long.MAX_VALUE;
        } else {
            this.maxDlBitrate = maxDlBitrate;
        }
        if (maxUlBitrate < 0) {
            // FIXME: Remove sysout; throw meaningful exception and handle it
            System.out.println("The maximum Ul bitrate is too small");
            this.maxUlBitrate = Long.MAX_VALUE;
        } else {
            this.maxUlBitrate = maxUlBitrate;
        }

        this.availableDlBitrate = this.maxDlBitrate;
        this.availableUlBitrate = this.maxUlBitrate;

        this.minDlBitrate = (long) (this.maxDlBitrate * MIN_BITRATE_PORTION);
        this.minUlBitrate = (long) (this.maxUlBitrate * MIN_BITRATE_PORTION);
    }

    public final boolean isCellCommunicationEnabled() {
        return enabled;
    }

    public final String getNodeId() {
        return nodeId;
    }

    public final long getAvailableDlBitrate() {
        return availableDlBitrate;
    }

    public final long getAvailableUlBitrate() {
        return availableUlBitrate;
    }

    public long getMaxDlBitrate() {
        return maxDlBitrate;
    }

    public long getMaxUlBitrate() {
        return maxUlBitrate;
    }

    public void consumeUl(long consume) {
        availableUlBitrate -= consume;
    }

    public void consumeDl(long consume) {
        availableDlBitrate -= consume;
    }

    public void freeUl(long free) {
        availableUlBitrate += free;
    }

    public void freeDl(long free) {
        availableDlBitrate += free;
    }

    public boolean ulPossible() {
        return availableUlBitrate >= minUlBitrate;
    }

    public boolean dlPossible() {
        return availableDlBitrate >= minDlBitrate;
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
                .append(maxDlBitrate, other.maxDlBitrate)
                .append(maxUlBitrate, other.maxUlBitrate)
                .append(minUlBitrate, other.minUlBitrate)
                .append(minDlBitrate, other.minDlBitrate)
                .append(availableDlBitrate, other.availableDlBitrate)
                .append(availableUlBitrate, other.availableUlBitrate)
                .append(enabled, other.enabled)
                .append(nodeId, other.nodeId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(nodeId)
                .append(maxDlBitrate)
                .append(maxUlBitrate)
                .append(minUlBitrate)
                .append(minDlBitrate)
                .append(availableDlBitrate)
                .append(availableUlBitrate)
                .append(enabled)
                .toHashCode();
    }
}
