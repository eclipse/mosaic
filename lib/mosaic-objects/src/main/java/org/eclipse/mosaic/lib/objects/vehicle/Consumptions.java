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

package org.eclipse.mosaic.lib.objects.vehicle;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    /**
     * Electrical power consumption in Wh.
     */
    private final double electricalPower;

    public Consumptions(double fuel, double electricalPower) {
        this.fuel = fuel;
        this.electricalPower = electricalPower;
    }

    /**
     * Returns the fuel consumption in ml.
     */
    public double getFuel() {
        return this.fuel;
    }

    /**
     * Returns the electrical power in Watt.
     */
    public double getElectricalPower() {
        return electricalPower;
    }

    /**
     * Return a new Consumptions object containing the total consumption.
     *
     * @param consumptions the previous total consumptions
     * @return a new Consumptions object containing the total consumption
     */
    public Consumptions addConsumptions(Consumptions consumptions) {
        return new Consumptions(
                this.getFuel() + consumptions.getFuel(),
                this.getElectricalPower() + consumptions.getElectricalPower()
        );
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 17)
                .append(this.fuel)
                .append(this.electricalPower)
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
                .append(this.electricalPower, other.electricalPower)
                .isEquals();
    }

    @Override
    public String toString() {
        return "Consumptions{" + "fuel=" + this.fuel + ", electricalPower=" + this.electricalPower + '}';
    }

}
