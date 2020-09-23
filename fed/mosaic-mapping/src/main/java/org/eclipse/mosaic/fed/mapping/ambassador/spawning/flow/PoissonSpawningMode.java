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

import javax.annotation.Nullable;

/**
 * The poisson mode of spawning vehicles with exponential time gaps
 * based on a given target flow.
 *
 * @author arvieira@labnet.nce.ufrj.br
 */
public class PoissonSpawningMode implements SpawningMode {

    private final RandomNumberGenerator rng;

    private final Long spawningEndTime;
    private final double lambda;

    private long nextSpawnTime;

    /**
     * Constructor for {@link PoissonSpawningMode}.
     *
     * @param start         spawning start time [ns]
     * @param spawnsPerHour the target flow of vehicles to be spawned [veh/hour]
     * @param end           spawning end time [ns]
     */
    public PoissonSpawningMode(RandomNumberGenerator rng, long start, double spawnsPerHour, @Nullable Long end) {
        this.spawningEndTime = end;
        this.rng = rng;
        this.nextSpawnTime = start;

        this.lambda = (spawnsPerHour / TIME.HOUR);
    }

    @Override
    public boolean isSpawningActive(long currentTime) {
        return spawningEndTime == null || spawningEndTime > currentTime;
    }

    @Override
    public long getNextSpawningTime(long currentTime) {
        long result = nextSpawnTime;
        long step = (long) (Math.log(1 - (rng.nextDouble())) / (-lambda));
        nextSpawnTime += step;
        return result;
    }
}
