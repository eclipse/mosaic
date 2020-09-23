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

package org.eclipse.mosaic.interactions.communication;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * This extension of {@link Interaction} is intended to be used to
 * exchange information about a sent V2X message.
 */
@Immutable
public final class V2xMessageTransmission extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(V2xMessageTransmission.class);

    /**
     * Interaction that has been sent.
     */
    private final V2xMessage message;

    /**
     * Constructor using fields.
     *
     * @param time    Timestamp of this interaction, unit: [ns]
     * @param message The V2X message.
     */
    public V2xMessageTransmission(long time, @Nonnull V2xMessage message) {
        super(time);
        this.message = message;
    }

    @Nonnull
    public V2xMessage getMessage() {
        return this.message;
    }

    /**
     * Short method to get the source node name for convenience.
     *
     * @return The source node name.
     */
    @Nonnull
    public String getSourceName() {
        return getMessage().getRouting().getSource().getSourceName();
    }

    /**
     * Short method to get the source node position for convenience.
     *
     * @return The source node position.
     */
    @Nullable
    public GeoPoint getSourcePosition() {
        return getMessage().getRouting().getSource().getSourcePosition();
    }

    /**
     * Short method to get the unique message id for convenience.
     *
     * @return The message id.
     */
    public int getMessageId() {
        return getMessage().getId();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 17)
                .append(message)
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

        V2xMessageTransmission other = (V2xMessageTransmission) obj;
        return new EqualsBuilder()
                .append(this.message, other.message)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("message", message)
                .toString();
    }
}
