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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning.lane;

import org.eclipse.mosaic.fed.mapping.ambassador.spawning.VehicleTypeSpawner;
import org.eclipse.mosaic.lib.enums.VehicleClass;

import java.util.Comparator;
import java.util.List;

/**
 * Specific lane index selector which spawns trucks (VehicleClass#HeavyGoodsVehicle) on the
 * right most lane. This is quite suitable for highways and avoids spawning trucks
 * on the left or middle lanes.
 */
public class HighwaySpecificLaneIndexSelector implements LaneIndexSelector {

    /**
     * A list of lanes on the highway.
     */
    private final List<Integer> lanes;

    /**
     * The lane selector used to select spawning lane for vehicles that are not of
     * vehicle class {@link VehicleClass#HeavyGoodsVehicle}, {@link VehicleClass#ExceptionalSizeVehicle}
     * or {@link VehicleClass#PublicTransportVehicle}.
     */
    private final LaneIndexSelector defaultLaneSelector;

    /**
     * The lane used for vehicles of type {@link VehicleClass#HeavyGoodsVehicle},
     * {@link VehicleClass#ExceptionalSizeVehicle} and {@link VehicleClass#PublicTransportVehicle}.
     * This is usually the smallest lane index.
     */
    private final int truckLane;

    /**
     * The previously chosen lane.
     */
    private int previousLane = -1;

    /**
     * Constructor for {@link HighwaySpecificLaneIndexSelector}.
     * Sets {@link #defaultLaneSelector} and {@link #truckLane}.
     *
     * @param lanes list of lane indices
     */
    public HighwaySpecificLaneIndexSelector(List<Integer> lanes) {
        this.lanes = lanes;
        // initialize final fields dependent on lanes
        this.defaultLaneSelector = new RoundRobinLaneIndexSelector(lanes);
        this.truckLane = lanes.stream()
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("Given lanes are empty"));
    }

    /**
     * Selects the lane index for the vehicle to be spawned. All truck
     * classes will be spawned on the {@link #truckLane}. All other
     * lanes are decided by the {@link #defaultLaneSelector}.
     *
     * @param vehicleType the time of the vehicle that is spawned next
     * @return index where the vehicle is supposed to be spawned
     */
    @Override
    public int nextLane(VehicleTypeSpawner vehicleType) {
        if (lanes.size() == 1) {
            return truckLane;
        }
        int nextLane;
        // spawn all trucks on the truck lane, decide the others using the default selector
        switch (vehicleType.getVehicleClass()) {
            case HeavyGoodsVehicle:
            case PublicTransportVehicle:
            case ExceptionalSizeVehicle:
                nextLane = truckLane;
                break;
            default:
                nextLane = defaultLaneSelector.nextLane(vehicleType);
                if (nextLane == previousLane) {
                    nextLane = defaultLaneSelector.nextLane(vehicleType);
                }
        }

        previousLane = nextLane;
        return nextLane;
    }
}
