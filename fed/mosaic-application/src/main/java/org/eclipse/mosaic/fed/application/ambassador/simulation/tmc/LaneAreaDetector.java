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

package org.eclipse.mosaic.fed.application.ambassador.simulation.tmc;

import org.eclipse.mosaic.lib.objects.traffic.LaneAreaDetectorInfo;

/**
 * A LaneAreaDetector is used to retrieve the information of
 * a {@link LaneAreaDetectorInfo}.
 */
public class LaneAreaDetector {

    private final String id;
    private LaneAreaDetectorInfo lastLaneAreaInfo;

    /**
     * The constructor for {@link LaneAreaDetector}.
     *
     * @param id The id of the {@link LaneAreaDetector}.
     */
    public LaneAreaDetector(String id) {
        this.id = id;
    }

    /**
     * Updates this lane segment with new information.
     *
     * @param info The new information.
     */
    public void update(LaneAreaDetectorInfo info) {
        this.lastLaneAreaInfo = info;
    }

    public double getTrafficDensity() {
        return lastLaneAreaInfo == null ? 0 : lastLaneAreaInfo.getTrafficDensity();
    }

    public double getLength() {
        return lastLaneAreaInfo == null ? 0 : lastLaneAreaInfo.getLength();
    }

    @SuppressWarnings("unused")
    public int getAmountOfVehiclesOnSegment() {
        return lastLaneAreaInfo == null ? 0 : Math.max(0, lastLaneAreaInfo.getVehicleCount());
    }

    public double getMeanSpeed() {
        return lastLaneAreaInfo == null || lastLaneAreaInfo.getVehicleCount() == 0 ? -1d : lastLaneAreaInfo.getMeanSpeed();
    }

    public String getId() {
        return id;
    }
}
