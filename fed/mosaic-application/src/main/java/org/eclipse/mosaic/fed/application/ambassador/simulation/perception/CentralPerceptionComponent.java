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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.TrafficObjectIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.TrafficLightObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.TrafficLightMap;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleMap;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.TrafficLightUpdates;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.PerformanceMonitor;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The {@link CentralPerceptionComponent} is responsible for keeping a spatial index of all vehicles,
 * which allows fast querying of nearby vehicles.
 */
public class CentralPerceptionComponent {

    private final static Logger LOG = LoggerFactory.getLogger(CentralPerceptionComponent.class);

    private CartesianRectangle scenarioBounds;
    /**
     * Configuration containing parameters for setting up the spatial indexes.
     */
    private final CApplicationAmbassador.CPerception configuration;

    /**
     * The spatial index used to store and find objects by their positions.
     */
    private TrafficObjectIndex trafficObjectIndex;

    /**
     * The last {@link VehicleUpdates} interaction which is used to update the vehicleIndex.
     */
    private VehicleUpdates latestVehicleUpdates;

    /**
     * The last {@link TrafficLightUpdates} interaction which is used to update the vehicleIndex.
     */
    private TrafficLightUpdates latestTrafficLightUpdates;

    /**
     * If set to true, the traffic light index will be updated when {@code updateSpatialIndices} is called.
     */
    private boolean updateVehicleIndex = false;

    /**
     * If set to true, the traffic light index will be updated when {@code updateSpatialIndices} is called.
     */
    private boolean updateTrafficLightIndex = false;

    public CentralPerceptionComponent(CApplicationAmbassador.CPerception perceptionConfiguration) {
        this.configuration = Validate.notNull(perceptionConfiguration, "perceptionConfiguration must not be null");
    }

    /**
     * Initializes the spatial index used for perception.
     *
     * @throws InternalFederateException if perception backend wasn't properly defined
     */
    public void initialize() throws InternalFederateException {
        try {
            // evaluate bounding box for perception
            scenarioBounds = configuration.perceptionArea == null
                    ? SimulationKernel.SimulationKernel.getCentralNavigationComponent().getRouting().getScenarioBounds()
                    : configuration.perceptionArea.toCartesian();
            // see what backends are configured
            boolean vehicleIndexConfigured = configuration.vehicleIndex != null;
            boolean trafficLightIndexConfigured = configuration.trafficLightIndex != null;

            TrafficObjectIndex.Builder indexBuilder = new TrafficObjectIndex.Builder(LOG);
            if (scenarioBounds.getArea() <= 0) {
                LOG.warn("The bounding area of the scenario could not be determined. Defaulting to low performance spatial index.");
                if (vehicleIndexConfigured) { // if configured default to map index
                    indexBuilder.withVehicleIndex(new VehicleMap());
                }
                if (trafficLightIndexConfigured) { // if configured default to map index
                    indexBuilder.withTrafficLightIndex(new TrafficLightMap());
                }
            } else {
                if (vehicleIndexConfigured) {
                    indexBuilder.withVehicleIndex(configuration.vehicleIndex);
                }
                if (trafficLightIndexConfigured) {
                    indexBuilder.withTrafficLightIndex(configuration.trafficLightIndex);
                }
            }
            trafficObjectIndex = indexBuilder.build();

            if (configuration.measurePerformance) {
                trafficObjectIndex = new MonitoringTrafficObjectIndexProvider(trafficObjectIndex, PerformanceMonitor.getInstance());
            }
        } catch (Exception e) {
            throw new InternalFederateException("Couldn't initialize CentralPerceptionComponent", e);
        }
    }

    /**
     * Returns the {@link TrafficObjectIndex} storing all vehicles.
     */
    public TrafficObjectIndex getTrafficObjectIndex() {
        return trafficObjectIndex;
    }

    public CartesianRectangle getScenarioBounds() {
        return scenarioBounds;
    }

    /**
     * Updates the spatial indices (currently only vehicles).
     * If the positions of vehicles have not changed since last call of this method, nothing is done.
     */
    public void updateSpatialIndices() {
        if (updateVehicleIndex) {
            // do not update index until next VehicleUpdates interaction is received
            updateVehicleIndex = false;
            // using Iterables.concat allows iterating over both lists subsequently without creating a new list
            trafficObjectIndex.updateVehicles(Iterables.concat(latestVehicleUpdates.getAdded(), latestVehicleUpdates.getUpdated()));
        }
        if (updateTrafficLightIndex) {
            // do not update index until next TrafficLightUpdates interaction is received
            updateVehicleIndex = false;
            // using Iterables.concat allows iterating over both lists subsequently without creating a new list
            trafficObjectIndex.updateTrafficLights(latestTrafficLightUpdates.getUpdated());
        }
    }

