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
 * Test suite for {@link GrowAndShrinkSpawningMode}.
 */
public class GrowAndShrinkSpawningModeTest {

    @Test
    public void exponentialGrowth() {
        SpawningMode spawningMode = new GrowAndShrinkSpawningMode(null, 100 * TIME.SECOND, 500, 600 * TIME.SECOND, true);

        double[] expectedSpawningTimes = new double[]{
                100.010,
                172.000,
                243.994,
                285.426,
                309.269,
                326.616,
                341.063,
                353.709,
                365.027,
                375.299,
                384.716,
                393.419,
                400.000,
                407.200,
                414.400,
                421.600,
                428.800,
                436.000,
                443.200,
                450.000,
                457.200,
                464.400,
                472.441,
                481.423,
                491.584,
                503.247,
                516.879,
                533.183,
                553.283,
                579.099,
                614.246
        };

        int spawnItem = 0;
        long nextSpawningTime = 100 * TIME.SECOND;
        while (spawningMode.isSpawningActive(nextSpawningTime)) {
            nextSpawningTime = spawningMode.getNextSpawningTime(nextSpawningTime);
            assertEquals(expectedSpawningTimes[spawnItem++], (double) nextSpawningTime / TIME.SECOND, 0.01d);
        }
    }

    @Test
    public void linearGrowth() {
        SpawningMode spawningMode = new GrowAndShrinkSpawningMode(null, 100 * TIME.SECOND, 500, 600 * TIME.SECOND, false);

        double[] expectedSpawningTimes = new double[]{
                100.010,
                172.000,
                243.978,
                266.763,
                280.299,
                292.293,
                303.527,
                314.164,
                324.297,
                333.994,
                343.310,
                352.287,
                360.963,
                369.365,
                377.521,
                385.449,
                393.170,
                400.000,
                407.200,
                414.400,
                421.600,
                428.800,
                436.000,
                443.200,
                450.000,
                456.918,
                464.118,
                471.630,
                479.496,
                487.770,
                496.518,
                505.828,
                515.815,
                526.642,
                538.540,
                551.870,
                567.230,
                585.749,
                610.023
        };

        int spawnItem = 0;
        long nextSpawningTime = 100 * TIME.SECOND;
        while (spawningMode.isSpawningActive(nextSpawningTime)) {
            nextSpawningTime = spawningMode.getNextSpawningTime(nextSpawningTime);
            assertEquals(expectedSpawningTimes[spawnItem++], (double) nextSpawningTime / TIME.SECOND, 0.01d);
        }
    }


    @Test
    public void linearGrowthWithRandom() {
        RandomNumberGenerator rng = new DefaultRandomNumberGenerator(45729834729L);

        SpawningMode spawningMode = new GrowAndShrinkSpawningMode(rng, 100 * TIME.SECOND, 500, 600 * TIME.SECOND, false);

        double[] expectedSpawningTimes = new double[]{
                100.010,
                176.865,
                242.842,
                263.729,
                277.049,
                290.144,
                301.254,
                311.933,
                322.550,
                333.234,
                342.675,
                351.153,
                359.804,
                368.854,
                376.406,
                384.082,
                391.415,
                399.404,
                400.000,
                407.581,
                414.575,
                422.310,
                429.448,
                437.105,
                443.667,
                450.000,
                457.279,
                464.825,
                471.792,
                479.871,
                487.856,
                496.841,
                506.496,
                516.861,
                528.481,
                540.524,
                554.560,
                570.153,
                587.662,
                614.822
        };

        int spawnItem = 0;
        long nextSpawningTime = 100 * TIME.SECOND;
        while (spawningMode.isSpawningActive(nextSpawningTime)) {
            nextSpawningTime = spawningMode.getNextSpawningTime(nextSpawningTime);
            assertEquals(expectedSpawningTimes[spawnItem++], (double) nextSpawningTime / TIME.SECOND, 0.01d);
        }
    }


}