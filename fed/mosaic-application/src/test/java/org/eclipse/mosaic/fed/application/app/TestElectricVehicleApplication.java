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

package org.eclipse.mosaic.fed.application.app;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.ElectricVehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.MosaicApplication;
import org.eclipse.mosaic.fed.application.app.api.os.ElectricVehicleOperatingSystem;
import org.eclipse.mosaic.fed.application.app.empty.ElectricVehicle;
import org.eclipse.mosaic.interactions.application.ApplicationInteraction;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.electricity.VehicleChargingDenial;
import org.eclipse.mosaic.lib.objects.traffic.SumoTraciResult;
import org.eclipse.mosaic.lib.objects.vehicle.BatteryData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import org.mockito.Mockito;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TestElectricVehicleApplication extends AbstractApplication<ElectricVehicleOperatingSystem>
        implements TestApplicationWithSpy<ElectricVehicle>, ElectricVehicleApplication, CommunicationApplication, MosaicApplication {

    private ElectricVehicle thisApplicationSpy;

    public TestElectricVehicleApplication() {
        // We use this mock to later count calls of the class' methods
        thisApplicationSpy = Mockito.mock(ElectricVehicle.class);
    }

    public ElectricVehicle getApplicationSpy() {
        return thisApplicationSpy;
    }

    @Override
    public void processEvent(Event event) throws Exception {
        thisApplicationSpy.processEvent(event);
    }

    @Override
    public void onStartup() {
        thisApplicationSpy.onStartup();
    }

    @Override
    public void onShutdown() {
        thisApplicationSpy.onShutdown();
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        thisApplicationSpy.onMessageReceived(receivedV2xMessage);
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgement) {
        thisApplicationSpy.onAcknowledgementReceived(acknowledgement);

    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {
        thisApplicationSpy.onCamBuilding(camBuilder);

    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {
        thisApplicationSpy.onMessageTransmitted(v2xMessageTransmission);

    }

    @Override
    public void onSumoTraciResponded(SumoTraciResult sumoTraciResult) {
        thisApplicationSpy.onSumoTraciResponded(sumoTraciResult);
    }

    @Override
    public void onInteractionReceived(ApplicationInteraction applicationInteraction) {
        thisApplicationSpy.onInteractionReceived(applicationInteraction);
    }

    @Override
    public void onBatteryDataUpdated(@Nullable BatteryData previousBatteryData, @Nonnull BatteryData updatedBatteryData) {
        thisApplicationSpy.onBatteryDataUpdated(previousBatteryData, updatedBatteryData);
    }

    @Override
    public void onVehicleChargingDenial(VehicleChargingDenial vehicleChargingDenial) {
        thisApplicationSpy.onVehicleChargingDenial(vehicleChargingDenial);
    }

    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
        thisApplicationSpy.onVehicleUpdated(previousVehicleData, updatedVehicleData);
    }
}
