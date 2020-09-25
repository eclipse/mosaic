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

package org.eclipse.mosaic.lib.model.transmission;

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

/**
 * Logic of the loss probability model to simulate packet transmission.
 */
public class TransmissionModel {

    /**
     * Simulate the number and success of the transmission attempts.
     *
     * @param packetLossProbability probability of packet retransmission/loss
     * @param retries               number of transmission retries
     * @return TransmissionResult, which includes number of attempts and success
     */
    public static TransmissionResult simulateTransmission(RandomNumberGenerator rng, double packetLossProbability, int retries) {
        // try transmissionAttempts until transmission is successful or
        // maximum retries are reached (retries=0 for packet loss-model, one try)
        int transmissionAttempts = 0;
        boolean transmissionSuccess = false;
        do {
            transmissionAttempts++;
            double transmissionStatistic = rng.nextDouble();
            // transmissionStatistic [0,1] needs to be greater than configured lossProbability
            // means that lossProbability=0 (lossfree), lossProbability=1 (100% lossy)
            if (transmissionStatistic > packetLossProbability) {
                transmissionSuccess = true;
                break;
            }
        } while (transmissionAttempts <= retries);

        return new TransmissionResult(transmissionAttempts, transmissionSuccess);
    }

}
