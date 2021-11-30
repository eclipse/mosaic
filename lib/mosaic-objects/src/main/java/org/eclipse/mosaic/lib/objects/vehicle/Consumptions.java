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

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * Data object providing vehicle consumptions, e.g. fuel or electrical power.
 */
@Immutable
public class Consumptions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Fuel consumption in ml.
     */
    private final double fuel;

    public Consumptions(double fuel) {
        this.fuel = fuel;
    }

    /**
     * Returns the fuel consumption in ml.
     */
    public double getFuel() {
        return this.fuel;
    }

    /**
     * Return a new Consumptions object containing the total consumption.
     *
     * @param consumptions the previous total consumptions
     * @return a new Consumptions object containing the total consumption
     */
    public Consumptions addConsumptions(Consumptions consumptions) {
        return new Consumptions(this.getFuel() + consumptions.getFuel());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 17)
                .append(this.fuel)
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

        Consumptions other = (Consumptions) obj;
        return new EqualsBuilder()
                .append(this.fuel, other.fuel)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("fuel", fuel)
                .toString();
    }

}
