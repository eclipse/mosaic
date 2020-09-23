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

package org.eclipse.mosaic.rti.api.parameters;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.concurrent.Immutable;

/**
 * This class provides required information for an interaction subscription.
 * Currently, only the id of the Interaction is required.
 */
@Immutable
public class InteractionDescriptor {

    public final String interactionId;

    public InteractionDescriptor(String interactionId) {
        this.interactionId = interactionId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("interactionId", interactionId)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final InteractionDescriptor other = (InteractionDescriptor) o;
        return new EqualsBuilder()
                .append(interactionId, other.interactionId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(interactionId)
                .toHashCode();
    }

}
