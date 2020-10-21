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

package org.eclipse.mosaic.fed.cell.utility;

import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.transmission.TransmissionModel;
import org.eclipse.mosaic.lib.model.transmission.TransmissionResult;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Cellular Pr/Pl utility to determine packet retransmission and loss in regions.
 * (relies on the logic of the common {@link TransmissionModel#simulateTransmission} in MOSAIC-communication)
 */
@SuppressWarnings(value = {"NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"}, justification = "filled by json")
public class RetransmissionLossUtility {

    /**
     * Determine the number and success of the transmission attempts
     * for the given message in the according region
     * with the help of the DirectTransmission.
     *
     * @param region Region where the node is located
     * @param mode   Transmission mode (up, down, - uni,multi)
     * @return TransmissionResult, which includes number of attempts and success
     */
    public static TransmissionResult determineTransmissionAttempts(RandomNumberGenerator rng, CNetworkProperties region, TransmissionMode mode) {
        // Get parameters for the (region, transmission mode)-config
        double lossProbability;
        int maxRetries;
        switch (mode) {
            case UplinkUnicast:
                lossProbability = region.uplink.transmission.lossProbability;
                maxRetries = region.uplink.transmission.maxRetries;
                break;
            case DownlinkUnicast:
                lossProbability = region.downlink.unicast.transmission.lossProbability;
                maxRetries = region.downlink.unicast.transmission.maxRetries;
                break;
            case DownlinkMulticast:
                lossProbability = region.downlink.multicast.transmission.lossProbability;
                maxRetries = region.downlink.multicast.transmission.maxRetries;
                break;
            default:
                throw new RuntimeException("Unknown TransmissionMode: " + mode);
        }

        return TransmissionModel.simulateTransmission(rng, lossProbability, maxRetries);
    }
}
