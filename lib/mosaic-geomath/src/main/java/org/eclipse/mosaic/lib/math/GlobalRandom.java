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

package org.eclipse.mosaic.lib.math;

public class GlobalRandom {

    private static RandomNumberGenerator RANDOM = createGoodRand(0);

    public static RandomNumberGenerator createGoodRand() {
        return new KissRandomNumberGenerator(RANDOM.nextLong(0, Long.MAX_VALUE));
    }

    public static RandomNumberGenerator createGoodRand(long seed) {
        return new KissRandomNumberGenerator(seed);
    }

    public synchronized static void initialize(RandomNumberGenerator randomNumberGenerator) {
        GlobalRandom.RANDOM = randomNumberGenerator;
    }

    public static RandomNumberGenerator get() {
        return RANDOM;
    }

    public static double random() {
        return get().nextDouble();
    }

    public static int randomInt() {
        return get().nextInt();
    }

    public static int randomInt(int range) {
        return get().nextInt(range);
    }
}
