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

import java.util.List;

/**
 * Look up https://en.wikipedia.org/wiki/Round-robin_scheduling
 */
public class RoundRobinLaneIndexSelector implements LaneIndexSelector {

    private final List<Integer> lanes;

    private int selections = 0;

    public RoundRobinLaneIndexSelector(List<Integer> lanes) {
        this.lanes = lanes;
    }

    @Override
    public int nextLane(VehicleTypeSpawner vehicleType) {
        return lanes.get((selections++) % lanes.size());
    }
}
