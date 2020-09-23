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
 */

package org.eclipse.mosaic.lib.objects.v2x;

import org.eclipse.mosaic.lib.objects.addressing.DestinationAddressContainer;
import org.eclipse.mosaic.lib.objects.addressing.SourceAddressContainer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MessageStreamRouting extends MessageRouting {

    /**
     * The duration of the stream in ns.
     */
    private final long streamingDuration;

    /**
     * The bandwidth of the stream in bits per second.
     */
    private final long streamingBandwidth;

    public MessageStreamRouting(
            DestinationAddressContainer destinationAddressContainer,
            SourceAddressContainer sourceAddressContainer,
            long streamingDuration,
            long streamingBandwidth
    ) {
        super(destinationAddressContainer, sourceAddressContainer);
        this.streamingDuration = streamingDuration;
        this.streamingBandwidth = streamingBandwidth;
    }

    public long getStreamingDuration() {
        return streamingDuration;
    }

    public long getStreamingBandwidth() {
        return streamingBandwidth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageStreamRouting other = (MessageStreamRouting) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(streamingDuration, other.streamingDuration)
                .append(streamingBandwidth, other.streamingBandwidth)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(streamingDuration)
                .append(streamingBandwidth)
                .toHashCode();
    }
}
