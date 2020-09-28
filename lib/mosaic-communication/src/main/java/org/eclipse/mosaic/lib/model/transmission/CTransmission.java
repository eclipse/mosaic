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

public class CTransmission {

    /**
     * Probability of packet retransmission (in case of configured retries > 0)
     * or packet loss (retries = 0) for the packet retransmission/loss model
     * lossProbability should be between 0 and 1 ( lossProbability[0,1] )
     * means that lossProbability=0 (lossfree), lossProbability=1 (100% lossy).
     */
    public double lossProbability = 0.0d;

    /**
     * Maximum Number of retransmissions.
     */
    public int maxRetries;
}
