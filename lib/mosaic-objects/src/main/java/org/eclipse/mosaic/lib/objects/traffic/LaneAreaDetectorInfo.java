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

package org.eclipse.mosaic.lib.objects.traffic;

import org.eclipse.mosaic.lib.objects.UnitData;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LaneAreaDetectorInfo extends UnitData {

    private static final long serialVersionUID = 1L;

    private final double length;

    private final int vehicleCount;

    private final double meanSpeed;

    private final int haltingVehicles;

    private final double trafficDensity;

    /**
     * Returns the length of the detector in [m].
     *
     * @return the length of the detector in [m]
     */
    public double getLength() {
        return length;
    }

    /**
     * Returns the number of vehicles in the area of this detector.
     *
     * @return the number of vehicles in the area of this detector
     */
    public int getVehicleCount() {
        return vehicleCount;
    }

    /**
     * Returns the mean speed of the vehicles in the area of this detector [m/s].
     *
     * @return the mean speed of the vehicles in the area of this detector [m/s]
     */
    public double getMeanSpeed() {
        return meanSpeed;
    }

    /**
     * Returns the number of standing vehicles in the area of this detector.
     *
     * @return the number of standing vehicles in the area of this detector
     */
    public int getHaltingVehicles() {
        return haltingVehicles;
    }

    /**
     * Returns  the traffic density in the area of this detector [veh/km].
     *
     * @return the traffic density in the area of this detector [veh/km]
     */
    public double getTrafficDensity() {
        return trafficDensity;
    }

    private LaneAreaDetectorInfo(
            long time,
            String name,
            double length,
            int vehicleCount,
            double meanSpeed,
            int haltingVehicles,
            double trafficDensity
    ) {
        super(time, name, null);
        this.length = length;
        this.vehicleCount = vehicleCount;
        this.meanSpeed = meanSpeed;
        this.haltingVehicles = haltingVehicles;
        this.trafficDensity = trafficDensity;
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

        LaneAreaDetectorInfo other = (LaneAreaDetectorInfo) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(this.length, other.length)
                .append(this.vehicleCount, other.vehicleCount)
                .append(this.meanSpeed, other.meanSpeed)
                .append(this.haltingVehicles, other.haltingVehicles)
                .append(this.trafficDensity, other.trafficDensity)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 29)
                .append(super.hashCode())
                .append(length)
                .append(vehicleCount)
                .append(meanSpeed)
                .append(haltingVehicles)
                .append(trafficDensity)
                .toHashCode();
    }

    public String toString() {
        return "LaneAreaDetectorInfo{"
                + "name=" + super.getName()
                + ", length=" + length
                + ", vehicleCount=" + vehicleCount
                + ", meanSpeed=" + meanSpeed
                + ", haltingVehicles=" + haltingVehicles
                + ", trafficDensity=" + trafficDensity + '}';
    }

    /**
     * A builder for creating {@link LaneAreaDetectorInfo} objects without using the monstrous constructor.
     */
    public static class Builder {

        private long time;
        private String name;
        private double length;
        private int vehicleCount;
        private double meanSpeed;
        private int haltingVehicles;
        private double density;

        /**
         * Init the builder with the current simulation time [ns] and name of the lane area.
         */
        public Builder(long time, String name) {
            this.time = time;
            this.name = name;
        }

        public LaneAreaDetectorInfo.Builder length(double length) {
            this.length = length;
            return this;
        }

        public LaneAreaDetectorInfo.Builder density(double density) {
            this.density = density;
            return this;
        }

        public LaneAreaDetectorInfo.Builder haltingVehicles(int haltingVehicles) {
            this.haltingVehicles = haltingVehicles;
            return this;
        }

        public LaneAreaDetectorInfo.Builder vehicleData(int vehicleCount, double meanSpeed) {
            this.vehicleCount = vehicleCount;
            this.meanSpeed = meanSpeed;
            return this;
        }

        /**
         * Returns the final {@link LaneAreaDetectorInfo} based on the properties given before.
         */
        public LaneAreaDetectorInfo create() {
            return new LaneAreaDetectorInfo(
                    time, name,
                    length,
                    vehicleCount,
                    meanSpeed,
                    haltingVehicles,
                    density
            );
        }
    }
}
