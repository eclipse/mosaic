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

package org.eclipse.mosaic.fed.application.ambassador.simulation;

import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.enums.LaneChangeMode;
import org.eclipse.mosaic.lib.enums.SpeedMode;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleParameter;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleParameter.VehicleParameterType;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.ColorUtils;

import java.awt.Color;
import java.util.Collection;
import java.util.Vector;
import javax.annotation.Nonnull;

/**
 * {@link VehicleParameters} in combination with {@link VehicleParameters.VehicleParametersChangeRequest} are
 * used to change specific attributes of simulated {@link VehicleUnit}s, this is highly influenced by SUMO's API
 * and most parameters are derived from SUMO.
 */
public final class VehicleParameters {

    private final VehicleType initialVehicleType;

    private double maxSpeed;
    private double maxAcceleration;
    private double maxDeceleration;
    private double emergencyDeceleration;
    private double minimumGap;
    private double imperfection;
    private double reactionTime;
    private double speedFactor;
    private Color vehicleColor;
    private LaneChangeMode laneChangeMode;
    private SpeedMode speedMode;

    /**
     * Constructor for {@link VehicleParameters}, sets the values derived by
     * the given {@link VehicleType}.
     *
     * @param initialValues the {@link VehicleType}, that defines the initial {@link VehicleParameters}
     */
    VehicleParameters(@Nonnull final VehicleType initialValues) {
        initialVehicleType = initialValues;
        maxSpeed = initialValues.getMaxSpeed();
        maxAcceleration = initialValues.getAccel();
        maxDeceleration = initialValues.getDecel();
        emergencyDeceleration = initialValues.getEmergencyDecel();
        minimumGap = initialValues.getMinGap();
        imperfection = initialValues.getSigma();
        reactionTime = initialValues.getTau();
        speedFactor = initialValues.getSpeedFactor();
        vehicleColor = ColorUtils.toColor(initialValues.getColor(), null);
        laneChangeMode = initialValues.getLaneChangeMode();
        speedMode = initialValues.getSpeedMode();
    }

    /**
     * This private constructor is called, when an {@link VehicleParametersChangeRequest} is
     * initiated. In the beginning all values are referred from the prior parameters.
     *
     * @param before the {@link VehicleParameters} before the change
     */
    private VehicleParameters(@Nonnull final VehicleParameters before) {
        initialVehicleType = before.initialVehicleType;
        maxSpeed = before.getMaxSpeed();
        maxAcceleration = before.getMaxAcceleration();
        maxDeceleration = before.getMaxDeceleration();
        emergencyDeceleration = before.getEmergencyDeceleration();
        minimumGap = before.getMinimumGap();
        imperfection = before.getImperfection();
        reactionTime = before.getReactionTime();
        speedFactor = before.getSpeedFactor();
        vehicleColor = before.getVehicleColor();
        laneChangeMode = before.getLaneChangeMode();
        speedMode = before.getSpeedMode();
    }

    public final double getMaxSpeed() {
        return maxSpeed;
    }

    public final double getMaxAcceleration() {
        return maxAcceleration;
    }

    public final double getMaxDeceleration() {
        return maxDeceleration;
    }

    public double getEmergencyDeceleration() {
        return emergencyDeceleration;
    }

    public final double getMinimumGap() {
        return minimumGap;
    }

    public final double getSpeedFactor() {
        return speedFactor;
    }

    public final double getReactionTime() {
        return reactionTime;
    }

    public final double getImperfection() {
        return imperfection;
    }

    public final VehicleType getInitialVehicleType() {
        return initialVehicleType;
    }

    public Color getVehicleColor() {
        return vehicleColor;
    }

    public final LaneChangeMode getLaneChangeMode() {
        return laneChangeMode;
    }

    public SpeedMode getSpeedMode() {
        return speedMode;
    }

    public static class VehicleParametersChangeRequest {

        private static final double CHANGE_DELTA = 0.01d;

        private final Collection<VehicleParameter> changedParameters;
        private final VehicleParameters newParameters;
        private final VehicleOperatingSystem vehicleOperatingSystem;
        private boolean invalidated = false;

        VehicleParametersChangeRequest(VehicleOperatingSystem vehicleOperatingSystem, VehicleParameters before) {
            newParameters = new VehicleParameters(before);
            this.vehicleOperatingSystem = vehicleOperatingSystem;
            changedParameters = new Vector<>();
        }

