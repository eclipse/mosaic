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

package org.eclipse.mosaic.fed.application.ambassador.simulation.tmc;

import org.eclipse.mosaic.lib.objects.traffic.InductionLoopInfo;

/**
 * A InductionLoop is used to retrieve the information of
 * an {@link InductionLoopInfo}.
 */
public class InductionLoop {

    private final String id;

    private InductionLoopInfo lastInductionLoopInfo;

    /**
     * Constructor for {@link InductionLoop}.
     *
     * @param id The id of the {@link InductionLoop}
     */
    public InductionLoop(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Updates this lane detector with new information.
     *
     * @param info The new information.
     */
    public void update(InductionLoopInfo info) {
        this.lastInductionLoopInfo = info;
    }

    /**
     * Returns the traffic flow at this detector if
     * {@link #lastInductionLoopInfo} is set.
     *
     * @return The traffic flow at this detector in veh/h.
     */
    public double getTrafficFlowVehPerHour() {
        return lastInductionLoopInfo == null ? 0 : Math.max(0, lastInductionLoopInfo.getTrafficFlow());
    }

    /**
     * Returns the average speed at this detector if
     * {@link #lastInductionLoopInfo} is set.
     *
     * @return The average speed at this detector in m/s.
     */
    public double getAverageSpeedMs() {
        return lastInductionLoopInfo == null ? 0 : Math.max(0, lastInductionLoopInfo.getMeanSpeed());
    }

    /**
     * Returns the amount of vehicles that passed the {@link InductionLoop}.
     * {@code null} safe.
     *
     * @return the amount of vehicles that passed the detector
     */
    @SuppressWarnings("unused")
    public int getAmountOfPassedVehicles() {
        return lastInductionLoopInfo == null ? 0 : Math.max(0, lastInductionLoopInfo.getVehicleCount());
    }
}
