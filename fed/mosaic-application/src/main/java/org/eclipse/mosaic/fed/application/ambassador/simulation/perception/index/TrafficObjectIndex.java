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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.TrafficLightIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleIndex;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link TrafficObjectIndex} is a representation of space using a special data structure.
 * The goal is to allow for efficient querying of nearby entities.
 */
public class TrafficObjectIndex {
    private final Logger log;
    private final VehicleIndex vehicleIndex;
    private final TrafficLightIndex trafficLightIndex;

    private TrafficObjectIndex(Logger log, VehicleIndex vehicleIndex, TrafficLightIndex trafficLightIndex) {
        this.log = log;
        this.vehicleIndex = vehicleIndex;
        this.trafficLightIndex = trafficLightIndex;
        if (vehicleIndex != null) {
            vehicleIndex.initialize();
        }
        if (trafficLightIndex != null) {
            trafficLightIndex.initialize();
        }
    }

    protected TrafficObjectIndex(TrafficObjectIndex parent) {
        this(parent.log, parent.vehicleIndex, parent.trafficLightIndex);
    }

    private boolean vehicleIndexProviderConfigured() {
        return vehicleIndex != null;
    }

    private boolean trafficLightIndexProviderConfigured() {
        return trafficLightIndex != null;
    }

    /**
     * Queries the {@link TrafficObjectIndex} and returns all vehicles inside the {@link PerceptionModel}.
     */
    public List<VehicleObject> getVehiclesInRange(PerceptionModel perceptionModel) {
        if (vehicleIndexProviderConfigured()) {
            return vehicleIndex.getVehiclesInRange(perceptionModel);
        } else {
            log.debug("No Traffic Light Index Provider configured. No Vehicles will be in range.");
            return new ArrayList<>();
        }
    }

    /**
     * Adds a vehicle to the {@link TrafficObjectIndex}.
     *
     * @param vehicleRegistration The interaction containing information about the spawned vehicles
     */
    public void addVehicle(VehicleRegistration vehicleRegistration) {
        if (vehicleIndexProviderConfigured()) {
            vehicleIndex.addVehicle(vehicleRegistration);
        } else {
            log.debug("No Vehicle Index Provider configured. No Vehicle will be added.");
        }
    }

    /**
     * Remove all vehicles from the {@link TrafficObjectIndex} by a list of vehicle ids.
     *
     * @param vehiclesToRemove the list of vehicles to remove from the index
     */
    public void removeVehicles(Iterable<String> vehiclesToRemove) {
        if (vehicleIndexProviderConfigured()) {
            vehicleIndex.removeVehicles(vehiclesToRemove);
        } else {
            log.debug("No Vehicle Index Provider configured. No Vehicle will be removed.");
        }
    }

    /**
     * Updates the {@link TrafficObjectIndex} with a list of {@link VehicleData} objects.
     *
     * @param vehiclesToUpdate the list of vehicles to add or update in the index
     */
    public void updateVehicles(Iterable<VehicleData> vehiclesToUpdate) {
        if (vehicleIndexProviderConfigured()) {
            vehicleIndex.updateVehicles(vehiclesToUpdate);
        } else {
            log.debug("No Vehicle Index Provider configured. Index won't be updated.");
        }
    }

    /**
     * Returns the amount of indexed vehicles.
     *
     * @return the number of vehicles
     */
    public int getNumberOfVehicles() {
        if (vehicleIndexProviderConfigured()) {
            return vehicleIndex.getNumberOfVehicles();
        } else {
            log.debug("No Vehicle Index Provider configured. There are no indexed Vehicles.");
            return 0;
        }
    }

    /**
     * Queries the {@link TrafficObjectIndex} and returns all traffic lights inside the {@link PerceptionModel}.
     */
    public List<TrafficLightObject> getTrafficLightsInRange(PerceptionModel perceptionModel) {
        if (trafficLightIndexProviderConfigured()) {
            return trafficLightIndex.getTrafficLightsInRange(perceptionModel);
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
            trafficLightIndex.addTrafficLight(trafficLightGroup);
        } else {
            log.debug("No Traffic Light Index Provider configured. Traffic Light won't be added.");
        }
    }

    /**
     * Updates the {@link TrafficObjectIndex} in regard to traffic lights. The unit simulator has to be queried as
     * {@code TrafficLightUpdates} do not contain all necessary information.
     *
     * @param trafficLightsToUpdate a list of information packages transmitted by the traffic simulator
     */
    public void updateTrafficLights(Map<String, TrafficLightGroupInfo> trafficLightsToUpdate) {
        if (trafficLightIndexProviderConfigured()) {
            trafficLightIndex.updateTrafficLights(trafficLightsToUpdate);
        } else {
            log.debug("No Traffic Light Index Provider configured. Index won't be updated.");
        }
    }

    /**
     * Returns the number of TLs in the simulation.
     *
     * @return the number of TLs
     */
    public int getNumberOfTrafficLights() {
        if (trafficLightIndexProviderConfigured()) {
            return trafficLightIndex.getNumberOfTrafficLights();
        } else {
            log.debug("No Traffic Light Index Provider configured. There are no indexed Traffic Lights.");
            return 0;
        }
    }

    public static class Builder {
        private final Logger log;
        private VehicleIndex vehicleIndex = null;
        private TrafficLightIndex trafficLightIndex = null;

        public Builder(Logger log) {
            this.log = log;
        }

        public Builder withVehicleIndex(VehicleIndex vehicleIndex) {
            this.vehicleIndex = vehicleIndex;
            return this;
        }

        public Builder withTrafficLightIndex(TrafficLightIndex trafficLightIndex) {
            this.trafficLightIndex = trafficLightIndex;
            return this;
        }

        public TrafficObjectIndex build() {
            return new TrafficObjectIndex(log, vehicleIndex, trafficLightIndex);
        }
    }
}