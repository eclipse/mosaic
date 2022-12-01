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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObjectAdapter;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.TrafficLightObject;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;
import org.eclipse.mosaic.lib.spatial.KdTree;
import org.eclipse.mosaic.lib.spatial.SpatialTreeTraverser;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link TrafficLightIndexProvider} using a KD-Tree to store traffic lights.
 */
public class TrafficLightTree implements TrafficLightIndexProvider {

    @Expose
    public int bucketSize = 20;

    private KdTree<TrafficLightObject> trafficLightTree;

    private SpatialTreeTraverser.InRadius<TrafficLightObject> treeTraverser;

    private final Map<String, TrafficLightObject> indexedTrafficLights = new HashMap<>();

    @Override
    public void initialize() {
        // nothing to initialize
    }

    @Override
    public List<TrafficLightObject> getTrafficLightsInRange(PerceptionModel perceptionModel) {
        treeTraverser.setup(perceptionModel.getBoundingBox().center,
                perceptionModel.getBoundingBox().center.distanceSqrTo(perceptionModel.getBoundingBox().min)); // overestimating distance
        treeTraverser.traverse(trafficLightTree);
        return treeTraverser.getResult().stream().filter(perceptionModel::isInRange).collect(Collectors.toList());
    }

    @Override
    public void addTrafficLight(TrafficLightGroup trafficLightGroup) {
        String trafficLightGroupId = trafficLightGroup.getGroupId();
        trafficLightGroup.getTrafficLights().forEach(
                (trafficLight) -> {
                    String trafficLightId = calculateTrafficLightId(trafficLightGroupId, trafficLight.getId());
                    if (SimulationKernel.SimulationKernel.getCentralPerceptionComponentComponent().getScenarioBounds()
                            .contains(trafficLight.getPosition().toCartesian())) {
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

    @Override
    public void updateTrafficLights(Map<String, TrafficLightGroupInfo> trafficLightGroupsToUpdate) {
        if (trafficLightTree == null) {
            List<TrafficLightObject> allTrafficLights = new ArrayList<>(indexedTrafficLights.values());
            trafficLightTree = new KdTree<>(new SpatialObjectAdapter<>(), allTrafficLights, bucketSize);
            treeTraverser = new SpatialTreeTraverser.InRadius<>();
        }
        trafficLightGroupsToUpdate.forEach(
                (trafficLightGroupId, trafficLightGroupInfo) -> {
                    List<TrafficLightState> trafficLightStates = trafficLightGroupInfo.getCurrentState();
                    for (int i = 0; i < trafficLightStates.size(); i++) { // check if inside bounding area
                        String trafficLightId = calculateTrafficLightId(trafficLightGroupId, i);
                        final TrafficLightState trafficLightState = trafficLightStates.get(i);
                        indexedTrafficLights.computeIfPresent(trafficLightId, (id, trafficLightObject)
                                -> trafficLightObject.setTrafficLightState(trafficLightState));
                    }
                }
        );
    }

    @Override
    public int getNumberOfTrafficLights() {
        return trafficLightTree.getRoot().size();
    }

    private String calculateTrafficLightId(String trafficLightGroupId, int trafficLightIndex) {
        return trafficLightGroupId + "_" + trafficLightIndex;
    }
}
