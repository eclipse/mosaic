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

package org.eclipse.mosaic.interactions.application;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nullable;

/**
 * This extension of {@link Interaction} is intended to be used in custom applications, it can be
 * extended for simulation units to react upon different influences and can be used for intercommunication
 * between applications.
 */
public abstract class ApplicationInteraction extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(ApplicationInteraction.class);

    /**
     * Id of the specific unit which should receive this interaction.
     */
    private final String unitId;

    /**
     * Constructor for a {@link ApplicationInteraction}.
     *
     * @param time   Timestamp of this interaction, unit: [ns]
     * @param unitId identifier of the unit which should retrieve this interaction. If  {@code null},
     *               all simulation simulated units will be notified.
     */
    public ApplicationInteraction(long time, @Nullable String unitId) {
        super(time);
        this.unitId = unitId;
    }

    /**
     * Returns the id of the specific unit, that receives this interaction.
     *
     * @return the id of the specific unit.
     */
    @Nullable
    public String getUnitId() {
        return unitId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 97)
                .append(unitId)
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

        ApplicationInteraction other = (ApplicationInteraction) obj;
        return new EqualsBuilder()
                .append(this.unitId, other.unitId)
                .isEquals();
    }

    @Override
    public final String getTypeId() {
        /* Since this interaction is abstract, we cannot use an implementation-specific class name
         * otherwise it would not be passed to the application. Therefore we use
         * a fixed type ID based on this base class name */
        return TYPE_ID;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("unitId", unitId)
                .toString();
    }
}
