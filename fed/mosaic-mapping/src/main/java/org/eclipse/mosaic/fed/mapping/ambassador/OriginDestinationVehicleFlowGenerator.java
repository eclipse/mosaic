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
 */

package org.eclipse.mosaic.fed.mapping.ambassador;

import org.eclipse.mosaic.fed.mapping.config.CPrototype;
import org.eclipse.mosaic.fed.mapping.config.units.CVehicle;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import java.util.List;

/**
 * See the {@link CVehicle.COriginDestinationMatrixMapper} for detailed description of the class and its fields.
 * This class is used to generate a stream of vehicles between an origin- and a destination-point.
 */
public class OriginDestinationVehicleFlowGenerator {
    private final List<CVehicle.COriginDestinationPoint> originDestinationPointConfigurations;
    private final List<CPrototype> prototypeConfigurations;
    private final Boolean deterministic;

    /**
     * Values for the OD-matrix. Unit should be vehicles/hour.
     */
    private final List<List<Double>> odValues;

    /**
     * Constructor for {@link OriginDestinationVehicleFlowGenerator}.
     *
     * @param matrixMapperConfiguration the configuration of the matrix mapper set in the json configuration
     */
    OriginDestinationVehicleFlowGenerator(CVehicle.COriginDestinationMatrixMapper matrixMapperConfiguration) {
        this.originDestinationPointConfigurations = matrixMapperConfiguration.points;
        this.prototypeConfigurations = matrixMapperConfiguration.types;
        this.deterministic = matrixMapperConfiguration.deterministic;
        this.odValues = matrixMapperConfiguration.odValues;
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
                vehicleConfiguration.startingTime = 0;
                //Assuming, if we only have set a flow, that we want to have an unlimited number of vehicles
                vehicleConfiguration.maxNumberVehicles = Integer.MAX_VALUE;


                framework.addVehicleStream(new VehicleFlowGenerator(vehicleConfiguration, randomNumberGenerator, flowNoise));
            }
        }
    }
}
