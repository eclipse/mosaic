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

package org.eclipse.mosaic.lib.objects.vehicle;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * Provides consumptions of a vehicle.
 */
@Immutable
public final class VehicleConsumptions implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Consumptions currentConsumptions;
    private final Consumptions allConsumptions;

    /**
     * Construct a {@link VehicleConsumptions} object.
     *
     * @param currentConsumptions current consumption.
     * @param allConsumptions     total consumption.
     */
    public VehicleConsumptions(final Consumptions currentConsumptions, final Consumptions allConsumptions) {
        this.currentConsumptions = currentConsumptions;
        this.allConsumptions = allConsumptions;
    }

    /**
     * Returns the current consumptions during this time step.
     */
    public Consumptions getCurrentConsumptions() {
        return currentConsumptions;
    }

    /**
     * Returns the accumulated consumptions including the current emissions.
     *
     * @return The sum of all previous consumptions including the current emissions.
     */
    public Consumptions getAllConsumptions() {
        return allConsumptions;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 13)
                .append(currentConsumptions)
                .append(allConsumptions)
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

        VehicleConsumptions other = (VehicleConsumptions) obj;
        return new EqualsBuilder()
                .append(this.currentConsumptions, other.currentConsumptions)
                .append(this.allConsumptions, other.allConsumptions)
                .isEquals();
    }

    @Override
    public String toString() {
        return "VehicleConsumptions{" + "currentConsumptions=" + currentConsumptions + ", allConsumptions=" + allConsumptions + '}';
    }

}

