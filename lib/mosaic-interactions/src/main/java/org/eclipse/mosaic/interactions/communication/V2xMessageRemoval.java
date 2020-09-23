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

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This extension of {@link Interaction} is intended to be used to
 * exchange information about to delete V2X messages.
 */
public final class V2xMessageRemoval extends Interaction {

    private static final long serialVersionUID = 1L;
    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(V2xMessageRemoval.class);

    /**
     * Collection with ids to identify all V2XMessages to delete.
     */
    private final Collection<Integer> ids;

    /**
     * Constructor using fields.
     *
     * @param time Timestamp of this interaction, unit: [ns]
     * @param ids  Collection with ids to identify all V2XMessages to delete.
     */
    public V2xMessageRemoval(long time, @Nonnull Collection<Integer> ids) {
        super(time);
        this.ids = Collections.unmodifiableCollection(Objects.requireNonNull(ids));
    }

    @Nonnull
    public Collection<Integer> getRemovedMessageIds() {
        return ids;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 79)
                .append(ids)
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

        V2xMessageRemoval other = (V2xMessageRemoval) obj;
        return new EqualsBuilder()
                .append(this.ids, other.ids)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("ids", ids)
                .toString();
    }
}