        /**
         * Changes the maximum speed limit.
         *
         * @param newValue new speed limit
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        public final VehicleParametersChangeRequest changeMaxSpeed(double newValue) {
            checkInvalidated();
            if (Math.abs(newValue - newParameters.getMaxSpeed()) > CHANGE_DELTA) {
                newParameters.maxSpeed = Math.max(0, newValue);
                changedParameters.add(new VehicleParameter(VehicleParameterType.MAX_SPEED, newParameters.maxSpeed));
            }
            return this;
        }

        /**
         * Changes the maximum acceleration.
         *
         * @param newValue maximum acceleration
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        public final VehicleParametersChangeRequest changeMaxAcceleration(double newValue) {
            checkInvalidated();
            if (Math.abs(newValue - newParameters.getMaxAcceleration()) > CHANGE_DELTA) {
                newParameters.maxAcceleration = Math.max(0, newValue);
                changedParameters.add(new VehicleParameter(VehicleParameterType.MAX_ACCELERATION, newParameters.maxAcceleration));
            }
            return this;
        }

        /**
         * Changes the maximum deceleration.
         *
         * @param newValue maximum deceleration
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        @SuppressWarnings("UnusedReturnValue")
        public final VehicleParametersChangeRequest changeMaxDeceleration(double newValue) {
            checkInvalidated();
            if (Math.abs(newValue - newParameters.getMaxDeceleration()) > CHANGE_DELTA) {
                newParameters.maxDeceleration = Math.max(0, newValue);
                changedParameters.add(new VehicleParameter(VehicleParameterType.MAX_DECELERATION, newParameters.maxDeceleration));
            }
            return this;
        }

        /**
         * Changes the emergency deceleration.
         *
         * @param newValue emergency deceleration
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        @SuppressWarnings("UnusedReturnValue")
        public final VehicleParametersChangeRequest changeEmergencyDeceleration(double newValue) {
            checkInvalidated();
            if (Math.abs(newValue - newParameters.getEmergencyDeceleration()) > CHANGE_DELTA) {
                newParameters.emergencyDeceleration = Math.max(0, newValue);
                changedParameters.add(
                        new VehicleParameter(VehicleParameterType.EMERGENCY_DECELERATION, newParameters.emergencyDeceleration)
                );
            }
            return this;
        }

        /**
         * Changes the minimum gap to the leading vehicle.
         *
         * @param newValue minimum gap to the leading vehicle
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        @SuppressWarnings("UnusedReturnValue")
        public final VehicleParametersChangeRequest changeMinimumGap(double newValue) {
            checkInvalidated();
            if (Math.abs(newValue - newParameters.getMinimumGap()) > CHANGE_DELTA) {
                newParameters.minimumGap = Math.max(0, newValue);
                changedParameters.add(new VehicleParameter(VehicleParameterType.MIN_GAP, newParameters.minimumGap));
            }
            return this;
        }

        /**
         * Changes the speed factor.
         *
         * @param newValue speed factor
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        @SuppressWarnings("UnusedReturnValue")
        public final VehicleParametersChangeRequest changeSpeedFactor(double newValue) {
            checkInvalidated();
            if (Math.abs(newValue - newParameters.getSpeedFactor()) > CHANGE_DELTA) {
                newParameters.speedFactor = Math.max(0, newValue);
                changedParameters.add(new VehicleParameter(VehicleParameterType.SPEED_FACTOR, newParameters.speedFactor));
            }
            return this;
        }

        /**
         * Changes the vehicles' imperfection (sigma).
         *
         * @param newValue imperfection (sigma)
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        @SuppressWarnings("UnusedReturnValue")
        public final VehicleParametersChangeRequest changeImperfection(double newValue) {
            checkInvalidated();
            if (Math.abs(newValue - newParameters.getImperfection()) > CHANGE_DELTA) {
                newParameters.imperfection = Math.min(Math.max(0, newValue), 1);
                changedParameters.add(new VehicleParameter(VehicleParameterType.IMPERFECTION, newParameters.imperfection));
            }
            return this;
        }

        /**
         * Changes the vehicles' reaction time (tau).
         *
         * @param newValue reaction time (tau)
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        public final VehicleParametersChangeRequest changeReactionTime(double newValue) {
            checkInvalidated();
            if (Math.abs(newValue - newParameters.getReactionTime()) > CHANGE_DELTA) {
                newParameters.reactionTime = Math.max(0, newValue);
                changedParameters.add(new VehicleParameter(VehicleParameterType.REACTION_TIME, newParameters.reactionTime));
            }
            return this;
        }

        /**
         * Changes the vehicle color.
         *
         * @param newValue vehicle color
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        public final VehicleParametersChangeRequest changeColor(Color newValue) {
            checkInvalidated();
            if (newValue != newParameters.vehicleColor && !newValue.equals(newParameters.vehicleColor)) {
                newParameters.vehicleColor = newValue;
                changedParameters.add(new VehicleParameter(VehicleParameterType.COLOR, newParameters.vehicleColor));
            }
            return this;
        }

        /**
         * Changes the lane change mode.
         *
         * @param newValue lane change mode
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        public final VehicleParametersChangeRequest changeLaneChangeMode(LaneChangeMode newValue) {
            checkInvalidated();
            if (newValue != newParameters.laneChangeMode) {
                newParameters.laneChangeMode = newValue;
                changedParameters.add(new VehicleParameter(VehicleParameterType.LANE_CHANGE_MODE, newParameters.laneChangeMode));
            }
            return this;
        }

        /**
         * Changes the speed mode.
         *
         * @param newValue speed mode
         * @return this {@link VehicleParametersChangeRequest} for easy cascading
         */
        public final VehicleParametersChangeRequest changeSpeedMode(SpeedMode newValue) {
            checkInvalidated();
            if (newValue != newParameters.speedMode) {
                newParameters.speedMode = newValue;
                changedParameters.add(new VehicleParameter(VehicleParameterType.SPEED_MODE, newParameters.speedMode));
            }
            return this;
        }

        /**
         * Applies the changed parameters in the vehicles' operating system.
         */
        public void apply() {
            this.vehicleOperatingSystem.applyVehicleParametersChange(this);
        }

        final VehicleParameters getUpdatedBehavior() {
            invalidated = true;
            return newParameters;
        }

        private void checkInvalidated() {
            if (invalidated) {
                throw new IllegalStateException("Could not change behavior anymore");
            }
        }

        Collection<VehicleParameter> getChangedParameters() {
            invalidated = true;
            return changedParameters;
        }
    }
}
