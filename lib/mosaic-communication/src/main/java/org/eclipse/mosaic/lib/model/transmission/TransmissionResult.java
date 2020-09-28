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

/**
 * Result from Pr/Pl Model for determination of packet retransmission and loss.
 */
public class TransmissionResult {

    /**
     * Number of transmission attempts.
     */
    public int attempts;

    /**
     * Number of hops of transmission.
     */
    public int numberOfHops;

    /**
     * Successful or unsuccessful transmission.
     */
    public boolean success;

    /**
     * Transmission delay.
     */
    public long delay;

    /**
     * Create a new default {@link TransmissionResult} object.
     */
    public TransmissionResult() {
    }

    public TransmissionResult(boolean success) {
        this.success = success;
    }

    /**
     * Create a new {@link TransmissionResult} object with specified input parameter.
     *
     * @param attempts Transmission attempts.
     * @param success Transmission status, the transmission is successful {true else false}.
     */
    public TransmissionResult(int attempts, boolean success) {
        this.attempts = attempts;
        this.success = success;
    }

    /**
     * Create a new {@link TransmissionResult} object with specified input parameter.
     *
     * @param success transmission status, the transmission is successful {true else false}.
     * @param delay Transmission delay.
     */
    public TransmissionResult(boolean success, long delay) {
        this.success = success;
        this.delay = delay;
    }

    /**
     * Create a new {@link TransmissionResult} object while cloning from the previous {@link TransmissionResult} object.
     *
     * @param cloneFrom object to be cloned
     */
    public TransmissionResult(TransmissionResult cloneFrom) {
        this.attempts = cloneFrom.attempts;
        this.numberOfHops = cloneFrom.numberOfHops;
        this.success = cloneFrom.success;
        this.delay = cloneFrom.delay;
    }
}
