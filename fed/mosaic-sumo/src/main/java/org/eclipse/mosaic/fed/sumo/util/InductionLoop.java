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

package org.eclipse.mosaic.fed.sumo.util;

import static java.lang.Long.max;
import static java.lang.Math.min;

import org.eclipse.mosaic.lib.util.RingBuffer;
import org.eclipse.mosaic.rti.TIME;

/**
 * This class is used to calculate the traffic flow aggregated
 * from the last counts of passed vehicles at this detector.
 */
public class InductionLoop {

    /**
     * Induction lopp identifier.
     */
    private final String id;

    /**
     * Defines the time window size in which flows shall be calculated.
     */
    private final long measurementWindow;

    /**
     * Stores as many {@link VehicleCount}s as needed for
     * calculations for the defined {@link #measurementWindow}.
     */
    private final RingBuffer<VehicleCount> vehicleCounts;

    /**
     * Cached traffic flow.
     */
    private Double trafficFlowCached;

    /**
     * Latest time stamp when traffic flow was cached.
     */
    private long trafficFlowCacheTime = 0;

    /**
     * Creates a new induction loop instance.
     *
     * @param id                    induction loop identifier
     * @param measurementWindowSize window in which the traffic flow shall be calculated
     */
    public InductionLoop(String id, long measurementWindowSize) {
        this.id = id;
        this.measurementWindow = max(TIME.SECOND, measurementWindowSize);
        // assuming that simulation step is always greater than 100ms
        vehicleCounts = new RingBuffer<>((int) ((measurementWindow / TIME.SECOND) * 10));
    }

    public String getId() {
        return id;
    }

    /**
     * Updates this lane detector with new information.
     *
     * @param time           Update at this time.
     * @param passedVehicles The number of passed vehicles.
     */
    public void update(long time, int passedVehicles) {
        this.vehicleCounts.add(new VehicleCount(time, passedVehicles));
        this.trafficFlowCached = null;
    }

    /**
     * Getter for the traffic flow per hour.
     *
     * @param currentTime The current time.
     * @return The traffic flow at this detector in veh/h.
     */
    public double getTrafficFlowVehPerHour(long currentTime) {
        if (trafficFlowCached == null || trafficFlowCacheTime + 60 * TIME.SECOND > currentTime) {
            trafficFlowCached = calculateTrafficFlow(currentTime);
            trafficFlowCacheTime = currentTime;
        }
        return trafficFlowCached;
    }

    /**
     * Calculates the flow of vehicles driving through the detector during the last x simulation steps. This measure is not
     * provided by the traffic simulator, but is instead gathered from the vehicle counts during the last simulation steps.
     */
    private double calculateTrafficFlow(long currentTime) {
        int total = 0;
        for (VehicleCount count : vehicleCounts) {
            if (count.time <= currentTime && count.time > currentTime - measurementWindow) {
                total += count.counts;
            }
        }
        long duration = min(currentTime, measurementWindow);
        return duration > 0 ? total / (duration / (double) TIME.SECOND) * 3600d : 0;
    }

    private static class VehicleCount {
        private final long time;
        private final int counts;

        private VehicleCount(long time, int counts) {
            this.time = time;
            this.counts = counts;
        }
    }
}
