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

package org.eclipse.mosaic.lib.objects.v2x;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Contains the receive signal strength of a V2X communication.
 */
public class V2xReceiverInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long receiveTime;

    private long sendTime;

    /**
     * The rss in [dBm]. {@code 0} if no rss is available.
     */
    private float receiveSignalStrength;

    /**
     * The needed bandwidth in [bps]. {@code 0} if no bandwidth information is available.
     */
    private long neededBandwidth;

    /**
     * Create a new {@link V2xReceiverInformation}.
     *
     * @param receiveTime The time the message was received
     */
    public V2xReceiverInformation(final long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public V2xReceiverInformation sendTime(long messageStartTime) {
        this.sendTime = messageStartTime;
        return this;
    }

    public V2xReceiverInformation signalStrength(float receiveSignalStrength) {
        this.receiveSignalStrength = receiveSignalStrength;
        return this;
    }

    public V2xReceiverInformation neededBandwidth(long neededBandwidthInBps) {
        this.neededBandwidth = neededBandwidthInBps;
        return this;
    }

    /**
     * The time at which this message was sent out.
     * @return the time in [ns]
     */
    public long getSendTime() {
        return sendTime;
    }

    /**
     * The time at which this message was received.
     * @return the time in [ns]
     */
    public long getReceiveTime() {
        return receiveTime;
    }

    /**
     * Returns the receive signal strength (rss) if supported.
     *
     * @return the receive signal strength (rss) if supported. {@code 0} if not supported. Unit: [dBm].
     */
    public float getReceiveSignalStrength() {
        return receiveSignalStrength;
    }

    /**
     * Returns the needed bandwidth if supported.
     *
     * @return the needed bandwidth, if supported. {@code 0} if not supported. Unit: [bps].
     */
    public long getNeededBandwidth() {
        return neededBandwidth;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 37)
                .append(receiveTime)
                .append(receiveSignalStrength)
                .append(neededBandwidth)
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

        V2xReceiverInformation other = (V2xReceiverInformation) obj;
        return new EqualsBuilder()
                .append(this.receiveTime, other.receiveTime)
                .append(this.neededBandwidth, other.neededBandwidth)
                .append(this.receiveSignalStrength, other.receiveSignalStrength)
                .isEquals();
    }

    @Override
    public String toString() {
        return "V2XReceiverInformation{" + "receiveSignalStrength=" + receiveSignalStrength + '}';
    }
}
