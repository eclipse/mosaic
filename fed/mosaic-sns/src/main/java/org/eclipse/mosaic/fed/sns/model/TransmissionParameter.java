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

package org.eclipse.mosaic.fed.sns.model;

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.delay.Delay;
import org.eclipse.mosaic.lib.model.transmission.CTransmission;

/**
 * Class holding all necessary information for the calculation of a transmission.
 */
public class TransmissionParameter {
    /**
     * {@link RandomNumberGenerator} to be used for transmission calculation.
     */
    public RandomNumberGenerator randomNumberGenerator;

    /**
     * Delay configuration for the transmission.
     */
    public Delay delay;

    /**
     * Contains parameters regarding the loss-probability and the maxRetries.
     */
    public CTransmission transmission;

    /**
     * The time to live (#hops) for the transmission.
     */
    public int ttl;

    /**
     * Creates a {@link TransmissionParameter}-object.
     *
     * @param randomNumberGenerator {@link RandomNumberGenerator} to be used for transmission calculation
     * @param delay           delay for the transmission
     * @param ttl             time to live for the transmission

     */
    public TransmissionParameter(RandomNumberGenerator randomNumberGenerator, Delay delay, CTransmission transmission,
                                 int ttl) {
        this.randomNumberGenerator = randomNumberGenerator;
        this.delay = delay;
        this.transmission = transmission;
        this.ttl = ttl;
    }
}
