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

import org.eclipse.mosaic.fed.application.ambassador.ErrorRegister;
import org.eclipse.mosaic.fed.application.ambassador.navigation.IRoutingModule;
import org.eclipse.mosaic.fed.application.ambassador.navigation.NavigationModule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.RoadSideUnitOperatingSystem;
import org.eclipse.mosaic.lib.enums.RsuType;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.RsuAwarenessData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import javax.annotation.Nonnull;

/**
 * This class represents a Road Side Unit in the application simulator.
 */
public class RoadSideUnit extends AbstractSimulationUnit implements RoadSideUnitOperatingSystem {

    private final IRoutingModule routingModule;

    /**
     * Creates a new Road Side simulation Unit.
     *
     * @param rsuName     RSU identifier
     * @param rsuPosition RSU position
     */
    public RoadSideUnit(String rsuName, GeoPoint rsuPosition) {
        super(rsuName, rsuPosition);
        setRequiredOperatingSystem(RoadSideUnitOperatingSystem.class);
        routingModule = new NavigationModule(this);
    }

    @Override
    public GeoPoint getPosition() {
        return getInitialPosition();
    }

    @Override
    public IRoutingModule getRoutingModule() {
        return routingModule;
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
            throw new RuntimeException(ErrorRegister.ROAD_SIDE_UNIT_NoEventResource.toString());
        }

        getOsLog().error("Unknown event resource: {}", event);
        throw new RuntimeException(ErrorRegister.ROAD_SIDE_UNIT_UnknownEvent.toString());
    }

    @Override
    public CamBuilder assembleCamMessage(CamBuilder camBuilder) {
        camBuilder
                .awarenessData(new RsuAwarenessData(RsuType.REPEATER))
                .position(getPosition());

        for (CommunicationApplication communicationApplication : getApplicationsIterator(CommunicationApplication.class)) {
            communicationApplication.onCamBuilding(camBuilder);
        }
        return camBuilder;
    }

}
