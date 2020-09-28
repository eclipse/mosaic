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

package org.eclipse.mosaic.interactions.traffic;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * This is the first {@link Interaction} sent in every simulation, it contains predefined
 * vehicle types.
 */
@Immutable
public final class VehicleTypesInitialization extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(VehicleTypesInitialization.class);

    /**
     * Map containing vehicle types mapped to their IDs.
     */
    private final Map<String, VehicleType> types;

    /**
     * Creates a new VehicleTypesInitialization.
     *
     * @param time  Timestamp of this interaction, unit: [ns]
     * @param types VehicleTypes
     */
    public VehicleTypesInitialization(long time, Map<String, VehicleType> types) {
        super(time);
        this.types = types;
    }

    public Map<String, VehicleType> getTypes() {
        return types;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 79)
                .append(types)
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

        VehicleTypesInitialization other = (VehicleTypesInitialization) obj;
        return new EqualsBuilder()
                .append(this.types, other.types)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("types", types)
                .toString();
    }
}

