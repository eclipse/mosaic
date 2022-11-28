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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.TrafficLightObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.TrafficLightIndexProvider;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleIndexProvider;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link SpatialIndexProvider} is a representation of space using a special data structure.
 * The goal is to allow for efficient querying of nearby entities.
 */
public class SpatialIndexProvider implements SpatialIndex {
    private final Logger log;
    private final VehicleIndexProvider vehicleIndexProvider;
    private final TrafficLightIndexProvider trafficLightIndexProvider;

    private SpatialIndexProvider(Logger log, VehicleIndexProvider vehicleIndexProvider, TrafficLightIndexProvider trafficLightIndexProvider) {
        this.log = log;
        this.vehicleIndexProvider = vehicleIndexProvider;
        this.trafficLightIndexProvider = trafficLightIndexProvider;
    }

    private boolean vehicleIndexProviderConfigured() {
        return vehicleIndexProvider != null;
    }

    private boolean trafficLightIndexProviderConfigured() {
        return trafficLightIndexProvider != null;
    }

    /**
     * Queries the {@link SpatialIndexProvider} and returns all vehicles inside the {@link PerceptionModel}.
     */
    @Override
    public List<VehicleObject> getVehiclesInRange(PerceptionModel perceptionModel) {
        if (vehicleIndexProviderConfigured()) {
            return vehicleIndexProvider.getVehiclesInRange(perceptionModel);
        } else {
            log.debug("No Traffic Light Index Provider configured. No Vehicles will be in range.");
            return new ArrayList<>();
        }
    }

    /**
     * Remove all vehicles from the {@link SpatialIndexProvider} by a list of vehicle ids.
     *
     * @param vehiclesToRemove the list of vehicles to remove from the index
     */
    @Override
    public void removeVehicles(Iterable<String> vehiclesToRemove) {
        if (vehicleIndexProviderConfigured()) {
            vehicleIndexProvider.removeVehicles(vehiclesToRemove);
        } else {
            log.debug("No Vehicle Index Provider configured. No Vehicle will be removed.");
        }
    }

    /**
     * Updates the {@link SpatialIndexProvider} with a list of {@link VehicleData} objects.
     *
     * @param vehiclesToUpdate the list of vehicles to add or update in the index
     */
    @Override
    public void updateVehicles(Iterable<VehicleData> vehiclesToUpdate) {
        if (vehicleIndexProviderConfigured()) {
            vehicleIndexProvider.updateVehicles(vehiclesToUpdate);
        } else {
            log.debug("No Vehicle Index Provider configured. Index won't be updated.");
        }
    }

    /**
     * Returns the amount of indexed vehicles.
     *
     * @return the number of vehicles
     */
    @Override
    public int getNumberOfVehicles() {
        if (vehicleIndexProviderConfigured()) {
            return vehicleIndexProvider.getNumberOfVehicles();
        } else {
            log.debug("No Vehicle Index Provider configured. There are no indexed Vehicles.");
            return 0;
        }
    }

    /**
     * Queries the {@link SpatialIndexProvider} and returns all traffic lights inside the {@link PerceptionModel}.
     */
    @Override
    public List<TrafficLightObject> getTrafficLightsInRange(PerceptionModel perceptionModel) {
        if (trafficLightIndexProviderConfigured()) {
            return trafficLightIndexProvider.getTrafficLightsInRange(perceptionModel);
        } else {
            log.debug("No Traffic Light Index Provider configured. No Traffic Lights will be in range.");
            return new ArrayList<>();
        }
    }

    /**
     * Adds traffic lights to the spatial index, as their positions are static it is sufficient
     * to store positional information only once.
     *
     * @param trafficLightGroup the registered traffic light group
     */
    public void addTrafficLightGroup(TrafficLightGroup trafficLightGroup) {
        if (trafficLightIndexProviderConfigured()) {
            trafficLightIndexProvider.addTrafficLight(trafficLightGroup);
        } else {
            log.debug("No Traffic Light Index Provider configured. Traffic Light won't be added.");
        }
    }

    /**
     * Updates the {@link SpatialIndexProvider} in regard to traffic lights. The unit simulator has to be queried as
     * {@code TrafficLightUpdates} do not contain all necessary information.
     *
     * @param trafficLightsToUpdate a list of information packages transmitted by the traffic simulator
     */
    @Override
    public void updateTrafficLights(Map<String, TrafficLightGroupInfo> trafficLightsToUpdate) {
        if (trafficLightIndexProviderConfigured()) {
            trafficLightIndexProvider.updateTrafficLights(trafficLightsToUpdate);
        } else {
            log.debug("No Traffic Light Index Provider configured. Index won't be updated.");
        }
    }

    /**
     * Returns the number of TLs in the simulation.
     *
     * @return the number of TLs
     */
    @Override
    public int getNumberOfTrafficLights() {
        if (trafficLightIndexProviderConfigured()) {
            return trafficLightIndexProvider.getNumberOfTrafficLights();
        } else {
            log.debug("No Traffic Light Index Provider configured. There are no indexed Traffic Lights.");
            return 0;
        }
    }

    public static class Builder {
        private final Logger log;
        private VehicleIndexProvider vehicleIndexProvider = null;
        private TrafficLightIndexProvider trafficLightIndexProvider = null;

        public Builder(Logger log) {
            this.log = log;
        }

        public Builder withVehicleIndexProvider(VehicleIndexProvider vehicleIndexProvider) {
            this.vehicleIndexProvider = vehicleIndexProvider;
            return this;
        }

        public Builder withTrafficLightIndexProvider(TrafficLightIndexProvider trafficLightIndexProvider) {
            this.trafficLightIndexProvider = trafficLightIndexProvider;
            return this;
        }

        public SpatialIndexProvider build() {
            if (vehicleIndexProvider != null) {
                vehicleIndexProvider.initialize();
            }
            if (trafficLightIndexProvider != null) {
                trafficLightIndexProvider.initialize();
            }
            return new SpatialIndexProvider(log, vehicleIndexProvider, trafficLightIndexProvider);
        }
    }
}