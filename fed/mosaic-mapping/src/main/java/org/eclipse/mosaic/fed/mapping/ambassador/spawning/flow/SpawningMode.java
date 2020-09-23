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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning.flow;

/**
 * Interface to be implemented by all {@link SpawningMode}'s. Includes
 * all methods to supply necessary spawning functionality.
 */
public interface SpawningMode {

    /**
     * Determines whether the spawning for the given simulation time is still active.
     * Depending on the implementation this can be determined by different metrics.
     *
     * @param currentTime the current simulation time (ns)
     * @return {@code true} if this spawning mode has still vehicles to spawn
     */
    boolean isSpawningActive(long currentTime);

    /**
     * Returns the next spawning time. Depending on the implementation
     * this will also set the next spawning time.
     *
     * @param currentTime the current simulation time (ns)
     * @return the next time (ns) a vehicle should be spawned
     */
    long getNextSpawningTime(long currentTime);
}
