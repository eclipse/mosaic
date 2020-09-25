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

package org.eclipse.mosaic.fed.mapping.config;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CParameterDeviations {

    public double length = 0.0;
    public double minGap = 0.0;
    public double maxSpeed = 0.0;
    public double speedFactor = 0.0;
    public double accel = 0.0;
    public double decel = 0.0;
    public double tau = 0.0;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CParameterDeviations that = (CParameterDeviations) o;

        return new EqualsBuilder()
                .append(length, that.length)
                .append(minGap, that.minGap)
                .append(maxSpeed, that.maxSpeed)
                .append(speedFactor, that.speedFactor)
                .append(accel, that.accel)
                .append(decel, that.decel)
                .append(tau, that.tau)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(length)
                .append(minGap)
                .append(maxSpeed)
                .append(speedFactor)
                .append(accel)
                .append(decel)
                .append(tau)
                .toHashCode();
    }
}
