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
import org.eclipse.mosaic.fed.cell.data.SimulationData;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.delay.Delay;
import org.eclipse.mosaic.lib.model.delay.GammaSpeedDelay;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cellular delay utility to calculate the delay from within cell.
 * (Relies on the logic of the MOSAIC-communication Delay model)
 */
public final class DelayUtility {

    private static final Logger log = LoggerFactory.getLogger(DelayUtility.class);

    /**
     * Calculates the core delay for the given region.
     *
     * @param region                Region to calculate the delay for
     * @param mode                  The transmission mode either Uplink (ie Veh,Rsu -> GEO) or
     *                              Downlink (in unicast as well as multicast (ie GEO -> Veh,Rsu).
     * @param nodeId                Id of a single node for the GammaSpeedDelay
     *                              (only possible in Unicast, otherwise can be null)
     * @param rng {@link RandomNumberGenerator} to be used for delay calculation
     * @return calculatedDelay [unit: ns]
     * @throws InternalFederateException if delay type couldn't be determined, or invalid combination
     *                                   of {@link Delay} and {@link TransmissionMode} was used
     */
    public static long calculateDelay(CNetworkProperties region, TransmissionMode mode, String nodeId, RandomNumberGenerator rng) throws InternalFederateException {
        // Determine correct delay config

        Delay delay;
        try {
            delay = determineDelayType(mode, region);
        } catch (InternalFederateException e) {
            log.warn("Delay-type couldn't be determined. Reason: {}", e.getMessage());
            throw new InternalFederateException(e);
        }

        double speedOfNode = SimulationData.INSTANCE.getSpeedOfNode(nodeId);
        long delayValueInNs = delay.generateDelay(rng, speedOfNode);

        if (log.isDebugEnabled()) {
            log.debug("Calculated {}-delay for message in region \"{}\": {} ns (not considering retransmission and maximum node bandwidth)",
                    mode.toString(), region.id, delayValueInNs);
        }
        return delayValueInNs;
    }

    private static Delay determineDelayType(TransmissionMode mode, CNetworkProperties region) throws InternalFederateException {
        Delay delay;
        switch (mode) {
            case UplinkUnicast:
                delay = region.uplink.delay;
                break;
            case DownlinkUnicast:
                delay = region.downlink.unicast.delay;
                break;
            case DownlinkMulticast:
                delay = region.downlink.multicast.delay;
                break;
            default:
                throw new InternalFederateException("Unknown TransmissionMode: " + mode);
        }

        if (delay instanceof GammaSpeedDelay && mode.equals(TransmissionMode.DownlinkMulticast)) {
            throw new InternalFederateException("Impossible delay configuration for " + mode + " in region \"" + region.id + "\"");
        }

        return delay;
    }
}
