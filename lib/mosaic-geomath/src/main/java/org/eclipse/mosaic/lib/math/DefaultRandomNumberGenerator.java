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

package org.eclipse.mosaic.lib.math;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DefaultRandomNumberGenerator implements RandomNumberGenerator {

    private final Random random;

    public DefaultRandomNumberGenerator() {
        this(new Random());
    }

    public DefaultRandomNumberGenerator(long seed) {
        this(new Random(seed));
    }

    public DefaultRandomNumberGenerator(Random randomImpl) {
        this.random = randomImpl;
    }

    @Override
    public synchronized double nextDouble() {
        synchronized (random) {
            return random.nextDouble();
        }
    }

    @Override
    public double nextGaussian() {
        synchronized (random) {
            return random.nextGaussian();
        }
    }

    public void shuffle(List<?> list) {
        synchronized (random) {
            Collections.shuffle(list, random);
        }
    }
}
