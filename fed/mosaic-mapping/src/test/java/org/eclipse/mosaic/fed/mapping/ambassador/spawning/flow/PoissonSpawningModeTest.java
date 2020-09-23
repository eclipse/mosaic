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

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;

/**
 * Test suite for {@link PoissonSpawningMode}
 */
public class PoissonSpawningModeTest {

    private RandomNumberGenerator randomNumberGenerator = new DefaultRandomNumberGenerator(531212388L);

    @Test
    public void possionDistribution() {
        // SETUP
        SpawningMode spawningMode = new PoissonSpawningMode(randomNumberGenerator,
                0 * TIME.SECOND,
                2000,
                (0 + 1800) * TIME.SECOND
        );

        // RUN
        int spawnItem = 0;
        long nextSpawningTime = 0 * TIME.SECOND;
        while (spawningMode.isSpawningActive(nextSpawningTime)) {
            nextSpawningTime = spawningMode.getNextSpawningTime(nextSpawningTime);
            spawnItem++;
        }

        // ASSERT
        // with a target flow of 2000 veh/h we expect 1000 vehicles to be spawned in 1800 seconds
        assertEquals(1000, spawnItem, 50);
    }


}