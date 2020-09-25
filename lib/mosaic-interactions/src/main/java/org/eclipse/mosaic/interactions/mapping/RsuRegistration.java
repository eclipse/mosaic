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

package org.eclipse.mosaic.interactions.mapping;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.mapping.RsuMapping;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * This extension of {@link Interaction} informs that a new Road Side Unit (RSU) has been added to the simulation.
 */
public final class RsuRegistration extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(RsuRegistration.class);

    /**
     * The new RSU.
     */
    private final RsuMapping rsuMapping;

    /**
     * Constructor.
     *
     * @param time         Timestamp of this interaction, unit: [ns]
     * @param name         The name of the RSU.
     * @param group        Group identifier
     * @param applications Installed applications of the RSU.
     * @param position     Position of the RSU.
     */
    public RsuRegistration(final long time, final String name,
                           final String group, final List<String> applications,
                           final GeoPoint position) {
        super(time);
        this.rsuMapping = new RsuMapping(name, group, applications, position);
    }

    public RsuMapping getMapping() {
        return rsuMapping;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 59)
                .append(rsuMapping)
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

        RsuRegistration other = (RsuRegistration) obj;
        return new EqualsBuilder()
                .append(this.rsuMapping, other.rsuMapping)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("rsuMapping", rsuMapping)
                .toString();
    }
}
