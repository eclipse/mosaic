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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning.lane;

import org.eclipse.mosaic.fed.mapping.ambassador.spawning.VehicleTypeSpawner;

/**
 * Interface defining all methods necessary to implement a {@link LaneIndexSelector}.
 * Implementations of this interface are supposed to supply an algorithm to decide, which
 * lane a vehicle should be spawned on.
 */
public interface LaneIndexSelector {

    /**
     * Supplies a lane index depending on the given {@link VehicleTypeSpawner}.
     *
     * @param vehicleType the time of the vehicle that is spawned next
     * @return the next spawning lane according to the given vehicle type
     */
    int nextLane(VehicleTypeSpawner vehicleType);
}
