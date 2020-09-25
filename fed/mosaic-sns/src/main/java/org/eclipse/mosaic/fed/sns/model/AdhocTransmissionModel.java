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

import org.eclipse.mosaic.fed.sns.ambassador.SimulationNode;
import org.eclipse.mosaic.fed.sns.util.AdhocTransmissionModelTypeAdapterFactory;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.delay.Delay;
import org.eclipse.mosaic.lib.model.transmission.CTransmission;
import org.eclipse.mosaic.lib.model.transmission.TransmissionModel;
import org.eclipse.mosaic.lib.model.transmission.TransmissionResult;

import com.google.gson.annotations.JsonAdapter;

import java.util.Map;

/**
 * Abstract class defining necessary methods for the SNS to do calculations.
 */
@JsonAdapter(AdhocTransmissionModelTypeAdapterFactory.class)
public abstract class AdhocTransmissionModel {

    /**
     * Constructs a {@link TransmissionResult} using the given {@link Delay}.
     * Note: This method could also be overwritten if this simple way of calculating the transmission doesn't fit your needs.
     *
     * @param randomNumberGenerator {@link RandomNumberGenerator} to be used for the calculation
     * @param delay                 the actual delay
     * @return A {@link TransmissionResult} calculated using the given parameters
     */
    TransmissionResult simulateTransmission(RandomNumberGenerator randomNumberGenerator, Delay delay, CTransmission transmission) {
        TransmissionResult result =
                TransmissionModel.simulateTransmission(randomNumberGenerator, transmission.lossProbability, transmission.maxRetries);
        result.delay = delay.generateDelay(randomNumberGenerator, 0); // speed of node is only relevant for not-supported GammaSpeedDelay
        result.numberOfHops += 1;
        return result;
    }

    /**
     * Method to be implemented by extensions of {@link AdhocTransmissionModel}, calculating transmissions using topocast.
     *
     * @param senderName            The sender of the transmission.
     * @param receivers             The receivers of the transmission.
     * @param transmissionParameter Data class holding the maximumTtl, the {@link Delay} and the current map of simulated entities
     * @param currentNodes          a reference to all currently online nodes
     * @return Map of the receivers and their transmission results.
     */
    public abstract Map<String, TransmissionResult> simulateTopocast(
            String senderName, Map<String, SimulationNode> receivers,
            TransmissionParameter transmissionParameter, Map<String, SimulationNode> currentNodes
    );

    /**
     * Method to be implemented by extensions of {@link AdhocTransmissionModel}, calculating transmissions using geocast.
     *
     * @param senderName            The sender of the transmission.
     * @param receivers             The receivers of the transmission.
     * @param transmissionParameter Data class holding the maximumTtl, the {@link Delay} and the current map of simulated entities
     * @param currentNodes          a reference to all currently online nodes
     * @return Map of the receivers and their transmission results.
     */
    public abstract Map<String, TransmissionResult> simulateGeocast(
            String senderName, Map<String, SimulationNode> receivers,
            TransmissionParameter transmissionParameter, Map<String, SimulationNode> currentNodes
    );
}
