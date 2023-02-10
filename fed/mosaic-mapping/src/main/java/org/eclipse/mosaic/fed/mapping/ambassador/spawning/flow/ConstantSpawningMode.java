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


import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.rti.TIME;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The default mode of spawning vehicles with constant time gaps
 * based on a given target flow.
 */
public class ConstantSpawningMode extends AbstractSpawningMode {

    private final long timeSpacing;

    /**
     * Constructor for {@link ConstantSpawningMode}.
     *
     * @param rng           the {@link RandomNumberGenerator} for the spawner
     * @param start         spawning start time [ns]
     * @param spawnsPerHour the target flow of vehicles to be spawned [veh/hour]
     * @param end           spawning end time [ns]
     */
    public ConstantSpawningMode(RandomNumberGenerator rng, long start, double spawnsPerHour, Long end) {
        super(rng, start, spawnsPerHour, end);
        this.timeSpacing = (long) (TIME.HOUR / spawnsPerHour);
    }

    @Override
    public boolean isSpawningActive(long currentTime) {
        return end > currentTime;
    }

    @Override
    public long getNextSpawningTime(long currentTime) {
        // spawning time returned and incremented by constant value, which can be affected by noise
        long spawnTime = nextSpawningTime;
        nextSpawningTime += noise(timeSpacing);
        return spawnTime;
    }

    @Override
    double getFlowByTime(long time) {
        return maximumFlow;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("timeSpacing", timeSpacing)
                .build();
    }
}
