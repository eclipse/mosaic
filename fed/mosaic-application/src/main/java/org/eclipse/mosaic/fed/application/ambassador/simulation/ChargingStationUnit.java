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

import org.eclipse.mosaic.fed.application.ambassador.ErrorRegister;
import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.app.api.ChargingStationApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.ChargingStationOperatingSystem;
import org.eclipse.mosaic.interactions.electricity.ChargingStationUpdates;
import org.eclipse.mosaic.lib.enums.RsuType;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.electricity.ChargingStationData;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.RsuAwarenessData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class represents a charging station for electric vehicles in the application simulator.
 */
public class ChargingStationUnit extends AbstractSimulationUnit implements ChargingStationOperatingSystem {

    @Nullable
    private ChargingStationData cs;

    /**
     * Creates a new charging station simulation unit.
     *
     * @param chargingStationName     charging station identifier
     * @param chargingStationPosition charging station position
     */
    public ChargingStationUnit(String chargingStationName, GeoPoint chargingStationPosition) {
        super(chargingStationName, chargingStationPosition);
        setRequiredOperatingSystem(ChargingStationOperatingSystem.class);
    }

    @Override
    public GeoPoint getPosition() {
        return getInitialPosition();
    }

    @Override
    public void sendChargingStationUpdates(long time, ChargingStationData chargingStation) {
        ChargingStationUpdates csu = new ChargingStationUpdates(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                chargingStation
        );
        sendInteractionToRti(csu);
    }

    @Override
    public ChargingStationData getChargingStationData() {
        return cs;
    }

    private void updateChargingStation(final ChargingStationData cs) {
        ChargingStationData previousState = this.cs;
        this.cs = cs;
        for (ChargingStationApplication application : getApplicationsIterator(ChargingStationApplication.class)) {
            application.onChargingStationUpdated(previousState, cs);
        }
    }

    @Override
    public void processEvent(@Nonnull final Event event) throws Exception {
        // never remove the preProcessEvent call!
        final boolean preProcessed = super.preProcessEvent(event);

        // failsafe
        if (preProcessed) {
            return;
        }

        final Object resource = event.getResource();

        // failsafe
        if (resource == null) {
            getOsLog().error("Event has no resource: {}", event);
            throw new RuntimeException(ErrorRegister.CHARGING_STATION_NoEventResource.toString());
        }

        if (resource instanceof ChargingStationData) {
            updateChargingStation((ChargingStationData) resource);
        } else {
            getOsLog().error("Unknown event resource: {}", event);
            throw new RuntimeException(ErrorRegister.CHARGING_STATION_UnknownEvent.toString());
        }
    }

    @Override
    public CamBuilder assembleCamMessage(CamBuilder camBuilder) {
        camBuilder
                .awarenessData(new RsuAwarenessData(RsuType.CHARGING_STATION))
                .position(getPosition());
        for (CommunicationApplication communicationApplication : getApplicationsIterator(CommunicationApplication.class)) {
            communicationApplication.onCamBuilding(camBuilder);
        }
        return camBuilder;
    }

}
