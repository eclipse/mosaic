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
 * Specialized delay model for constant delay, which delivers always the same delay.
 */
public final class ConstantDelay extends Delay {
    // Simply, the constant delay in [ns]
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public long delay;

    @Override
    public String toString() {
        return "[delayType: ConstantDelay, delay: " + delay + "]";
    }

    @Override
    public long generateDelay(RandomNumberGenerator randomNumberGenerator, double speedOfNode) {
        return (delay);
    }
}
