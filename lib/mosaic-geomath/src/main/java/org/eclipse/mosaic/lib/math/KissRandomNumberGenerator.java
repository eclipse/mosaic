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

import java.util.Random;

public class KissRandomNumberGenerator extends DefaultRandomNumberGenerator {

    public KissRandomNumberGenerator() {
        super(new KissRandom());
    }

    public KissRandomNumberGenerator(long seed) {
        super(new KissRandom(seed));
    }

    private static class KissRandom extends Random {

        private long x;
        private long y = 362436362436362436L;
        private long z = 1234567890987654321L;
        private long c = 123456123456123456L;

        private KissRandom() {
            this((long) ((Math.random() - 0.5) * 2 * Long.MAX_VALUE));
        }

        private KissRandom(long seed) {
            x = seed;
        }

        @Override
        protected int next(int bits) {
            // linear congruential generator
            x = 6906969069L * x + 1234567;

            // Xorshift
            y ^= y << 13;
            y ^= y >> 17;
            y ^= y << 43;

            // Multiply-with-carry
            long t = (z << 58) + c;
            c = z >> 6;
            z += t;
            c += z < t ? 1 : 0;

            return (int) ((x + y + z) >> 16) >>> (32 - bits);
        }
    }
}
