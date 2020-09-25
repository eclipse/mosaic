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

import org.eclipse.mosaic.lib.util.gson.TimeFieldAdapter;

import com.google.gson.annotations.JsonAdapter;

/**
 * GammaDelay holds the GammaRandomDelay and the GammaSpeedDelay, which have the same configuration.
 * GammaRandomDelay bases directly on the Gamma distribution (b=2,p=2) with
 * min and expected value. Due to the nature of the Gamma distribution,
 * the resulting delays can be far higher than the expected value.
 * GammaSpeedDelay bases on the GammaRandomDelay and includes an additional speed penalty
 * according to the current speed of the vehicle.
 */
public abstract class GammaDelay extends Delay {

    /**
     * Minimum delay in nanoseconds for the Gamma distribution.
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public long minDelay;

    /**
     * Expected delay in nanoseconds for the Gamma distribution.
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public long expDelay;

    @Override
    public String toString() {
        return "[delayType: " + getClass().getSimpleName() + ", minDelay: " + minDelay + ", expDelay: " + expDelay + "]";
    }
}
