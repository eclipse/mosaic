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

import java.util.List;

public interface RandomNumberGenerator {

    /**
     * Returns a random double between 0 and 1.
     *
     * @return the random double
     */
    double nextDouble();

    /**
     * Returns a random double within the specified range.
     *
     * @param min the smallest value that can be returned (inclusive)
     * @param max the greatest value that can be returned (exclusive)
     * @return the random double
     */
    default double nextDouble(double min, double max) {
        if (max < min) {
            throw new IllegalArgumentException("Min value must be smaller or equal to end value.");
        }
        return min == max
                ? min
                : min + (max - min) * nextDouble();
    }

    /**
     * Returns a random integer;
     *
     * @return the random int
     */
    default int nextInt() {
        return Math.toIntExact(nextLong(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    /**
     * Returns a random integer within the specified range.
     *
     * @param max the greatest value that can be returned (exclusive)
     * @return the random int
     */
    default int nextInt(int max) {
        return Math.toIntExact(nextLong(0, max));
    }

    /**
     * Returns a random integer within the specified range.
     *
     * @param min the smallest value that can be returned (inclusive)
     * @param max the greatest value that can be returned (exclusive)
     * @return the random int
     */
    default int nextInt(int min, int max) {
        return Math.toIntExact(nextLong(min, max));
    }

    /**
     * Returns a random long within the specified range.
     *
     * @param min the smallest value that can be returned (inclusive)
     * @param max the greatest value that can be returned (exclusive)
     * @return the random long
     */
    default long nextLong(long min, long max) {
        return (long) nextDouble(min, max);
    }

    /**
     * Returns a random boolean.
     *
     * @return the random boolean
     */
    default boolean nextBoolean() {
        return nextDouble() < 0.5;
    }

    /**
     * Returns a Gaussian distributed random double between -1.0 and 1.0.
     *
     * @return the Gaussian distributed random double
     */
    double nextGaussian();

    /**
     * Returns a Gaussian distributed random double from [mean - deviation * mean, mean + deviation * mean].
     *
     * @param mean      the mean value of the distribution
     * @param deviation the standard deviation of the distribution
     * @return the Gaussian distributed random double
     */
    default double nextGaussian(double mean, double deviation) {
        return mean + nextGaussian() * deviation;
    }

    /**
     * Randomly permutes the specified list using this source of randomness.
     *
     * @param list the list to permute
     */
    void shuffle(List<?> list);

}
