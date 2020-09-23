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

import org.eclipse.mosaic.lib.enums.LaneChangeMode;
import org.eclipse.mosaic.lib.enums.SpeedMode;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.awt.Color;
import java.io.Serializable;
import javax.annotation.Nonnull;

public class VehicleParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum VehicleParameterType {
        MIN_GAP(Double.class),
        MAX_SPEED(Double.class),
        MAX_ACCELERATION(Double.class),
        MAX_DECELERATION(Double.class),
        EMERGENCY_DECELERATION(Double.class),
        SPEED_FACTOR(Double.class),
        IMPERFECTION(Double.class),
        REACTION_TIME(Double.class),
        LANE_CHANGE_MODE(LaneChangeMode.class),
        SPEED_MODE(SpeedMode.class),
        COLOR(Color.class);

        private Class<? extends Serializable> parameterType;

        VehicleParameterType(Class<? extends Serializable> parameterType) {
            this.parameterType = parameterType;
        }

        public Class<?> getParameterType() {
            return parameterType;
        }
    }

    private final VehicleParameterType parameterType;

    private final Object value;

    public VehicleParameter(@Nonnull final VehicleParameterType parameterType, @Nonnull final Object value) {
        this.parameterType = parameterType;
        if (parameterType.getParameterType().isAssignableFrom(value.getClass())) {
            this.value = value;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Parameter value (type=%s) is not suitable to parameter type (name=%s, type=%s)",
                    value.getClass(), parameterType.name(), parameterType.getClass()
            ));
        }
    }

    @Nonnull
    public VehicleParameterType getParameterType() {
        return parameterType;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 53)
                .append(parameterType)
                .append(value)
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

        VehicleParameter other = (VehicleParameter) obj;
        return new EqualsBuilder()
                .append(this.parameterType, other.parameterType)
                .append(this.value, other.value)
                .isEquals();
    }
}
