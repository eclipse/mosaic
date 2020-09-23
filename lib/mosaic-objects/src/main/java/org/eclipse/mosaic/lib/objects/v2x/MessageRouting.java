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

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
public class MessageRouting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The source address of the message.
     */
    private final SourceAddressContainer source;

    /**
     * The destination address of the message.
     */
    private final DestinationAddressContainer destination;

    public MessageRouting(DestinationAddressContainer destination, SourceAddressContainer source) {
        this.source = Objects.requireNonNull(source);
        this.destination = Objects.requireNonNull(destination);
    }

    public SourceAddressContainer getSource() {
        return source;
    }

    public DestinationAddressContainer getDestination() {
        return destination;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 97)
                .append(source)
                .append(destination)
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

        MessageRouting other = (MessageRouting) obj;
        return new EqualsBuilder()
                .append(this.source, other.source)
                .append(this.destination, other.destination)
                .isEquals();
    }

    @Override
    public String toString() {
        return "MessageRouting{" + "sourceAddressContainer=" + source + ", destinationAddressContainer=" + destination + '}';
    }
}
