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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GammaSpeedDelayTest {

    private final GammaDelay delay = new GammaSpeedDelay();

    private final RandomNumberGenerator randomNumberGenerator = new DefaultRandomNumberGenerator(0);
    
    @Before
    public void setup() {
        delay.expDelay = 80;
        delay.minDelay = 40;
    }

    @Test
    public void delay_vel1mpers() {
        //RUN
        long delayNsAt = averageGammaDelay(1d, 10000);

        //ASSERT
        Assert.assertEquals(80, (double) delayNsAt, 2d);
    }

    @Test
    public void delay_vel10mpers() {
        //RUN
        long delayNsAt = averageGammaDelay(10d, 10000);

        //ASSERT
        Assert.assertEquals(85, (double) delayNsAt, 2d);
    }

    @Test
    public void delay_vel40mpers() {
        //RUN
        long delayNsAt = averageGammaDelay(40d, 10000);

        //ASSERT
        Assert.assertEquals(191, (double) delayNsAt, 3d);
    }

    private long averageGammaDelay(double velocity, int iterations) {
        long delayTotal = 0;
        long delayValue;
        for (int i = 0; i < iterations; i++) {
            delayValue = delay.generateDelay(randomNumberGenerator, velocity);
            assertTrue(delayValue >= delay.minDelay);

            delayTotal += delayValue;
        }
        return delayTotal / iterations;
    }
}
