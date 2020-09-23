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

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.rti.TIME;

import java.security.InvalidParameterException;

/**
 * This class can be used to specify your own {@link SpawningMode}'s.
 * With the classes {@link IncreaseExponential}, {@link DecreaseExponential}, {@link IncreaseLinear} and {@link DecreaseLinear}
 * it offers preconfigured spawning modes.
 */
public abstract class AbstractSpawningMode implements SpawningMode {

    private static final long MIN_SPACING = 50 * TIME.MILLI_SECOND;
    private static final double EXP_BASE = 10;
    private static final double NOISE_MIN = 0.9;
    private static final double NOISE_MAX = 1.1;
    private static final double MINIMAL_FLOW_PERCENTAGE = 0.1;
    private static final double FLOW_PERCENTAGE = 1.0 - MINIMAL_FLOW_PERCENTAGE;

    private final RandomNumberGenerator rng;

    /**
     * Increases the flow of spawning vehicles exponentially up to the given target flow
     * until the given end time as reached.
     */
    public static class IncreaseExponential extends AbstractSpawningMode {

        /**
         * Constructor for {@link IncreaseExponential}.
         *
         * @param start         the time at which spawning of vehicles should be started
         * @param spawnsPerHour the flow which is targeted (veh/hour)
         * @param end           the time at which spawning should be ended. If not given, vehicles will be spawned
         *                      forever but flow would not increase anymore after 1800 seconds of spawning.
         */
        public IncreaseExponential(RandomNumberGenerator rng, long start, double spawnsPerHour, Long end) {
            super(rng, start, spawnsPerHour, end);
        }

        double getFlowByTime(long time) {
            if (time > end) {
                return maximumFlow;
            }
            double duration = (double) (end - start);
            double timeStep = (double) (time - start);
            return Math.min(maximumFlow,
                    (maximumFlow / EXP_BASE) * Math.pow(EXP_BASE, timeStep / duration)
            );
        }
    }

    /**
     * Decreases the flow of spawning vehicles exponentially from the given target flow
     * until the given end time as reached.
     */
    public static class DecreaseExponential extends AbstractSpawningMode {

        /**
         * Constructor for {@link DecreaseExponential}.
         *
         * @param start         the time at which spawning of vehicles should be started
         * @param spawnsPerHour the flow with which spawning is started (veh/hour)
         * @param end           the time at which spawning should be ended. If not given, vehicles will be spawned
         *                      forever but flow would not decrease anymore after 1800 seconds of spawning.
         */
        public DecreaseExponential(RandomNumberGenerator rng, long start, double spawnsPerHour, Long end) {
            super(rng, start, spawnsPerHour, end);
        }

        double getFlowByTime(long time) {
            if (time > end) {
                return maximumFlow / EXP_BASE;
            }
            double duration = (double) (end - start);
            double timeStep = (double) (end - time);
            return Math.min(maximumFlow,
                    (maximumFlow / EXP_BASE) * Math.pow(EXP_BASE, timeStep / duration)
            );
        }
    }

    /**
     * Increases the flow of spawning vehicles linearly up to the given target flow
     * until the given end time as reached.
     */
    public static class IncreaseLinear extends AbstractSpawningMode {

        /**
         * Constructor for {@link IncreaseLinear}.
         *
         * @param start         the time at which spawning of vehicles should be started
         * @param spawnsPerHour the flow which is targeted (veh/hour)
         * @param end           the time at which spawning should be ended. If not given, vehicles will be spawned
         *                      forever but flow would not increase anymore after 1800 seconds of spawning.
         */
        public IncreaseLinear(RandomNumberGenerator rng, long start, double spawnsPerHour, Long end) {
            super(rng, start, spawnsPerHour, end);
        }

        double getFlowByTime(long time) {
            if (time > end) {
                return maximumFlow;
            }
            double duration = (double) (end - start);
            double timeStep = (double) (time - start);
            // minimal flow is equal to 10 percent of maximum flow
            return maximumFlow * MINIMAL_FLOW_PERCENTAGE + (timeStep * maximumFlow * FLOW_PERCENTAGE) / duration;
        }
    }

    /**
     * Decreases the flow of spawning vehicles linearly from the given target flow
     * until the given end time as reached.
     */
    public static class DecreaseLinear extends AbstractSpawningMode {

        /**
         * Constructor for {@link DecreaseLinear}.
         *
         * @param start         the time at which spawning of vehicles should be started
         * @param spawnsPerHour the flow with which spawning is started (veh/hour)
         * @param end           the time at which spawning should be ended. If not given, vehicles will be spawned
         *                      forever but flow would not decrease anymore after 1800 seconds of spawning.
         */
        public DecreaseLinear(RandomNumberGenerator rng, long start, double spawnsPerHour, Long end) {
            super(rng, start, spawnsPerHour, end);
        }

        double getFlowByTime(long time) {
            if (time > end) {
                return maximumFlow * MINIMAL_FLOW_PERCENTAGE;
            }
            double duration = (double) (end - start);
            double timeStep = (double) (end - time);
            // minimal flow is equal to 10 percent of maximum flow
            return maximumFlow * MINIMAL_FLOW_PERCENTAGE + (timeStep * maximumFlow * FLOW_PERCENTAGE) / duration;
        }
    }

    /**
     * The time when the spawner should start spawning [ns].
     */
    final long start;

    /**
     * The time when the spawner should stop spawning [ns].
     */
    final long end;

    final double maximumFlow;


    long nextSpawningTime;

    /**
     * Constructor for {@link AbstractSpawningMode}.
     *
     * @param rng           the {@link RandomNumberGenerator} for the spawner
     * @param start         spawning start time [ns]
     * @param spawnsPerHour the target flow of vehicles to be spawned [veh/hour]
     * @param end           spawning end time (ns)
     */
    AbstractSpawningMode(RandomNumberGenerator rng, long start, double spawnsPerHour, Long end) {
        if (end == null) {
            throw new InvalidParameterException("End value can't be null.");
        }
        this.rng = rng;
        this.start = start;
        this.end = end;
        this.maximumFlow = spawnsPerHour;

        nextSpawningTime = start;
    }

    @Override
    public boolean isSpawningActive(long currentTime) {
        return currentTime < end;
    }

    @Override
    public long getNextSpawningTime(long currentTime) {
        long spawningTime = nextSpawningTime;
        double currentFlow = getFlowByTime(currentTime);
        long spacing = noise((long) ((TIME.HOUR / currentFlow)));

        nextSpawningTime += Math.max(MIN_SPACING, spacing);

        return spawningTime;
    }

    protected long noise(long spacing) {
        if (rng == null) {
            return spacing;
        }
        return (long) (rng.nextDouble(NOISE_MIN, NOISE_MAX) * spacing);
    }

    abstract double getFlowByTime(long time);
}
