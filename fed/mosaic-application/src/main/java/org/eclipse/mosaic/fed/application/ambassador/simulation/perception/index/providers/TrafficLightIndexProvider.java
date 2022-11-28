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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.SpatialIndexProvider;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.TrafficLightObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.util.TrafficLightIndexProviderTypeAdapterFactory;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;
import java.util.Map;

@JsonAdapter(TrafficLightIndexProviderTypeAdapterFactory.class)
public interface TrafficLightIndexProvider {
    /**
     * Method called to initialize index after configuration has been read.
     */
    void initialize();

    /**
     * Queries the {@link SpatialIndexProvider} and returns all traffic lights inside the {@link PerceptionModel}.
     */
    List<TrafficLightObject> getTrafficLightsInRange(PerceptionModel perceptionModel);

    /**
     * Adds traffic lights to the spatial index, as their positions are static it is sufficient
     * to store positional information only once.
     *
     * @param trafficLightGroup the registration interaction
     */
    void addTrafficLight(TrafficLightGroup trafficLightGroup);

    /**
     * Updates the {@link SpatialIndexProvider} in regard to traffic lights. The unit simulator has to be queried as
     * {@code TrafficLightUpdates} do not contain all necessary information.
     *
     * @param trafficLightGroupsToUpdate a list of information packages transmitted by the traffic simulator
     */
    void updateTrafficLights(Map<String, TrafficLightGroupInfo> trafficLightGroupsToUpdate);


    /**
     * Returns the number of TLs in the simulation.
     *
     * @return the number of TLs
     */
    int getNumberOfTrafficLights();
}
