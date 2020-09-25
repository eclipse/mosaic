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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;

/**
 * Test suite for {@link AbstractSpawningMode.IncreaseExponential} and {@link AbstractSpawningMode.DecreaseExponential}.
 */
public class ExponentialSpawningModeTest {

    @Test
    public void exponentialGrowth() {
        SpawningMode spawningMode = new AbstractSpawningMode.IncreaseExponential(null, 100 * TIME.SECOND, 2000, 200 * TIME.SECOND);

        double[] expectedSpawningTimes = new double[]{
                0.000,
                18.000,
                36.000,
                47.892,
                55.750,
                61.725,
                66.711,
                71.056,
                74.931,
                78.436,
                81.642,
                84.599,
                87.346,
                89.912,
                92.321,
                94.592,
                96.740,
                98.779,
                100.719
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
    public void exponentialShrink() {
        SpawningMode spawningMode = new AbstractSpawningMode.DecreaseExponential(null, 100 * TIME.SECOND, 2000, 200 * TIME.SECOND);

        double[] expectedSpawningTimes = new double[]{
                0.000,
                1.800,
                3.600,
                5.476,
                7.432,
                9.474,
                11.610,
                13.848,
                16.200,
                18.676,
                21.290,
                24.057,
                26.996,
                30.128,
                33.479,
                37.081,
                40.972,
                45.200,
                49.824,
                54.920,
                60.589,
                66.964,
                74.228,
                82.640,
                92.584,
                104.653
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