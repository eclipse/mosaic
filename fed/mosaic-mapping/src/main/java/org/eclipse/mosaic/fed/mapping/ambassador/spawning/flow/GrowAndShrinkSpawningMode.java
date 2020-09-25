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

import static java.lang.Math.max;

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.rti.TIME;

/**
 * This implementation of {@link SpawningMode} spawns the vehicles in a
 * way where first the flow is increased, then it stays constant and afterwards
 * it is decreased.
 */
public class GrowAndShrinkSpawningMode implements SpawningMode {

    private static final int PHASES_INCREASE = 6;
    private static final int PHASES_CONSTANT = 1;
    private static final int PHASES_DECREASE = 3;

    private final SpawningMode increaseMode;
    private final long endIncrease;

    private final SpawningMode constantMode;
    private final long endConstant;

    private final SpawningMode decreaseMode;
    private final long endDecrease;

    /**
     * Constructor for {@link GrowAndShrinkSpawningMode}.
     *
     * @param rng           the {@link RandomNumberGenerator} for the spawner
     * @param start         spawning start time [ns]
     * @param spawnsPerHour the target flow of vehicles to be spawned [veh/hour]
     * @param end           spawning end time [ns]
     * @param exponential   flag if flow should be linearly or exponentially increased/decreased
     */
    public GrowAndShrinkSpawningMode(RandomNumberGenerator rng, long start, double spawnsPerHour, long end, boolean exponential) {
        // spawning phase duration determined by total amount of time divided by total amount of phases
        long durationPerPhase = (end - start) / (PHASES_INCREASE + PHASES_DECREASE + PHASES_CONSTANT);
        // calculate the end times of the phases, used to set the durations of the spawners
        endIncrease = start + durationPerPhase * PHASES_INCREASE;
        endConstant = endIncrease + durationPerPhase * PHASES_CONSTANT;
        endDecrease = endConstant + durationPerPhase * PHASES_DECREASE;
        // define spawning modes with calculated durations
        constantMode = new ConstantSpawningMode(rng, endIncrease, spawnsPerHour, endConstant);
        if (exponential) {
            increaseMode = new AbstractSpawningMode.IncreaseExponential(rng, start, spawnsPerHour, endIncrease);
            decreaseMode = new AbstractSpawningMode.DecreaseExponential(rng, endConstant, spawnsPerHour, endDecrease);
        } else {
            increaseMode = new AbstractSpawningMode.IncreaseLinear(rng, start, spawnsPerHour, endIncrease);
            decreaseMode = new AbstractSpawningMode.DecreaseLinear(rng, endConstant, spawnsPerHour, endDecrease);
        }

    }

    @Override
    public boolean isSpawningActive(long currentTime) {
        return currentTime < endDecrease;
    }

    @Override
    public long getNextSpawningTime(long currentTime) {
        long nextSpawningTime = increaseMode.getNextSpawningTime(currentTime);  // increase phase started
        if (nextSpawningTime >= endIncrease) {  // increase phase finished, constant phase started
            nextSpawningTime = constantMode.getNextSpawningTime(currentTime);
        }
        if (nextSpawningTime >= endConstant) {  // constant phase finished, decrease phase started
            nextSpawningTime = decreaseMode.getNextSpawningTime(currentTime);
        }
        return max(nextSpawningTime, currentTime + 10 * TIME.MILLI_SECOND);
    }
}
