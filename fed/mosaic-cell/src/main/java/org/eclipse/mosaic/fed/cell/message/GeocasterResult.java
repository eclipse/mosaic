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

import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The message that comes out of the Geocaster and is intended for the Downstream module.
 * It contains information about the Unicast/Multicast mode and the accordingly casted
 * receiver(s) in their current region.
 */
public class GeocasterResult {

    /**
     * Indicates the type of {@link TransmissionMode}.
     * This should either be {@link TransmissionMode#DownlinkUnicast} or {@link TransmissionMode#DownlinkMulticast}.
     */
    private final TransmissionMode downstreamMode;

    /**
     * For all receivers in a CNetworkProperties.
     */
    private final Multimap<CNetworkProperties, String> receivers;

    private final V2xMessage v2xMessage;

    private final boolean isFullMessage;

    /**
     * Creates a new {@link GeocasterResult} object.
     *
     * @param receivers      All the receivers in a specific base region.
     * @param downstreamMode Transmission mode as downstream.
     * @param v2xMessage     Vehicle-to-X message.
     * @param isFullMessage  true if the message object contains the V2X message
     */
    public GeocasterResult(Multimap<CNetworkProperties, String> receivers, TransmissionMode downstreamMode,
                           V2xMessage v2xMessage, boolean isFullMessage) {
        this.downstreamMode = downstreamMode;
        this.receivers = receivers;
        this.v2xMessage = v2xMessage;
        this.isFullMessage = isFullMessage;
    }

    /**
     * Returns the downstream mode.
     *
     * @return Transmission mode in downstream.
     */
    public TransmissionMode getDownstreamMode() {
        return downstreamMode;
    }

    /**
     * Returns all the receivers in a base region.
     *
     * @return Receivers and located base region.
     */
    public Multimap<CNetworkProperties, String> getReceivers() {
        return receivers;
    }

    /**
     * Returns the V2XMessage.
     *
     * @return V2XMessage.
     */
    public V2xMessage getV2xMessage() {
        return v2xMessage;
    }

    public boolean isFullMessage() {
        return isFullMessage;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 89)
                .append(downstreamMode)
                .append(receivers)
                .append(v2xMessage)
                .append(isFullMessage)
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

        GeocasterResult other = (GeocasterResult) obj;
        return new EqualsBuilder()
                .append(this.downstreamMode, other.downstreamMode)
                .append(this.receivers, other.receivers)
                .append(this.v2xMessage, other.v2xMessage)
                .append(this.isFullMessage, other.isFullMessage)
                .isEquals();
    }
}
