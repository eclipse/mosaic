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

package org.eclipse.mosaic.fed.mapping.config;


/**
 * Class that contains options for the parametrization of the Mapping.
 */
public class CMappingConfiguration {

    /**
     * Scales the traffic by the given factor. E.g. 2.0 would
     * double the number of spawned vehicles
     */
    public double scaleTraffic = 1.0;

    /**
     * Defines the point in time (in seconds) to start spawning vehicles. If not set (default),
     * all vehicles will be spawned according to the vehicles configuration.
     */
    public Double start;

    /**
     * Defines the point in time (in seconds) to end spawning vehicles. If not set (default),
     * all vehicles will be spawned according to the vehicles configuration or until the simulation ends.
     */
    public Double end;

    /**
     * If set to <code>true</code> and if the parameter <code>start</code> is set, the starting
     * times of each spawner is adjusted accordingly, so that we shouldn't wait
     * in case that simulation starting time and spawner starting time are widely spread out.
     * All spawners before <code>start</code> will be completely ignored then.
     */
    public boolean adjustStartingTimes = false;

    /**
     * If set to <code>true</code>, all flow definitions defined by vehicle spawners with more than 1 vehicle
     * result in slightly randomized departure times. The specified `targetFlow` of the vehicle spawner is kept.
     */
    public boolean randomizeFlows = false;

    /**
     * If set to {@code true}, the starting times of all vehicle spawner definitions are randomized by {@code +-60} seconds.
     */
    public boolean randomizeStartingTimes = false;

    /**
     * If set to {@code true}, the configured weights of all types are slightly randomized by {@code +-1%} of the sum of all weights.
     */
    public boolean randomizeWeights = false;
}
