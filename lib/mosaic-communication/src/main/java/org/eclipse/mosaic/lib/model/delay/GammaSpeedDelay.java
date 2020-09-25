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

/**
 * The GammaSpeedDelay-Type calculates a delay derived from the GammaDistribution
 * and also including a certain impairment for higher vehicle speeds.
 */
public class GammaSpeedDelay extends GammaRandomDelay {

    @Override
    public long generateDelay(RandomNumberGenerator randomNumberGenerator, double speedOfNode) {
        final long grDelay = super.generateDelay(randomNumberGenerator, speedOfNode);
        final double speedPenalty = getSpeedPenalty(speedOfNode);

        return (long) (grDelay * speedPenalty);
    }

    /**
     * Estimator function that fits a experimental graph illustrating the additional delay
     * experienced through relative motion (according to the smartv2x measurements).
     *
     * @param speed velocity of the UE in m/s
     * @return The speed penalty
     */
    private static double getSpeedPenalty(double speed) {
        // Convert speed as the graph is fitted to km/h
        speed *= 3.6;
        // Define coefficients as estimated by a fitting algorithm
        final double a = 1;
        final double b = -9.258e-04;
        final double c = 7.357e-05;
        // Calc speed penalty with quadratic polynomial function
        double penalty = a + b * speed + c * Math.pow(speed, 2.0);
        // Note: for small speed-values the penalty will be fitted to a factor below 1.0, therefore we limit it to 1.0 here
        return Math.max(penalty, 1.0);
    }
}
