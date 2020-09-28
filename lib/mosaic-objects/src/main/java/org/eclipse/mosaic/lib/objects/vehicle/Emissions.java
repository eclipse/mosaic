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
 * Data object providing emission values (e.g. NOx, PMx, CO2, etc.).
 */
@Immutable
public final class Emissions implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double co2;
    private final double co;
    private final double hc;
    private final double pmx;
    private final double nox;

    public Emissions(final double co2, final double co, final double hc, final double pmx, final double nox) {
        this.co2 = co2;
        this.co = co;
        this.hc = hc;
        this.pmx = pmx;
        this.nox = nox;
    }

    /**
     * Returns the carbon dioxide (CO2) emission during the simulation. Unit: [mg].
     */
    public double getCo2() {
        return co2;
    }

    /**
     * Returns the carbon monoxide (CO) emission during the simulation. Unit: [mg].
     */
    public double getCo() {
        return co;
    }

    /**
     * Returns the hydrocarbon (HC) emissions during the simulation. Unit: [mg].
     */
    public double getHc() {
        return hc;
    }

    /**
     * Returns the fine-particle emissions during the simulation. Unit: [mg].
     */
    public double getPmx() {
        return pmx;
    }

    /**
     * Returns The Nitrogen oxides (NO and NO2) emissions during the simulation. Unit: [mg].
     */
    public double getNox() {
        return nox;
    }

    public Emissions addEmissions(Emissions emissions) {
        return new Emissions(
                this.getCo2() + emissions.getCo2(),
                this.getCo() + emissions.getCo(),
                this.getHc() + emissions.getHc(),
                this.getPmx() + emissions.getPmx(),
                this.getNox() + emissions.getNox()
        );
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 37)
                .append(co2)
                .append(co)
                .append(hc)
                .append(pmx)
                .append(nox)
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

        Emissions other = (Emissions) obj;
        return new EqualsBuilder()
                .append(this.co2, other.co2)
                .append(this.co, other.co)
                .append(this.hc, other.hc)
                .append(this.nox, other.nox)
                .append(this.pmx, other.nox)
                .isEquals();
    }

    @Override
    public String toString() {
        return "Emissions{" + "co2=" + co2 + ", co=" + co + ", hc=" + hc + ", pmx=" + pmx + ", nox=" + nox + '}';
    }

}

