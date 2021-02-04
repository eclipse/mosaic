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
import org.eclipse.mosaic.fed.sns.ambassador.TransmissionSimulator;
import org.eclipse.mosaic.lib.geo.CartesianArea;
import org.eclipse.mosaic.lib.geo.CartesianCircle;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.misc.Tuple;
import org.eclipse.mosaic.lib.model.delay.Delay;
import org.eclipse.mosaic.lib.model.transmission.TransmissionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SophisticatedAdhocTransmissionModel extends AdhocTransmissionModel {

    private final static Logger log = LoggerFactory.getLogger(SimpleAdhocTransmissionModel.class);

    @Override
    public Map<String, TransmissionResult> simulateTopocast(String senderName, Map<String, SimulationNode> receivers,
                                                            TransmissionParameter transmissionParameter, Map<String, SimulationNode> currentNodes) {
        Map<String, TransmissionResult> results = new HashMap<>();
        receivers.forEach((receiverName, receiver) -> results
                .put(receiverName, simulateTransmission(
                        transmissionParameter.randomNumberGenerator, transmissionParameter.delay, transmissionParameter.transmission)
                ));
        return results;
    }

    @Override
    public Map<String, TransmissionResult> simulateGeocast(
            String senderName, Map<String, SimulationNode> receivers,
            TransmissionParameter transmissionParameter, Map<String, SimulationNode> currentNodes) {
        Map<String, TransmissionResult> results;
        // sender in destination area or can reach unit in destination area (flooding)
        if (canReachEntityInDestinationArea(senderName, receivers, currentNodes)) {
            receivers.remove(senderName); // sender should never receive its own message
            results = flooding(senderName, receivers, transmissionParameter, currentNodes);
        } else { // sender outside destination area (forwarding than flooding)
            Tuple<String, TransmissionResult> nodeInsideDestinationArea =
                    forwarding(senderName, receivers, transmissionParameter, currentNodes);
            if (nodeInsideDestinationArea == null) { // if no node has been reached while forwarding GeoArea, set all results to failed
                Map<String, TransmissionResult> unsuccessfulForwardAndFlood = new HashMap<>();
                receivers.forEach((receiverName, receiver) ->
                        unsuccessfulForwardAndFlood.put(receiverName, new TransmissionResult(false)));
                results = unsuccessfulForwardAndFlood;
                log.info("Greedy Forwarding to destination area failed");
            } else {
                String newSenderName = nodeInsideDestinationArea.getA(); // get name of node that was reached using greedy forwarding
                double forwardingDelay = nodeInsideDestinationArea.getB().delay; // get delay from node that was reached
                int forwardingNumberOfHops = nodeInsideDestinationArea.getB().numberOfHops; // get number of hops of node that was reached
                transmissionParameter.ttl -= forwardingNumberOfHops; // subtract number of hops from forwarding
                results = flooding(newSenderName, receivers, transmissionParameter, currentNodes);
                // add delay that was accumulated during greedy forwarding to all nodes
                results.forEach((receiverName, transmissionResult) -> {
                    transmissionResult.delay += forwardingDelay;
                    transmissionResult.numberOfHops += forwardingNumberOfHops;
                });
            }
        }

        return results;
    }

    /**
     * The Flood Transmission simulates a flooding approach to using multihop messages.
     * A vehicle sends messages to every vehicle in range which in turn relay the message to all vehicles in their range.
     * This algorithm is result orientated meaning that the the actual transmissions are not simulated.
     *
     * <pre>
     * The transmission is also instant, the delay for the single steps get added at the end;
     * it is not possible that a car which was initially in range of another car moves out of it during the transmission or such.
     * </pre>
     *
     * @param senderName            The Sender of the transmission.
     * @param receivers             The receivers of the transmission.
     * @param transmissionParameter Data class holding the maximumTtl, the {@link Delay} and the current map of simulated entities
     * @param currentNodes          a reference to all currently online nodes
     * @return List of the transmission results.
     */
    private Map<String, TransmissionResult> flooding(
            String senderName, Map<String, SimulationNode> receivers,
            TransmissionParameter transmissionParameter, Map<String, SimulationNode> currentNodes) {
        Map<String, TransmissionResult> results = new HashMap<>();
        receivers.forEach((receiverName, receiver) -> results.put(receiverName, new TransmissionResult(false, 0)));

        // this map is used to represent all entities, that will be flooding
        Map<String, SimulationNode> floodingEntities = new HashMap<>();
        floodingEntities.put(senderName, currentNodes.get(senderName));

        // in the beginning this reflects all receivers except the sender
        Map<String, SimulationNode> receiversUnsatisfied = new HashMap<>(receivers);

        // this map holds all entities, that can be reached with a single hop
        Map<String, SimulationNode> entitiesInReach;

        int currentDepth = 0;
        while (!receiversUnsatisfied.isEmpty() && currentDepth < transmissionParameter.ttl) {
            ++currentDepth;

            // this map reflects all future sender after a flooding step is completed
            Map<String, SimulationNode> foundAndSuccessfulTransmission = new HashMap<>();

            // do this for all of the currently sending entities
            for (Map.Entry<String, SimulationNode> floodingEntityEntry : floodingEntities.entrySet()) {
                CartesianArea singleHopReachArea = new CartesianCircle(
                        floodingEntityEntry.getValue().getPosition(),
                        floodingEntityEntry.getValue().getRadius()
                );
                // only search for unsatisfied receivers
                entitiesInReach = TransmissionSimulator.getEntitiesInArea(receiversUnsatisfied, singleHopReachArea);

                // simulate transmission for unsatisfied receivers in reach

                Map<String, TransmissionResult> transmissionResults = new HashMap<>();
                for (Map.Entry<String, SimulationNode> entry : entitiesInReach.entrySet()) {
                    transmissionResults.put(
                            entry.getKey(),
                            simulateTransmission(
                                    transmissionParameter.randomNumberGenerator,
                                    transmissionParameter.delay, transmissionParameter.transmission
                            )
                    );
                }

                int previousNumberOfHops = floodingEntityEntry.getKey().equals(senderName)
                        ? 0 : results.get(floodingEntityEntry.getKey()).numberOfHops; // determine previous numberOfHops
                long previousDelay = floodingEntityEntry.getKey().equals(senderName)
                        ? 0 : results.get(floodingEntityEntry.getKey()).delay; // determine previous delay
                transmissionResults.forEach((receiverName, transmissionResult) -> {
                    // if entity hasn't been satisfied yet use it as new sender
                    if (transmissionResult.success && receiversUnsatisfied.remove(receiverName) != null) {
                        transmissionResult.numberOfHops = previousNumberOfHops + 1;
                        transmissionResult.delay += previousDelay;
                        foundAndSuccessfulTransmission.put(receiverName, receivers.get(receiverName));
                        results.put(receiverName, transmissionResult);
                    }
                });
            }
            floodingEntities.clear(); // reset flooding entities
            floodingEntities.putAll(foundAndSuccessfulTransmission); // new entities which will be used as start nodes
        }
        return results;
    }

    /**
     * This method handles the logic of forwarding a message to an area. The used approach is greedy and won't always
     * result in a successful transmission. The method builds a sort of "pipeline" to reach a node in the receivers-map. This is
     * done by collecting all reachable entities of the current entity and selecting the one, that is closest to one in the receivers-
     * map. This process is repeated for the selected entity, until either a receiver-entity is reached, a single transmission failed
     * or the TTL is exceeded.
     * For further explanation view the documentation.
     *
     * @param senderName            The name of the sender of the transmission.
     * @param receivers             The receivers of the transmission.
     * @param transmissionParameter Data class holding the maximumTtl, the {@link Delay} and the current map of simulated entities
     * @param currentNodes          a reference to all currently online nodes
     * @return Map containing the first entity reached out of the receivers map
     */
    private Tuple<String, TransmissionResult> forwarding(
            String senderName, Map<String, SimulationNode> receivers,
            TransmissionParameter transmissionParameter, Map<String, SimulationNode> currentNodes) {

        String currentEntityName = senderName;
        SimulationNode currentEntity;
        TransmissionResult previousTransmissionResult = new TransmissionResult(true, 0);
        int currentDepth = 0;
        while (currentDepth < transmissionParameter.ttl) {
            ++currentDepth;

            currentEntity = currentNodes.get(currentEntityName);
            CartesianCircle singleHopReach = new CartesianCircle(
                    currentEntity.getPosition(),
                    currentEntity.getRadius()
            );
            // get all reachable entities within singlehop range and remove sender
            Map<String, SimulationNode> reachableEntities =
                    TransmissionSimulator.getEntitiesInArea(currentNodes, singleHopReach);
            reachableEntities.remove(senderName);
            // try to find entity to build "pipeline" to destination area
            String forwardingEntityName = getForwardingEntity(reachableEntities, receivers);
            if (forwardingEntityName == null) { // if no entity to forward the message to was found, Forwarding fails
                return null;
            }
            // simulate the transmission to the forwarding entity
            TransmissionResult transmissionResult =
                    simulateTransmission(transmissionParameter.randomNumberGenerator,
                            transmissionParameter.delay, transmissionParameter.transmission
                    );
            if (!transmissionResult.success) { // whenever the transmission on the way to the destination area fails, everything fails
                return null;
            }
            transmissionResult.numberOfHops = previousTransmissionResult.numberOfHops + 1;
            transmissionResult.delay += previousTransmissionResult.delay; // sum delays on the way

            // if an entity in the destination-area was found return it with the accumulated TransmissionResult
            // (this is especially relevant for the delay, which will be added to the other delays)
            if (receivers.containsKey(forwardingEntityName)) {
                return new Tuple<>(forwardingEntityName, transmissionResult);
            }

            previousTransmissionResult = transmissionResult;
            currentEntityName = forwardingEntityName;
        }
        // if destination area couldn't be reached in ttl, fail
        return null;

    }

    /**
     * This helper method evaluates if a GeoCast can be executed without forwarding the
     * destination area in any form. This boils down to checking if the communication-radius
     * of the sender is large enough to reach one of the entities in the destination area.
     *
     * @return {@code true} if node in destination area can be reached, {@code false} otherwise
     */
    private boolean canReachEntityInDestinationArea(String senderName,
                                                    Map<String, SimulationNode> possibleReceivers,
                                                    Map<String, SimulationNode> currentNodes) {
        SimulationNode sender = currentNodes.get(senderName);
        CartesianPoint senderPosition = sender.getPosition();
        double senderRadius = sender.getRadius();
        for (SimulationNode receiver : possibleReceivers.values()) {
            if (senderPosition.distanceTo(receiver.getPosition()) <= senderRadius) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method tries to find an entity, which is closest to a node in the destination area.
     *
     * @param reachableEntities a map of all entities reachable within the communication range
     * @param receivers         a map of all entities in the destination area
     * @return the name of the found entity
     */
    private String getForwardingEntity(Map<String, SimulationNode> reachableEntities,
                                       Map<String, SimulationNode> receivers) {
        double currentDistance = Double.MAX_VALUE;
        double candidateDistance;
        String currentEntityName = null;
        for (Map.Entry<String, SimulationNode> reachableEntityEntry : reachableEntities.entrySet()) {
            for (Map.Entry<String, SimulationNode> receiverEntry : receivers.entrySet()) {
                candidateDistance = reachableEntityEntry.getValue().getPosition().distanceTo(receiverEntry.getValue().getPosition());

                if (candidateDistance == 0) { // if this distance is 0 a node in the destination area has been found
                    return reachableEntityEntry.getKey();
                }
                if (candidateDistance < currentDistance) { // shorter distance
                    currentDistance = candidateDistance;
                    currentEntityName = reachableEntityEntry.getKey();
                }
            }
        }
        return currentEntityName;
    }
}
