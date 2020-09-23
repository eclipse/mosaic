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

package org.eclipse.mosaic.lib.objects.traffic;

import org.eclipse.mosaic.lib.objects.UnitData;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class InductionLoopInfo extends UnitData {

    private static final long serialVersionUID = 1L;

    private final int vehicleCount;

    private final double meanSpeed;

    private final double meanVehicleLength;

    private final double flow;

    /**
     * Returns the number of vehicles passed this induction loop during the last time step.
     *
     * @return the number of vehicles passed this induction loop during the last time step
     */
    public int getVehicleCount() {
        return vehicleCount;
    }

    /**
     * Returns the mean speed of vehicles passed this induction loop during the last time step [m/s].
     *
     * @return the mean speed of vehicles passed this induction loop during the last time step [m/s]
     */
    public double getMeanSpeed() {
        return meanSpeed;
    }

    /**
     * Returns the mean length of vehicles passed this induction loop during the last time step [m].
     *
     * @return the mean length of vehicles passed this induction loop during the last time step [m]
     */
    public double getMeanVehicleLength() {
        return meanVehicleLength;
    }

    /**
     * Returns the current traffic flow passing this induction loop in [veh/h].
     *
     * @return the current traffic flow passing this induction loop in [veh/h].
     */
    public double getTrafficFlow() {
        return flow;
    }

    private InductionLoopInfo(
            long time,
            String name,
            int vehicleCount,
            double meanSpeed,
            double meanVehicleLength,
            double flow
    ) {
        super(time, name, null);
        this.vehicleCount = vehicleCount;
        this.meanSpeed = meanSpeed;
        this.meanVehicleLength = meanVehicleLength;
        this.flow = flow;
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

        InductionLoopInfo other = (InductionLoopInfo) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(this.vehicleCount, other.vehicleCount)
                .append(this.meanSpeed, other.meanSpeed)
                .append(this.meanVehicleLength, other.meanVehicleLength)
                .append(this.flow, other.flow)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 29)
                .append(super.hashCode())
                .append(meanSpeed)
                .append(vehicleCount)
                .append(meanVehicleLength)
                .append(flow)
                .toHashCode();
    }

    public String toString() {
        return "InductionLoopInfo{"
                + "name=" + super.getName()
                + ", vehicleCount=" + vehicleCount
                + ", meanSpeed=" + meanSpeed
                + ", meanVehicleLength=" + meanVehicleLength
                + ", trafficFlow=" + flow + '}';
    }

    /**
     * A builder for creating {@link InductionLoopInfo} objects without using the monstrous constructor.
     */
    public static class Builder {
        private long time;
        private String name;
        private int vehicleCount;
        private double meanSpeed;
        private double meanVehicleLength;
        private double flow;

        /**
         * Init the builder with the current simulation time [ns] and name of the inductionloop.
         */
        public Builder(long time, String name) {
            this.time = time;
            this.name = name;
        }

        public Builder traffic(int vehicleCount, double flow) {
            this.vehicleCount = vehicleCount;
            this.flow = flow;
            return this;
        }

        public Builder vehicleData(double meanSpeed, double meanVehicleLength) {
            this.meanSpeed = meanSpeed;
            this.meanVehicleLength = meanVehicleLength;
            return this;
        }

        /**
         * Returns the final {@link InductionLoopInfo} based on the properties given before.
         */
        public InductionLoopInfo create() {
            return new InductionLoopInfo(
                    time, name,
                    vehicleCount,
                    meanSpeed,
                    meanVehicleLength,
                    flow
            );
        }
    }
}
