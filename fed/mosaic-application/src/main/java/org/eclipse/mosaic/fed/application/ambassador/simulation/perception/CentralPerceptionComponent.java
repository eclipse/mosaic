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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.lib.PerceptionIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.lib.SpatialIndex;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * The {@link CentralPerceptionComponent} is responsible for keeping a spatial index of all vehicles,
 * which allows fast querying of nearby vehicles.
 */
public class CentralPerceptionComponent {

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
            setSpatialIndex();
        } catch (Exception e) {
            throw new InternalFederateException("Couldn't initialize CentralPerceptionComponent");
        }
    }

    private void setSpatialIndex() {
        switch (configuration.perceptionBackend) {
            case Grid:
                // TODO: these probably need additional configuration
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
