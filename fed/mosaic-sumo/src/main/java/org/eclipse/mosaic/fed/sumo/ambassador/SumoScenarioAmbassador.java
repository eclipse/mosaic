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

package org.eclipse.mosaic.fed.sumo.ambassador;

import org.eclipse.mosaic.fed.sumo.bridge.TraciClientBridge;
import org.eclipse.mosaic.fed.sumo.util.MosaicConformVehicleIdTransformer;
import org.eclipse.mosaic.interactions.mapping.advanced.ScenarioVehicleRegistration;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.List;

/**
 * Implementation of {@link AbstractSumoAmbassador} which allows to execute
 * existing sumo scenarios. No vehicles are added to the simulation
 * which are defined in "VehicleRegistration" interaction. Those topics should be
 * addressed later.
 *
 * <br><br>
 * Configuration in runtime.json file:
 * <pre>
 * {@code
 * {
 *     "id": "sumo",
 *     "classname": "org.eclipse.mosaic.fed.sumo.ambassador.SumoScenarioAmbassador",
 *     "configuration": "sumo_config.json",
 *     "priority": 50,
 *     "host": "local",
 *     "port": 0,
 *     "deploy": true,
 *     "start": true,
 *     "subscriptions": [
 *         "VehicleSlowDown",
 *         "VehicleRouteChange",
 *         "VehicleLaneChange",
 *         "TrafficLightStateChange",
 *         "VehicleStop",
 *         "VehicleResume",
 *         "SumoTraciRequest",
 *         "VehicleDistanceSensorActivation",
 *         "VehicleParametersChange",
 *         "VehicleSpeedChange",
 *         "VehicleFederateAssignment",
 *         "VehicleUpdates",
 *         "VehicleRegistration",
 *         "VehicleRoutesInitialization"
 *         "InductionLoopDetectorSubscription",
 *         "LaneAreaDetectorSubscription",
 *         "TrafficLightSubscription"
 *     ],
 *     "javaClasspathEntries": []
 * }
 * }
 * </pre>
 */
public class SumoScenarioAmbassador extends AbstractSumoAmbassador {

    /**
     * Creates a new {@link SumoScenarioAmbassador} object using
     * the super constructor, which loads the configuration
     * from the ambassadorParameter.
     *
     * @param ambassadorParameter containing parameters for the sumo ambassador.
     */
    public SumoScenarioAmbassador(AmbassadorParameter ambassadorParameter) {
        super(ambassadorParameter);
    }

    @Override
    public void initialize(long startTime, long endTime) throws InternalFederateException {
        super.initialize(startTime, endTime);

        configure();
        startSumoLocal();
        initSumoConnection();
    }

    @SuppressWarnings(
            value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "We currently support only one SUMO simulation being active at once, "
                    + "especially when using this Scenario-based Ambassador. So it's fine here for now."
    )
    private void configure() {
        TraciClientBridge.VEHICLE_ID_TRANSFORMER = new MosaicConformVehicleIdTransformer();
    }

    @Override
    protected synchronized void flushNotYetAddedVehicles(long time) throws InternalFederateException {
        final List<String> departedVehicles = bridge.getSimulationControl().getDepartedVehicles();
        String vehicleTypeId;
        for (String vehicleId : departedVehicles) {
            bridge.getSimulationControl().subscribeForVehicle(vehicleId, time, this.getEndTime());

            vehicleTypeId = bridge.getVehicleControl().getVehicleTypeId(vehicleId);

            try {
                rti.triggerInteraction(new ScenarioVehicleRegistration(this.nextTimeStep, vehicleId, vehicleTypeId));
            } catch (IllegalValueException e) {
                throw new InternalFederateException(e);
            }
        }
    }

    @Override
    protected void initializeTrafficLights(long time) {
        try {
            super.initializeTrafficLights(time);
        } catch (Exception e) {
            log.error("Could not initialize traffic lights, skipping.", e);
        }
    }
}
