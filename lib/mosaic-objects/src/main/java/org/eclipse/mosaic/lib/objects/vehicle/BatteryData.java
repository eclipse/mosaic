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

import org.eclipse.mosaic.lib.objects.electricity.ChargingSpot;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * This class provides battery information for a vehicle provided by
 * the electricity or battery simulator.
 */
public class BatteryData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The id of the vehicle this battery belongs to.
     */
    private final String ownerId;

    /**
     * Time in [ns] until this information is valid.
     */
    private final long time;

    /**
     * Quotient of the current capacity and the initial capacity of the battery.
     * Range: [0=empty, 1=full]
     */
    private final double stateOfCharge;

    /**
     * Current capacity of the battery.
     * unit: [Ws]
     */
    private final double capacity;

    /**
     * Flag, indicating if the vehicle is currently being charged at a {@link ChargingSpot}.
     */
    private final boolean charging;

    /**
     * Creates a new {@link BatteryData} for an added vehicle.
     *
     * @param time    timestamp of the data
     * @param ownerId of the vehicle that the data belongs to
     */
    public BatteryData(long time, String ownerId) {
        this.ownerId = ownerId;
        this.time = time;
        this.stateOfCharge = -1.0;
        this.capacity = -1.0;
        this.charging = false;
    }

    /**
     * Creates a new {@link BatteryData} for an added vehicle.
     *
     * @param time          timestamp of the data
     * @param ownerId       id of the vehicle that the data belongs to
     * @param stateOfCharge state of charge of the battery [0,1]
     * @param capacity      current capacity of the battery
     * @param charging      flag indicating whether battery is charging
     */
    private BatteryData(long time, String ownerId, double stateOfCharge, double capacity, boolean charging) {
        this.ownerId = ownerId;
        this.time = time;
        this.stateOfCharge = stateOfCharge;
        this.capacity = capacity;
        this.charging = charging;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public long getTime() {
        return time;
    }

    /**
     * Returns the quotient of the current capacity and the initial capacity of the battery.
     * Range: [0=empty, 1=full]
     */
    public double getStateOfCharge() {
        return stateOfCharge;
    }

    /**
     * Returns {@code true}, if the battery of the vehicle is fully depleted.
     */
    public boolean isBatteryEmpty() {
        return getStateOfCharge() <= 0.0;
    }

    /**
     * Returns {@code true}, if the battery of the vehicle is fully charged.
     */
    public boolean isBatteryFull() {
        return getStateOfCharge() >= 1.0;
    }

    /**
     * Returns Current capacity of the battery.
     * unit: [Ws]
     */
    public double getCapacity() {
        return capacity;
    }

    /**
     * Returns {@code true} if the vehicle is currently being charged at a {@link ChargingSpot}.
     */
    public boolean isCharging() {
        return charging;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 53)
                .append(stateOfCharge)
                .append(capacity)
                .append(charging)
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

        BatteryData other = (BatteryData) obj;
        return new EqualsBuilder()
                .append(this.stateOfCharge, other.stateOfCharge)
                .append(this.capacity, other.capacity)
                .append(this.charging, other.charging)
                .isEquals();
    }

    @Override
    public String toString() {
        return "VehicleElectricInformation{"
                + "VehicleID=" + ownerId
                + ", rechargingType=" + stateOfCharge
                + ", capacity=" + capacity
                + ", charging=" + charging + '}';
    }

    public static class Builder {
        private final long time;
        private final String ownerId;
        private double stateOfCharge;
        private double capacity;
        private boolean charging;

        public Builder(long time, String ownerId) {
            this.time = time;
            this.ownerId = ownerId;
        }

        public Builder stateOfChargeInfo(double stateOfCharge, double capacity) {
            this.stateOfCharge = stateOfCharge;
            this.capacity = capacity;
            return this;
        }

        public Builder charging(boolean charging) {
            this.charging = charging;
            return this;
        }

        /**
         * Copies relevant data from a given {@link BatteryData} object.
         *
         * @param batteryData data to copy from
         * @return the builder
         */
        public Builder copyFrom(BatteryData batteryData) {
            stateOfCharge = batteryData.stateOfCharge;
            capacity = batteryData.capacity;
            charging = batteryData.charging;
            return this;
        }

        public BatteryData build() {
            return new BatteryData(time, ownerId, stateOfCharge, capacity, charging);

        }
    }
}
