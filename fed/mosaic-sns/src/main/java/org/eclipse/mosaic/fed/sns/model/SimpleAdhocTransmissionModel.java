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
 */

package org.eclipse.mosaic.fed.sns.model;

import org.eclipse.mosaic.fed.sns.ambassador.SimulationNode;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.delay.ConstantDelay;
import org.eclipse.mosaic.lib.model.delay.Delay;
import org.eclipse.mosaic.lib.model.gson.DelayTypeAdapterFactory;
import org.eclipse.mosaic.lib.model.transmission.CTransmission;
import org.eclipse.mosaic.lib.model.transmission.TransmissionResult;

import com.google.gson.annotations.JsonAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link SimpleAdhocTransmissionModel} assumes, that all receivers can be reached even though this might not be the
 * case in reality. Note: you should use a {@link Delay} that realistically approximates these transmissions, generally
 * this means the baseline delay should be increased.
 */
public class SimpleAdhocTransmissionModel extends AdhocTransmissionModel {

    /**
     * Delay to be used for a simple Multihop.
     */
    @JsonAdapter(DelayTypeAdapterFactory.class)
    public Delay simpleMultihopDelay = new ConstantDelay();

    /**
     * Transmission parameters to be used for a simple Multihop.
     */
    public CTransmission simpleMultihopTransmission = new CTransmission();

    /**
     * Simulates a direct transmission between the sender and receivers. Note: If a single addressed receiver is used
     * the 'receivers' map will only contain one entry as well as the results.
     *
     * @param senderName            The sender of the transmission.
     * @param receivers             The receivers of the transmission.
     * @param transmissionParameter Data class holding the maximumTtl, the {@link Delay} and the current map of simulated entities
     * @param currentNodes          a reference to all currently online nodes
     * @return Map of the receivers and their transmission results.
     */
    @Override
    public Map<String, TransmissionResult> simulateTopocast(
            String senderName, Map<String, SimulationNode> receivers,
            TransmissionParameter transmissionParameter, Map<String, SimulationNode> currentNodes) {
        return calculateTransmissions(
                transmissionParameter.randomNumberGenerator,
                receivers, transmissionParameter.delay, transmissionParameter.transmission
        );
    }

    /**
     * For this simple model Geocast function the same way that Topocasts do, with the only difference being that a
     * approximating {@link Delay} is used.
     *
     * @param senderName            The sender of the transmission.
     * @param receivers             The receivers of the transmission.
     * @param transmissionParameter Data class holding the maximumTtl, the {@link Delay} and the current map of simulated entities
     * @param currentNodes          a reference to all currently online nodes
     * @return Map of the receivers and their transmission results.
     */
    @Override
    public Map<String, TransmissionResult> simulateGeocast(
            String senderName, Map<String, SimulationNode> receivers,
            TransmissionParameter transmissionParameter, Map<String, SimulationNode> currentNodes) {
        return calculateTransmissions(
                transmissionParameter.randomNumberGenerator, receivers, simpleMultihopDelay, simpleMultihopTransmission
        );
    }

    /**
     * This is a helper method to avoid duplicated code, it takes transmission parameters from
     * the {@link #simulateGeocast} and {@link #simulateTopocast} methods and calculates the
     * {@link TransmissionResult}s for all receivers.
     *
     * @param rng       {@link RandomNumberGenerator} for the evaluation of delays and transmission success
     * @param receivers map containing all receivers
     * @param delay     the delay specification from the configuration
     * @return a map containing the {@link TransmissionResult}s for all receivers
     */
    private Map<String, TransmissionResult> calculateTransmissions(
            RandomNumberGenerator rng, Map<String, SimulationNode> receivers, Delay delay, CTransmission transmission) {
        Map<String, TransmissionResult> results = new HashMap<>();
        receivers.forEach((receiverName, receiver) -> results
                .put(receiverName, simulateTransmission(rng, delay, transmission)));
        return results;
    }
}
