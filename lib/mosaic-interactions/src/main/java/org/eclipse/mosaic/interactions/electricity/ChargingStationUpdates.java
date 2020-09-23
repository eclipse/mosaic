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

package org.eclipse.mosaic.interactions.electricity;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.electricity.ChargingStationData;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is intended to be used to forward the updated state of a
 * {@link ChargingStationData} to the RTI.
 */
public final class ChargingStationUpdates extends Interaction {

    private static final long serialVersionUID = 1L;
    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(ChargingStationUpdates.class);

    /**
     * Updated {@link ChargingStationData}.
     */
    private final ChargingStationData chargingStation;

    /**
     * Creates a new {@link ChargingStationUpdates} interaction.
     *
     * @param time            Timestamp of this interaction, unit: [ns]
     * @param chargingStation Updated {@link ChargingStationData}
     */
    public ChargingStationUpdates(long time, ChargingStationData chargingStation) {
        super(time);
        this.chargingStation = chargingStation;
    }

    /**
     * Returns the updated {@link ChargingStationData}.
     *
     * @return updated charging station
     */
    public ChargingStationData getChargingStation() {
        return chargingStation;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 41)
                .append(chargingStation)
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

        ChargingStationUpdates other = (ChargingStationUpdates) obj;
        return new EqualsBuilder()
                .append(this.chargingStation, other.chargingStation)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("chargingStation", chargingStation)
                .toString();
    }

}
