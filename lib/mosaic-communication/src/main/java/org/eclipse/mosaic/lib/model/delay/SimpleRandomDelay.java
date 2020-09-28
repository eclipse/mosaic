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

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.util.gson.TimeFieldAdapter;

import com.google.gson.annotations.JsonAdapter;

/**
 * SimpleRandomDelay delivers number-of-steps different uniformly distributed delays
 * in the interval defined by min and max.
 * E.g. minDelay=30ms, maxDelay=60ms, steps=4 -> possible delays={30,40,50,60}ms.
 */
public final class SimpleRandomDelay extends Delay {

    /**
     * Minimum delay in nanoseconds for the Gamma distribution.
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public long minDelay;

    /**
     * Maximum delay in nanoseconds for the Gamma distribution.
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public long maxDelay;

    /**
     * Number of possible delays between min and max.
     */
    public int steps;

    @Override
    public long generateDelay(RandomNumberGenerator randomNumberGenerator, double speedOfNode) {
        if (steps <= 1) {
            return (long) (minDelay + (maxDelay - minDelay) / 2.0);
        }
        return ((randomNumberGenerator.nextInt(0, steps) * (maxDelay - minDelay) / (steps - 1)) + minDelay);
    }

    @Override
    public String toString() {
        return "[delayType: SimpleRandomDelay, minDelay: " + minDelay + ", maxDelay " + maxDelay + ", steps: " + steps + "]";
    }
}
