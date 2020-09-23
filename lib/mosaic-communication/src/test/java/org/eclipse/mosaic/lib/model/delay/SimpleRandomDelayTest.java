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

package org.eclipse.mosaic.lib.model.delay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

public class SimpleRandomDelayTest {

    private final RandomNumberGenerator randomNumberGenerator = new DefaultRandomNumberGenerator(0);

    private final SimpleRandomDelay delay = new SimpleRandomDelay();

    @Test
    public void randomDelay_steps1_onePossibleDelay() {
        delay.steps = 1;
        delay.minDelay = 40;
        delay.maxDelay = 80;

        //RUN + ASSERT
        long[] validDelayValues = {60};

        long averageDelayInNs = averageRandomDelay(10000, validDelayValues);

        Assert.assertEquals(60, (double) averageDelayInNs, 1d);
    }

    @Test
    public void randomDelay_exp0() {
        //SETUP
        delay.steps = 2;
        delay.minDelay = 0;
        delay.maxDelay = 0;

        //RUN
        long[] validDelayValues = {0};

        long averageDelayInNs = averageRandomDelay(10000, validDelayValues);

        assertEquals(0, averageDelayInNs);
    }

    @Test
    public void randomDelay_steps2_twoPossibleDelays() {
        //SETUP
        delay.steps = 2;
        delay.minDelay = 40;
        delay.maxDelay = 80;

        //RUN + ASSERT
        long[] validDelayValues = {40, 80};

        long averageDelayInNs = averageRandomDelay(10000, validDelayValues);

        Assert.assertEquals(60, (double) averageDelayInNs, 1d);
    }

    @Test
    public void randomDelay_steps5_fivePossibleDelays() {
        //SETUP
        delay.steps = 5;
        delay.minDelay = 40;
        delay.maxDelay = 80;

        //RUN + ASSERT
        long[] validValues = {40, 50, 60, 70, 80};

        long averageDelayInNs = averageRandomDelay(10000, validValues);

        Assert.assertEquals(60, (double) averageDelayInNs, 1d);
    }

    @Test
    public void randomDelay_steps5_minEqualsMax_1PossibleDelay() {
        //SETUP
        delay.steps = 5;
        delay.minDelay = 80;
        delay.maxDelay = 80;

        //RUN + ASSERT
        long[] validValues = {80};

        long averageDelayInNs = averageRandomDelay(10000, validValues);

        Assert.assertEquals(80, (double) averageDelayInNs, 1d);
    }

    private long averageRandomDelay(int iterations, long[] validValues) {
        long delayTotal = 0;
        long delayValue;
        for (int i = 0; i < iterations; i++) {
            delayValue = delay.generateDelay(randomNumberGenerator, -1);

            assertTrue(ArrayUtils.contains(validValues, delayValue));

            delayTotal += delayValue;
        }
        return delayTotal / iterations;
    }
}
