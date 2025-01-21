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

package org.eclipse.mosaic.lib.objects.electricity;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.UnitData;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class encapsulates the semi-persistent state of a ChargingStation based on the
 * <em>ETSI TS 101 556-1</em> definition.
 * <p>
 * <em>An infrastructure, which provides one or several {@link ChargingSpot} units to supply electric
 * energy for charging EVs.</em>
 * </p>
 */
public final class ChargingStationData extends UnitData {

    private static final long serialVersionUID = 1L;

    /**
     * Map of all {@link ChargingSpot} units belonging to this {@code ChargingStation}.
     */
    private final Map<String, ChargingSpot> chargingSpots = new HashMap<>();

    /**
     * Creates a new charging station object.
     *
     * @param time          current simulation time
     * @param name          Unique identifier of the charging station
     * @param position      position of the charging station
     * @param chargingSpots List of all {@link ChargingSpot ChargingSpots} belonging to this charging station
     */

    public ChargingStationData(long time, String name, GeoPoint position, List<ChargingSpot> chargingSpots) {
        super(time, name, position);
        chargingSpots.forEach(chargingSpot -> this.chargingSpots.put(chargingSpot.getChargingSpotId(), chargingSpot));
    }




    public List<ChargingSpot> getChargingSpots() {
        return Lists.newArrayList(chargingSpots.values());
    }

    public ChargingSpot getChargingSpot(String chargingSpotId) {
        return chargingSpots.get(chargingSpotId);
    }

    /**
     * Returns {@code True}, if the {@code ChargingStation} has a free {@link ChargingSpot} available.
     * Charging Spots can be reserved by the Charging Station App or be engaged by a car without reservation app.
     */
    public boolean isAvailable() {
        for (ChargingSpot chargingSpot : chargingSpots.values()) {
            if (chargingSpot.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public boolean isChargingSpotAvailable(String chargingSpotId) {
        return chargingSpots.get(chargingSpotId) == null && chargingSpots.get(chargingSpotId).isAvailable();
    }

    /**
     * Returns the next available {@link ChargingSpot}. If none are available return {@code null}.
     *
     * @return next available charging spot
     */
    public String getNextAvailableChargingSpot() {
        for (ChargingSpot chargingSpot : chargingSpots.values()) {
            if (chargingSpot.isAvailable()) {
                return chargingSpot.getChargingSpotId();
            }
        }
        return null;
    }

    /**
     * Blocks the given charging spot, meaning a vehicle has docked at it.
     *
     * @param chargingSpotId the charging spot id to block
     */
    public void blockChargingSpot(String chargingSpotId) {
        chargingSpots.get(chargingSpotId).setAvailable(false);
    }


    /**
     * Unblocks the given charging spot, meaning a vehicle has undocked from it.
     *
     * @param chargingSpotId the charging spot id to unblock
     */
    public void unblockChargingSpot(String chargingSpotId) {
        chargingSpots.get(chargingSpotId).setAvailable(true);
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return "ChargingStation [chargingSpots="
                + Lists.newArrayList(chargingSpots.values()).subList(0, Math.min(chargingSpots.size(), maxLen))
                + ", name=" + getName() + ", position="
                + getPosition() + "]";
    }

    public static class Builder {
        private final long time;
        private final String name;
        private final GeoPoint position;
        private List<ChargingSpot> chargingSpots;


        public Builder(long time, String name, GeoPoint position) {
            this.time = time;
            this.name = name;
            this.position = position;
        }

        public Builder chargingSpots(List<ChargingSpot> chargingSpots) {
            this.chargingSpots = chargingSpots;
            return this;
        }

        /**
         * Copies all information of {@link ChargingSpot}s to the builder.
         * @param chargingStationData the data to copy
         * @return A builder with the copied data
         */
        public Builder copyFrom(ChargingStationData chargingStationData) {
            chargingSpots = new ArrayList<>();
            for (ChargingSpot chargingSpot : chargingStationData.getChargingSpots()) {
                ChargingSpot copiedChargingSpot = new ChargingSpot(
                        chargingSpot.getChargingSpotId(), chargingSpot.getChargingType(),
                        chargingSpot.getMaximumVoltage(), chargingSpot.getMaximumCurrent()
                );
                copiedChargingSpot.setAvailable(chargingSpot.isAvailable());
                chargingSpots.add(copiedChargingSpot);
            }
            return this;
        }

        public ChargingStationData build() {
            return new ChargingStationData(time, name, position, chargingSpots);
        }
    }
}
