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

package org.eclipse.mosaic.lib.model.delay;

import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GammaRandomDelayTest {

    private final GammaRandomDelay delay = new GammaRandomDelay();

    private final RandomNumberGenerator randomNumberGenerator = new DefaultRandomNumberGenerator(0);

    @Test
    public void randomDelay_exp0() {
        //SETUP
        delay.expDelay = 0;
        delay.minDelay = 0;

        //RUN
        long delayNsAt = averageGammaDelay(10000);

        //ASSERT
        Assert.assertEquals(0, (double) delayNsAt, 0.001d);
    }

    @Test
    public void randomDelay_exp80() {
        //SETUP
        delay.expDelay = 80;
        delay.minDelay = 40;

        //RUN
        long delayNsAt = averageGammaDelay(10000);

        //ASSERT
        Assert.assertEquals(80, (double) delayNsAt, 2d);
    }

    @Test
    public void randomDelay_exp100() {
        //SETUP
        delay.expDelay = 100;
        delay.minDelay = 20;

        //RUN
        long delayNsAt = averageGammaDelay(10000);

        //ASSERT
        Assert.assertEquals(100, (double) delayNsAt, 2d);
    }

    private long averageGammaDelay(int iterations) {
        long delayTotal = 0;
        long delayValue;
        for (int i = 0; i < iterations; i++) {
            delayValue = delay.generateDelay(randomNumberGenerator, -1);
            assertTrue(delayValue >= delay.minDelay);

            delayTotal += delayValue;
        }
        return delayTotal / iterations;
    }

}
