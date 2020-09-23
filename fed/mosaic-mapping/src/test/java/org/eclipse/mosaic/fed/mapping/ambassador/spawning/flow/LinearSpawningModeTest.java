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
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;

/**
 * Test suite for {@link AbstractSpawningMode.IncreaseLinear} and {@link AbstractSpawningMode.DecreaseLinear}.
 */
public class LinearSpawningModeTest {

    @Test
    public void linearGrowth() {
        SpawningMode spawningMode = new AbstractSpawningMode.IncreaseLinear(null, 100 * TIME.SECOND, 2000, 200 * TIME.SECOND);

        double[] expectedSpawningTimes = new double[]{
                0.000,
                18.000,
                36.000,
                42.870,
                47.116,
                50.820,
                54.255,
                57.485,
                60.544,
                63.460,
                66.251,
                68.933,
                71.518,
                74.017,
                76.437,
                78.787,
                81.071,
                83.296,
                85.466,
                87.584,
                89.655,
                91.681,
                93.666,
                95.612,
                97.521,
                99.395,
                101.236
        };

        int spawnItem = 0;
        long nextSpawningTime = 100 * TIME.SECOND;
        assertTrue(spawningMode.isSpawningActive(nextSpawningTime));
        while (spawningMode.isSpawningActive(nextSpawningTime)) {
            nextSpawningTime = spawningMode.getNextSpawningTime(nextSpawningTime);
            assertEquals(100 + expectedSpawningTimes[spawnItem++], (double) nextSpawningTime / TIME.SECOND, 0.001d);
        }
    }

    @Test
    public void linearShrink() {
        SpawningMode spawningMode = new AbstractSpawningMode.DecreaseLinear(null, 100 * TIME.SECOND, 2000, 200 * TIME.SECOND);

        double[] expectedSpawningTimes = new double[]{
                0.000,
                1.800,
                3.600,
                5.430,
                7.290,
                9.182,
                11.109,
                13.071,
                15.071,
                17.111,
                19.193,
                21.321,
                23.497,
                25.724,
                28.007,
                30.349,
                32.756,
                35.232,
                37.785,
                40.421,
                43.148,
                45.977,
                48.920,
                51.991,
                55.207,
                58.590,
                62.167,
                65.975,
                70.061,
                74.492,
                79.365,
                84.826,
                91.126,
                98.735,
                108.743
        };

        int spawnItem = 0;
        long nextSpawningTime = 100 * TIME.SECOND;
        assertTrue(spawningMode.isSpawningActive(nextSpawningTime));
        while (spawningMode.isSpawningActive(nextSpawningTime)) {
            nextSpawningTime = spawningMode.getNextSpawningTime(nextSpawningTime);
            assertEquals(100 + expectedSpawningTimes[spawnItem++], (double) nextSpawningTime / TIME.SECOND, 0.001d);
        }
    }

}