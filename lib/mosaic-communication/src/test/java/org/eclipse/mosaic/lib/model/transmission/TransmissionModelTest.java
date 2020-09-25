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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransmissionModelTest {

    private static final Logger log = LoggerFactory.getLogger(TransmissionModelTest.class);

    private final RandomNumberGenerator rng = new DefaultRandomNumberGenerator(45729834729L);

    @Test
    public void noLoss() {
        //RUN
        TransmissionResult result = TransmissionModel.simulateTransmission(rng, 0.0f, 0);

        //ASSERT
        assertTrue(result.success);
    }

    @Test
    public void fullLoss() {
        //RUN
        TransmissionResult result = TransmissionModel.simulateTransmission(rng, 1.0f, 0);

        //ASSERT
        assertFalse(result.success);
    }

    @Test
    public void lowLoss() {
        //SETUP
        CTransmission transmission = new CTransmission();
        transmission.lossProbability = 0.2f;
        transmission.maxRetries = Integer.MAX_VALUE;

        //RUN + ASSERT
        double avgAttempts = simulateTransmission(transmission, 10000);

        //ASSERT
        assertEquals(1.25, avgAttempts, 0.3d);
    }

    @Test
    public void highLoss() {
        //SETUP
        CTransmission transmission = new CTransmission();
        transmission.lossProbability = 0.8f;
        transmission.maxRetries = Integer.MAX_VALUE;

        //RUN + ASSERT
        double avgAttempts = simulateTransmission(transmission, 10000);

        //ASSERT
        assertEquals(5.0, avgAttempts, 0.5d);
    }

    private double simulateTransmission(CTransmission transmission, int iterations) {
        TransmissionResult tr;
        int totalAttempts = 0;
        for (int i = 0; i < iterations; i++) {
            tr = TransmissionModel.simulateTransmission(rng, transmission.lossProbability, transmission.maxRetries);
            totalAttempts += tr.attempts;

            assertTrue(tr.success);
        }
        return totalAttempts / (double) iterations;
    }

    @Test
    public void testLossProbability() {
        CTransmission transmission = new CTransmission();

        TransmissionResult tr;
        // Lossfree Region always delivers success@1st attempt
        transmission.lossProbability = 0f;
        transmission.maxRetries = 0;
        tr = TransmissionModel.simulateTransmission(rng, transmission.lossProbability, transmission.maxRetries);
        log.debug("Lossfree Region (retries=0): attempts={}, success={}", tr.attempts, tr.success);
        assertEquals(1, tr.attempts);
        assertTrue(tr.success);

        transmission.lossProbability = 0f;
        transmission.maxRetries = 3;
        tr = TransmissionModel.simulateTransmission(rng, transmission.lossProbability, transmission.maxRetries);
        log.debug("Lossfree Region (retries=3): attempts={}, success={}", tr.attempts, tr.success);
        assertEquals(1, tr.attempts);
        assertTrue(tr.success);

        // Lossy Region always tries max times, but never succeeds
        transmission.lossProbability = 1f;
        transmission.maxRetries = 0;
        tr = TransmissionModel.simulateTransmission(rng, transmission.lossProbability, transmission.maxRetries);
        log.debug("Lossy Region (retries=0): attempts={}, success={}", tr.attempts, tr.success);
        assertEquals(transmission.maxRetries + 1, tr.attempts);
        assertFalse(tr.success);

        transmission.lossProbability = 1f;
        transmission.maxRetries = 3;
        tr = TransmissionModel.simulateTransmission(rng, transmission.lossProbability, transmission.maxRetries);
        log.debug("Lossy Region (transmission.maxRetries=3): attempts={}, success={}", tr.attempts, tr.success);
        assertEquals(transmission.maxRetries + 1, tr.attempts);
        assertFalse(tr.success);

        // Intermediate Region delivers randomly mixed results
        transmission.lossProbability = 0.8f;
        transmission.maxRetries = 3;
        for (int i = 0; i < 5; i++) {
            tr = TransmissionModel.simulateTransmission(rng, transmission.lossProbability, transmission.maxRetries);
            log.debug("Intermediate Region (transmission.maxRetries=3) - {}. run: attempts={}, success={}", i, tr.attempts, tr.success);
        }
    }
}
