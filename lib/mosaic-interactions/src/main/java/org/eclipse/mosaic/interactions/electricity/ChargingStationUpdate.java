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

package org.eclipse.mosaic.interactions.electricity;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.electricity.ChargingStationData;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is intended to be used to forward updates of the
 * {@link ChargingStationData} to the RTI.
 */
public final class ChargingStationUpdate extends Interaction {

    private static final long serialVersionUID = 1L;
    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(ChargingStationUpdate.class);

    /**
     * The updated {@link ChargingStationData}.

     */
    private final ChargingStationData updatedChargingStation;

    /**
     * Creates a new {@link ChargingStationUpdate} interaction.
     *
     * @param time            Timestamp of this interaction, unit: [ns]
     * @param updatedChargingStation The updated {@link ChargingStationData}
     */
    public ChargingStationUpdate(long time, ChargingStationData updatedChargingStation) {
        super(time);
        this.updatedChargingStation = updatedChargingStation;
    }

    /**
     * Returns the updated {@link ChargingStationData}.
     *
     * @return updated charging station
     */
    public ChargingStationData getUpdatedChargingStation() {
        return updatedChargingStation;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 41)
                .append(updatedChargingStation)
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

        ChargingStationUpdate other = (ChargingStationUpdate) obj;
        return new EqualsBuilder()
                .append(this.updatedChargingStation, other.updatedChargingStation)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("updatedChargingStation", updatedChargingStation)
                .toString();
    }

}
