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

package org.eclipse.mosaic.lib.objects.addressing;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A container for a source network address.
 */
@Immutable
public class SourceAddressContainer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The network address of the sender.
     */
    private final NetworkAddress sourceAddress;

    /**
     * The host name of the sender.
     */
    private final String sourceName;

    /**
     * The geographic position of the sender (might be null).
     */
    private final GeoPoint sourcePosition;

    /**
     * Creates a new {@link SourceAddressContainer}.
     *
     * @param sourceAddress  Source address in IpV4.
     * @param sourceName     Source name as string
     * @param sourcePosition Current source position as GeoPoint.
     */
    public SourceAddressContainer(@Nonnull NetworkAddress sourceAddress, @Nonnull String sourceName, @Nullable GeoPoint sourcePosition) {
        this.sourceAddress = Objects.requireNonNull(sourceAddress);
        requireNonBroadcast();
        this.sourceName = Objects.requireNonNull(sourceName);
        this.sourcePosition = sourcePosition;
    }

    @Nonnull
    public NetworkAddress getSourceAddress() {
        return sourceAddress;
    }

    @Nonnull
    public String getSourceName() {
        return sourceName;
    }

    @Nullable
    public GeoPoint getSourcePosition() {
        return sourcePosition;
    }

    /**
     * Throws an IllegalArgumentException if the IPv4 Address given in form of Inet4Address object is equal to
     * the broadcast address (255.255.255.255)
     */
    private void requireNonBroadcast() {
        if (sourceAddress.isBroadcast()) {
            throw new IllegalArgumentException("Source address is not valid! Source address should differ from broadcast address.");
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 97)
                .append(sourceAddress)
                .append(sourceName)
                .append(sourcePosition)
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

        SourceAddressContainer other = (SourceAddressContainer) obj;
        return new EqualsBuilder()
                .append(this.sourceAddress, other.sourceAddress)
                .append(this.sourceName, other.sourceName)
                .append(this.sourcePosition, other.sourcePosition)
                .isEquals();
    }

    @Override
    public String toString() {
        return "SourceAddressContainer{" + "sourceAddress=" + sourceAddress + ", sourceName=" + sourceName + ", sourcePosition=" + sourcePosition + '}';
    }

}