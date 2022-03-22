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

package org.eclipse.mosaic.fed.mapping.ambassador;

import org.eclipse.mosaic.fed.mapping.config.CPrototype;
import org.eclipse.mosaic.fed.mapping.config.units.COriginDestinationMatrixMapper;
import org.eclipse.mosaic.fed.mapping.config.units.CVehicle;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;

import java.util.List;

/**
 * See the {@link COriginDestinationMatrixMapper} for detailed description of the class and its fields.
 * This class is used to generate a stream of vehicles between an origin- and a destination-point.
 */
public class OriginDestinationVehicleFlowGenerator {
    private final List<COriginDestinationMatrixMapper.COriginDestinationPoint> originDestinationPointConfigurations;
    private final List<CPrototype> prototypeConfigurations;
    private final Boolean deterministic;

    /**
     * Values for the OD-matrix. Unit should be vehicles/hour.
     */
    private final List<List<Double>> odValues;
    private final VehicleDeparture.LaneSelectionMode laneSelectionMode;
    private final VehicleDeparture.DepartureSpeedMode departureSpeedMode;
    private final double startingTime;
    private final Double maxTime;

    /**
     * Constructor for {@link OriginDestinationVehicleFlowGenerator}.
     *
     * @param matrixMapperConfiguration the configuration of the matrix mapper set in the json configuration
     */
    OriginDestinationVehicleFlowGenerator(COriginDestinationMatrixMapper matrixMapperConfiguration) {
        this.originDestinationPointConfigurations = matrixMapperConfiguration.points;
        this.prototypeConfigurations = matrixMapperConfiguration.types;
        this.deterministic = matrixMapperConfiguration.deterministic;
        this.odValues = matrixMapperConfiguration.odValues;
        this.startingTime = matrixMapperConfiguration.startingTime;
        this.maxTime = matrixMapperConfiguration.maxTime;
        this.laneSelectionMode = matrixMapperConfiguration.laneSelectionMode;
        this.departureSpeedMode = matrixMapperConfiguration.departSpeedMode;
    }

    /**
     * Generates the vehicle streams using the origin and destination pairs.
     *
     * @param framework             the framework handling the stream generation
     * @param randomNumberGenerator {@link RandomNumberGenerator} used for flow noise
     * @param flowNoise             flag if the stream should be affected by noise
     */
    void generateVehicleStreams(SpawningFramework framework, RandomNumberGenerator randomNumberGenerator, boolean flowNoise) {
        for (int fromId = 0; fromId < originDestinationPointConfigurations.size(); fromId++) {
            for (int toId = 0; toId < originDestinationPointConfigurations.size(); toId++) {

                double flow = odValues.get(fromId).get(toId);
                if (flow <= 0) {
                    continue;
                }

                CVehicle vehicleConfiguration = new CVehicle();
                vehicleConfiguration.origin = originDestinationPointConfigurations.get(fromId).position;
                vehicleConfiguration.destination = originDestinationPointConfigurations.get(toId).position;
                vehicleConfiguration.targetFlow = flow;
                vehicleConfiguration.types = prototypeConfigurations;
                vehicleConfiguration.deterministic = deterministic;

                vehicleConfiguration.startingTime = startingTime;
                vehicleConfiguration.maxTime = maxTime;
                vehicleConfiguration.laneSelectionMode = laneSelectionMode;
                vehicleConfiguration.departSpeedMode = departureSpeedMode;
                // Assuming, if we only have set a flow, that we want to have an unlimited number of vehicles

                framework.addVehicleStream(new VehicleFlowGenerator(vehicleConfiguration, randomNumberGenerator, flowNoise));
            }
        }
    }
}
