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

package org.eclipse.mosaic.lib.math;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DefaultRandomNumberGeneratorTest {

    private RandomNumberGenerator rng;

    @Before
    public void initRandomNumberGenerator() {
        rng = new DefaultRandomNumberGenerator(92659278482719L);
    }

    @Test
    public void nextDouble() {
        double d = rng.nextDouble();

        assertEquals(0.6003, d, 0.0001d);
    }

    @Test
    public void nextDouble_inRange() {
        double d = rng.nextDouble(10, 20);

        assertEquals(16, d, 0.01d);
    }

    @Test
    public void nextDouble_negative() {
        for (int i = 0; i < 10000; i++) {
            double d = rng.nextDouble(-100d, 0);
            assertTrue(d < 0 && d >= -100d);
        }
    }

    @Test
    public void nextInt_inRange() {
        int i = rng.nextInt(10, 20);

        assertEquals(16, i);
    }

    @Test
    public void nextLong_inRange() {
        long i = rng.nextLong(100L, 1000000L);

        assertEquals(600385, i);
    }

    @Test
    public void nextDouble_minEqualsMax() {
        double d = rng.nextDouble(100d, 100d);

        assertEquals(100, d, 0.0001d);
    }

    @Test
    public void nextInt_maxAlwaysExclusive() {
        for (int i = 0; i < 10000; i++) {
            assertEquals(0, rng.nextInt(0, 1));
        }
    }

    @Test
    public void nextLong_maxAlwaysExclusive() {
        for (int i = 0; i < 10000; i++) {
            assertEquals(0, rng.nextLong(0, 1));
        }
    }

    @Test
    public void nextGaussian_simple() {
        double g = rng.nextGaussian();
        assertEquals(0.33135670779567394, g, 0.000000001);
    }

    @Test
    public void nextGaussian_alwaysInRange() {
        int counter = 100000;
        int from80to120 = 0;
        int from60to140 = 0;
        int from40to160 = 0;
        for (int i = 0; i <= counter; i++) {
            double g = rng.nextGaussian(100.0, 20);
            if (80.0 <= g && g <= 120.0) {
                from80to120++;
                from60to140++;
                from40to160++;
            } else if (60.0 <= g && g <= 140.0) {
                from60to140++;
                from40to160++;
            } else if (40.0 <= g && g <= 160.0) {
                from40to160++;
            }
        }
        assertTrue((double) from80to120 / (double) counter <= 0.683 + 0.01);
        assertTrue((double) from60to140 / (double) counter <= 0.954 + 0.0075);
        assertTrue((double) from40to160 / (double) counter <= 0.997 + 0.005);
    }

    @Test
    public void nextGaussian() {
        double total = 0;
        for (double i = 0; i < 1000; i++) {
            total += rng.nextGaussian(1.0, 0.1);
        }
        assertEquals(total / 1000, 1.0, 0.01d);
    }

    @Test
    public void seed_sameSeed_sameResults() {
        long seed = 1000L;
        double[] first = createRandomNumbers(new DefaultRandomNumberGenerator(seed), 100);
        double[] second = createRandomNumbers(new DefaultRandomNumberGenerator(seed), 100);

        assertArrayEquals(first, second, 0.0001d);
    }

    @Test
    public void seed_differentSeed_differentResults() {
        double[] first = createRandomNumbers(new DefaultRandomNumberGenerator(1000L), 100);
        double[] second = createRandomNumbers(new DefaultRandomNumberGenerator(2000L), 100);

        boolean notEquals = false;
        for (int i = 0; i < first.length; i++) {
            notEquals |= !(Math.abs(first[i] - second[i]) < 0.0001d);
        }
        assertTrue(notEquals);
    }

    @Test
    public void seed_noSeed_differentResults() throws InterruptedException {
        double[] first = createRandomNumbers(new DefaultRandomNumberGenerator(), 100);
        Thread.sleep(100);
        double[] second = createRandomNumbers(new DefaultRandomNumberGenerator(), 100);

        boolean notEquals = false;
        for (int i = 0; i < first.length; i++) {
            notEquals |= !(Math.abs(first[i] - second[i]) < 0.0001d);
        }
        assertTrue(notEquals);
    }

    private double[] createRandomNumbers(RandomNumberGenerator rng, int num) {
        double[] result = new double[num];
        for (int i = 0; i < num; i++) {
            result[i] = rng.nextDouble();
        }
        return result;
    }

}
