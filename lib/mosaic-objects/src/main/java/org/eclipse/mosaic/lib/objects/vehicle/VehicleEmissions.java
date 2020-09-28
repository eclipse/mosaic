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
 * Provide emissions of a vehicle.
 */
@Immutable
public final class VehicleEmissions implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Emissions currentEmissions;
    private final Emissions allEmissions;

    public VehicleEmissions(final Emissions currentEmissions, final Emissions allEmissions) {
        this.currentEmissions = currentEmissions;
        this.allEmissions = allEmissions;
    }

    /**
     * Returns the emissions produced by the vehicle in the last simulation step.
     *
     * @return The current emissions of the last simulation step step.
     */
    public Emissions getCurrentEmissions() {
        return currentEmissions;
    }

    /**
     * Returns the accumulated emissions including the current emissions.
     *
     * @return The sum of all previous emissions including the current emissions.
     */
    public Emissions getAllEmissions() {
        return allEmissions;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 19)
                .append(currentEmissions)
                .append(allEmissions)
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

        VehicleEmissions other = (VehicleEmissions) obj;
        return new EqualsBuilder()
                .append(this.currentEmissions, other.currentEmissions)
                .append(this.allEmissions, other.allEmissions)
                .isEquals();
    }

    @Override
    public String toString() {
        return "VehicleEmissions{" + "currentEmissions=" + currentEmissions + ", allEmissions=" + allEmissions + '}';
    }

}

