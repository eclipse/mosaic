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

import org.apache.commons.math3.distribution.GammaDistribution;

/**
 * The GammaRandomDelay to model a more realistic delay distribution, which is in line
 * with the measurements from the campaign in the smartv2x project.
 * <p>
 * (This model is also the basis of the GammaSpeedDelay, which simply adds an additional speed penalty).
 * </p>
 */
public class GammaRandomDelay extends GammaDelay {

    /**
     * GammaDistribution with p=2, b=2, which fits best to the delay measurements in the smartv2x project.
     */
    private final static GammaDistribution GAMMA = new GammaDistribution(2, 2);

    @Override
    public long generateDelay(RandomNumberGenerator randomNumberGenerator, double speedOfNode) {
        // but needs factor of 1/4
        double factor = 0.25 * (expDelay - minDelay);
        // Get gamDelay from gammaDistribution (in [ms])
        double gamDelay = GAMMA.inverseCumulativeProbability(randomNumberGenerator.nextDouble()) * factor;
        // Calculate returned retDelay in [ns]

        return (long) ((gamDelay + minDelay));
    }
}
