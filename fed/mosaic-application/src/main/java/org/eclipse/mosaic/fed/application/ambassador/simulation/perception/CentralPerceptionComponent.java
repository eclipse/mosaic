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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.lib.PerceptionGrid;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.lib.PerceptionIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.lib.SpatialIndex;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.routing.database.DatabaseRouting;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CentralPerceptionComponent} is responsible for keeping a spatial index of all vehicles,
 * which allows fast querying of nearby vehicles.
 */
public class CentralPerceptionComponent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CApplicationAmbassador.CPerception configuration;

    /**
     * Representation of the Map, allowing for faster query times.
     */
    private SpatialIndex spatialIndex;

    private VehicleUpdates latestUpdates;

    private long nextUpdate = 0;

    public CentralPerceptionComponent(CApplicationAmbassador.CPerception perceptionConfiguration) {
        this.configuration = perceptionConfiguration;
    }

    public void initialize() throws InternalFederateException {
        try {
            if (configuration == null) {
                log.info("No Perception-Configuration was defined. Calling related API will result in errors.");
                return;
            }
            setSpatialIndex();
        } catch (Exception e) {
            throw new InternalFederateException("Couldn't initialize CentralPerceptionComponent");
        }
    }

    private void setSpatialIndex() {
        Database db = ((DatabaseRouting) SimulationKernel.SimulationKernel.getCentralNavigationComponent().getRouting()).getScenarioDatabase();
        CartesianRectangle scenarioBounds = db.getBoundingBox().toCartesian();
        switch (configuration.perceptionBackend) {
            case Grid:
                spatialIndex = new PerceptionGrid(configuration.gridCellWidth, configuration.gridCellHeight, scenarioBounds);
                break;
            case QuadTree:
            case Trivial:
            default:
                spatialIndex = new PerceptionIndex();
        }
    }

    public SpatialIndex getSpatialIndex() {
        return spatialIndex;
    }

    public void updateSpatialIndices() {
        if (SimulationKernel.SimulationKernel.getCurrentSimulationTime() > nextUpdate) {
            spatialIndex.update(latestUpdates);
            nextUpdate += configuration.spatialIndexUpdateInterval;
        }
    }

    public void updateVehicles(VehicleUpdates vehicleUpdates) {
        latestUpdates = vehicleUpdates;
    }
}
