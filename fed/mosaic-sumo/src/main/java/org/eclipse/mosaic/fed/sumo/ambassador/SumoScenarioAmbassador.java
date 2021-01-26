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

import org.eclipse.mosaic.fed.sumo.traci.TraciClient;
import org.eclipse.mosaic.fed.sumo.util.MosaicConformVehicleIdTransformer;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.mapping.advanced.ScenarioVehicleRegistration;
import org.eclipse.mosaic.lib.objects.mapping.VehicleMapping;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class SumoScenarioAmbassador extends SumoAmbassador {

    /**
     * Set containing all vehicles, that have been added using the SUMO route file.
     */
    private final Set<String> vehiclesAddedViaRouteFile = new HashSet<>();

    /**
     * Set containing all vehicles, that have been added from the RTI e.g. using the Mapping file.
     */
    private final Set<String> vehiclesAddedViaRti = new HashSet<>();

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
    }

    @SuppressWarnings(
            value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "We currently support only one SUMO simulation being active at once, "
                    + "especially when using this Scenario-based Ambassador. So it's fine here for now."
    )
    private void configure() {
        TraciClient.VEHICLE_ID_TRANSFORMER = new MosaicConformVehicleIdTransformer();
    }

    @Override
    protected synchronized void flushNotYetAddedVehicles(long time) throws InternalFederateException {
        super.flushNotYetAddedVehicles(time);
        final List<String> departedVehicles = traci.getSimulationControl().getDepartedVehicles();
        String vehicleTypeId;
        for (String vehicleId : departedVehicles) {
            if (vehiclesAddedViaRti.contains(vehicleId)) { // only handle route file vehicles here
                continue;
            }
            vehiclesAddedViaRouteFile.add(vehicleId);

            vehicleTypeId = traci.getVehicleControl().getVehicleTypeId(vehicleId);
            try {
                rti.triggerInteraction(new ScenarioVehicleRegistration(this.nextTimeStep, vehicleId, vehicleTypeId));
            } catch (IllegalValueException e) {
                throw new InternalFederateException(e);
            }
        }
    }

    @Override
    protected void receiveInteraction(VehicleRegistration interaction) throws InternalFederateException {
        VehicleMapping vehicleMapping = interaction.getMapping();
        boolean isVehicleAddedViaRti = !vehiclesAddedViaRouteFile.contains(vehicleMapping.getName());
        if (isVehicleAddedViaRti) {
            vehiclesAddedViaRti.add(vehicleMapping.getName());
            super.receiveInteraction(interaction);
        } else if (sumoConfig.subscribeToAllVehicles || vehicleMapping.hasApplication()) { // still subscribe to vehicles with apps
            log.info(
                    "VehicleRegistration for SUMO vehicle \"{}\" received at simulation time {} ns",
                    vehicleMapping.getName(),
                    interaction.getTime()
            );
            traci.getSimulationControl().subscribeForVehicle(vehicleMapping.getName(), interaction.getTime(), this.endTime);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Not subscribing to Vehicle \"{}\" at {} ns.", vehicleMapping.getName(), interaction.getTime());
            }
        }
    }
}
