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

package org.eclipse.mosaic.fed.application.ambassador.simulation;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.app.api.ElectricVehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.ElectricVehicleOperatingSystem;
import org.eclipse.mosaic.interactions.electricity.VehicleChargingDenial;
import org.eclipse.mosaic.interactions.electricity.VehicleChargingStartRequest;
import org.eclipse.mosaic.interactions.electricity.VehicleChargingStopRequest;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.vehicle.BatteryData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;

/**
 * This class represents an electric vehicle in the application simulator. It extends {@link VehicleUnit}
 * with further functionality.
 */
public class ElectricVehicleUnit extends VehicleUnit implements ElectricVehicleOperatingSystem {

    private BatteryData batteryData;

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

    private void updateBatteryInformation(final BatteryData currentBatteryData) {
        // set the new vehicle electric info reference
        BatteryData previousBatteryData = this.batteryData;
        this.batteryData = currentBatteryData;

        for (ElectricVehicleApplication application : getApplicationsIterator(ElectricVehicleApplication.class)) {
            application.onBatteryDataUpdated(previousBatteryData, currentBatteryData);
        }
    }

    private void onVehicleChargingDenialResponse(final VehicleChargingDenial vehicleChargingDenial) {
        for (ElectricVehicleApplication application : getApplicationsIterator(ElectricVehicleApplication.class)) {
            application.onChargingRequestRejected(vehicleChargingDenial);
        }
    }

    @Override
    protected boolean handleEventResource(Object resource, long eventType) {
        if (resource instanceof BatteryData) {
            updateBatteryInformation((BatteryData) resource);
            return true;
        }

        if (resource instanceof VehicleChargingDenial) {
            onVehicleChargingDenialResponse((VehicleChargingDenial) resource);
            return true;
        }

        return super.handleEventResource(resource, eventType);
    }

    @Override
    public BatteryData getBatteryState() {
        return batteryData;
    }

    @Override
    public void sendVehicleChargingStartRequest(String chargingStationId) {
        if (getVehicleData() != null && !getVehicleData().isStopped()) {
            VehicleChargingStartRequest vehicleChargingStartRequest = new VehicleChargingStartRequest(
                    SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                    getId(),
                    chargingStationId
            );
            sendInteractionToRti(vehicleChargingStartRequest);
        } else {
            throw new RuntimeException("Cannot send VehicleChargingStartRequest, vehicle is not stopped.");
        }
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