    /**
     * Store new vehicle registration of Vehicle.
     *
     * @param vehicleRegistration the interaction holding information about the spawnend vehicle
     */
    public void addVehicle(VehicleRegistration vehicleRegistration) {
        trafficObjectIndex.addVehicle(vehicleRegistration);
    }

    /**
     * Store new updates of all Vehicles to be used in the next update of the spatial index.
     *
     * @param vehicleUpdates the interaction holding all vehicle updates
     */
    public void updateVehicles(VehicleUpdates vehicleUpdates) {
        latestVehicleUpdates = vehicleUpdates;
        updateVehicleIndex = true;
        // we need to remove arrived vehicles in every simulation step, otherwise we could have dead vehicles in the index
        if (trafficObjectIndex.getNumberOfVehicles() > 0) {
            trafficObjectIndex.removeVehicles(vehicleUpdates.getRemovedNames());
        }
    }

    /**
     * Adds traffic lights to the spatial index, as their positions are static it is sufficient
     * to store positional information only once.
     *
     * @param trafficLightGroup the registered traffic light group interaction
     */
    public void addTrafficLightGroup(TrafficLightGroup trafficLightGroup) {
        trafficObjectIndex.addTrafficLightGroup(trafficLightGroup);
    }

    /**
     * Updates the traffic light index in regard to traffic lights. The unit simulator has to be queried as
     * {@link TrafficLightUpdates} do not contain all necessary information.
     *
     * @param trafficLightUpdates a list of information packages transmitted by the traffic simulator
     */
    public void updateTrafficLights(TrafficLightUpdates trafficLightUpdates) {
        latestTrafficLightUpdates = trafficLightUpdates;
        updateTrafficLightIndex = true;
    }

    /**
     * Wrapper class to measure atomic calls of update, search and remove of the used spatial index.
     */
    static class MonitoringTrafficObjectIndexProvider extends TrafficObjectIndex {
        private final PerformanceMonitor monitor;

        MonitoringTrafficObjectIndexProvider(TrafficObjectIndex parent, PerformanceMonitor monitor) {
            super(parent);
            this.monitor = monitor;
        }

        public List<VehicleObject> getVehiclesInRange(PerceptionModel searchRange) {
            try (PerformanceMonitor.Measurement m = monitor.start("search-vehicle")) {
                m.setProperties(getNumberOfVehicles(), SimulationKernel.SimulationKernel.getCurrentSimulationTime())
                        .restart();
                return super.getVehiclesInRange(searchRange);
            }
        }

        @Override
        public void addVehicle(VehicleRegistration vehicleRegistration) {
            super.addVehicle(vehicleRegistration);
        }

        @Override
        public void removeVehicles(Iterable<String> vehiclesToRemove) {
            try (PerformanceMonitor.Measurement m = monitor.start("remove-vehicle")) {
                m.setProperties(getNumberOfVehicles(), SimulationKernel.SimulationKernel.getCurrentSimulationTime())
                        .restart();
                super.removeVehicles(vehiclesToRemove);
            }
        }

        @Override
        public void updateVehicles(Iterable<VehicleData> vehiclesToUpdate) {
            try (PerformanceMonitor.Measurement m = monitor.start("update-vehicle")) {
                m.setProperties(getNumberOfVehicles(), SimulationKernel.SimulationKernel.getCurrentSimulationTime())
                        .restart();
                super.updateVehicles(vehiclesToUpdate);
            }
        }

        @Override
        public int getNumberOfVehicles() {
            return super.getNumberOfVehicles();
        }

        @Override
        public List<TrafficLightObject> getTrafficLightsInRange(PerceptionModel perceptionModel) {
            try (PerformanceMonitor.Measurement m = monitor.start("search-traffic-light")) {
                m.setProperties(getNumberOfTrafficLights(), SimulationKernel.SimulationKernel.getCurrentSimulationTime())
                        .restart();
                return super.getTrafficLightsInRange(perceptionModel);
            }
        }

        @Override
        public void addTrafficLightGroup(TrafficLightGroup trafficLightGroup) {
            super.addTrafficLightGroup(trafficLightGroup);
        }

        @Override
        public void updateTrafficLights(Map<String, TrafficLightGroupInfo> trafficLightsToUpdate) {
            try (PerformanceMonitor.Measurement m = monitor.start("update-traffic-light")) {
                m.setProperties(getNumberOfTrafficLights(), SimulationKernel.SimulationKernel.getCurrentSimulationTime())
                        .restart();
                super.updateTrafficLights(trafficLightsToUpdate);
            }
        }

        @Override
        public int getNumberOfTrafficLights() {
            return super.getNumberOfTrafficLights();
        }
    }
}
