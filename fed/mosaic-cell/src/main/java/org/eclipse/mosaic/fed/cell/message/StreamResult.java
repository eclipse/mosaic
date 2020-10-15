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

package org.eclipse.mosaic.fed.cell.message;

import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Generic message for stream modules (can be either Up or Down).
 */
public class StreamResult {

    /**
     * Name of the region.
     */
    private final String regionId;

    /**
     * Occupied bandwidth for a stream.
     */
    private final long consumedBandwidth;

    /**
     * Vehicle-to-X message.
     */
    private final V2xMessage v2xMessage;

    /**
     * Transmission mode for stream.
     */
    private final TransmissionMode mode;

    /**
     * Node which involved in the transmission.
     */
    private final String involvedNode;

    /**
     * Creates a new {@link StreamResult} object.
     *
     * @param regionId          Name of the region.
     * @param consumedBandwidth Bandwidth used for the stream.
     * @param mode              Transmission mode for the stream.
     * @param involvedNode      Node involved in the transmission.
     * @param v2XMessage        Transmitted message.
     */
    public StreamResult(String regionId, long consumedBandwidth, TransmissionMode mode, String involvedNode, V2xMessage v2XMessage) {
        this.regionId = regionId;
        this.consumedBandwidth = consumedBandwidth;
        this.mode = mode;
        this.involvedNode = involvedNode;
        this.v2xMessage = v2XMessage;
    }

    /**
     * Returns the name of the region where the stream is occurred.
     *
     * @return Name of the region.
     */
    public String getRegionId() {
        return regionId;
    }

    public long getConsumedBandwidth() {
        return consumedBandwidth;
    }

    public V2xMessage getV2xMessage() {
        return v2xMessage;
    }

    public TransmissionMode getMode() {
        return mode;
    }

    public String getInvolvedNode() {
        return involvedNode;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 89)
                .append(regionId)
                .append(consumedBandwidth)
                .append(v2xMessage)
                .append(mode)
                .append(involvedNode)
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
        if (getClass() != obj.getClass()) {
            return false;
        }

        StreamResult other = (StreamResult) obj;
        return new EqualsBuilder()
                .append(this.regionId, other.regionId)
                .append(this.consumedBandwidth, other.consumedBandwidth)
                .append(this.v2xMessage, other.v2xMessage)
                .append(this.mode, other.mode)
                .append(this.involvedNode, other.involvedNode)
                .isEquals();
    }
}
