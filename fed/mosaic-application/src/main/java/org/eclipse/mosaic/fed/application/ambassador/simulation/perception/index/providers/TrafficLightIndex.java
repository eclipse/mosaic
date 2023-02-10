/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.TrafficObjectIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.TrafficLightObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.util.TrafficLightIndexTypeAdapterFactory;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;

import com.google.gson.annotations.JsonAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonAdapter(TrafficLightIndexTypeAdapterFactory.class)
public abstract class TrafficLightIndex {

    /**
     * Stores {@link TrafficLightObject}s for fast removal and position update.
     */
    final Map<String, TrafficLightObject> indexedTrafficLights = new HashMap<>();

    /**
     * Returns the number of TLs in the simulation.
     *
     * @return the number of TLs
     */
    public int getNumberOfTrafficLights() {
        return indexedTrafficLights.size();
    }

    /**
     * Method called to initialize index after configuration has been read.
     */
    public abstract void initialize();

    /**
     * Queries the {@link TrafficObjectIndex} and returns all traffic lights inside the {@link PerceptionModel}.
     */
    public abstract List<TrafficLightObject> getTrafficLightsInRange(PerceptionModel perceptionModel);

    /**
     * Abstract method to be implemented by the specific traffic light indexes.
     * Shall contain functionality to be called before traffic lights are updated.
     */
    public abstract void onTrafficLightsUpdate();

    /**
     * Adds traffic lights to the spatial index, as their positions are static it is sufficient
     * to store positional information only once.
     *
     * @param trafficLightGroup the registration interaction
     */
    public void addTrafficLight(TrafficLightGroup trafficLightGroup) {
        String trafficLightGroupId = trafficLightGroup.getGroupId();
        trafficLightGroup.getTrafficLights().forEach(
                (trafficLight) -> {
                    String trafficLightId = calculateTrafficLightId(trafficLightGroupId, trafficLight.getId());
                    if (SimulationKernel.SimulationKernel.getCentralPerceptionComponent().getScenarioBounds()
                            .contains(trafficLight.getPosition().toCartesian())) { // check if inside bounding area
                        indexedTrafficLights.computeIfAbsent(trafficLightId, TrafficLightObject::new)
                                .setTrafficLightGroupId(trafficLightGroupId)
                                .setPosition(trafficLight.getPosition().toCartesian())
                                .setIncomingLane(trafficLight.getIncomingLane())
                                .setOutgoingLane(trafficLight.getOutgoingLane())
                                .setTrafficLightState(trafficLight.getCurrentState());
                    }
                }
        );
    }

    /**
     * Updates the {@link TrafficObjectIndex} in regard to traffic lights. The unit simulator has to be queried as
     * {@code TrafficLightUpdates} do not contain all necessary information.
     *
     * @param trafficLightGroupsToUpdate a list of information packages transmitted by the traffic simulator
     */
    public void updateTrafficLights(Map<String, TrafficLightGroupInfo> trafficLightGroupsToUpdate) {
        onTrafficLightsUpdate();
        trafficLightGroupsToUpdate.forEach(
                (trafficLightGroupId, trafficLightGroupInfo) -> {
                    List<TrafficLightState> trafficLightStates = trafficLightGroupInfo.getCurrentState();
                    for (int i = 0; i < trafficLightStates.size(); i++) {
                        String trafficLightId = calculateTrafficLightId(trafficLightGroupId, i);
                        final TrafficLightState trafficLightState = trafficLightStates.get(i);
                        indexedTrafficLights.computeIfPresent(trafficLightId, (id, trafficLightObject)
                                -> trafficLightObject.setTrafficLightState(trafficLightState));
                    }
                }
        );
    }

    private String calculateTrafficLightId(String trafficLightGroupId, int trafficLightIndex) {
        return trafficLightGroupId + "_" + trafficLightIndex;
    }
}
