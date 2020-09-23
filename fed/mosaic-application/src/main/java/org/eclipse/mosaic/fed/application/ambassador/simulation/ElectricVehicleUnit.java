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

package org.eclipse.mosaic.fed.application.ambassador.simulation;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.app.api.ElectricVehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.ElectricVehicleOperatingSystem;
import org.eclipse.mosaic.interactions.electricity.ChargingDenialResponse;
import org.eclipse.mosaic.interactions.electricity.VehicleChargingStartRequest;
import org.eclipse.mosaic.interactions.electricity.VehicleChargingStopRequest;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleBatteryState;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;

/**
 * This class represents an electric vehicle in the application simulator. It extends {@link VehicleUnit}
 * with further functionality.
 */
public class ElectricVehicleUnit extends VehicleUnit implements ElectricVehicleOperatingSystem {

    private VehicleBatteryState vehicleBatteryState;

    /**
     * Creates a new ElectricVehicle.
     *
     * @param vehicleName     vehicle identifier
     * @param vehicleType     vehicle type
     * @param initialPosition initial position
     */
    public ElectricVehicleUnit(String vehicleName, VehicleType vehicleType, final GeoPoint initialPosition) {
        super(vehicleName, vehicleType, initialPosition);
        setRequiredOperatingSystem(ElectricVehicleOperatingSystem.class);
    }

    private void updateBatteryInformation(final VehicleBatteryState currentVehicleBatteryState) {
        // set the new vehicle electric info reference
        VehicleBatteryState previousVehicleBatteryState = this.vehicleBatteryState;
        this.vehicleBatteryState = currentVehicleBatteryState;

        for (ElectricVehicleApplication application : getApplicationsIterator(ElectricVehicleApplication.class)) {
            application.onBatteryStateUpdated(previousVehicleBatteryState, currentVehicleBatteryState);
        }
    }

    private void onVehicleChargingDenialResponse(final ChargingDenialResponse chargingDenialResponse) {
        for (ElectricVehicleApplication application : getApplicationsIterator(ElectricVehicleApplication.class)) {
            application.onChargingRequestRejected(chargingDenialResponse);
        }
    }

    @Override
    protected boolean handleEventResource(Object resource, long eventType) {
        if (resource instanceof VehicleBatteryState) {
            updateBatteryInformation((VehicleBatteryState) resource);
            return true;
        }

        if (resource instanceof ChargingDenialResponse) {
            onVehicleChargingDenialResponse((ChargingDenialResponse) resource);
            return true;
        }

        return super.handleEventResource(resource, eventType);
    }

    @Override
    public VehicleBatteryState getBatteryState() {
        return vehicleBatteryState;
    }

    @Override
    public void sendVehicleChargingStartRequest(String chargingStationId) {
        VehicleChargingStartRequest vehicleChargingStartRequest = new VehicleChargingStartRequest(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                getId(),
                chargingStationId
        );
        sendInteractionToRti(vehicleChargingStartRequest);
    }

    @Override
    public void sendVehicleChargingStopRequest() {
        VehicleChargingStopRequest vehicleChargingStopRequest = new VehicleChargingStopRequest(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                getId()
        );
        sendInteractionToRti(vehicleChargingStopRequest);
    }
}
