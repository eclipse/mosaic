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

package org.eclipse.mosaic.app.tutorial.trafficlight;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.TrafficLightOperatingSystem;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLight;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgramPhase;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

import java.util.List;

/**
 * Simple Traffic Light example Application.
 *
 * - Prints out the lanes controlled by the traffic light group
 * - Prints out the traffic lights this group consists of
 * - Sets the remaining duration for the current traffic light phase
 *
 * See http://sumo.dlr.de/wiki/Simulation/Traffic_Lights and {@link org.eclipse.mosaic.app.tutorial.TrafficLightApp} for more details.
 */
@SuppressWarnings("JavadocReference")
public class TrafficLightControlApp extends AbstractApplication<TrafficLightOperatingSystem> {

    private boolean lengthened = false;

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "Startup");

        getLog().infoSimTime(this, "Getting controlled Lanes");

        for (String lane : getOs().getControlledLanes()) {
            getLog().infoSimTime(this, "Controlled lane: {}", lane);
        }

        //in the mapping_config we refer not to one separated traffic light but to traffic light group that is responsible for e.g. one crossroad
        //an example for it will be 5 traffic signals that we get in the next code line for the first traffic light group with group id 252864801
        //each traffic signal is responsible for each direction, in which a vehicle can drive on this crossroad
        final List<TrafficLight> trafficLights = getOs().getTrafficLightGroup().getTrafficLights();

        for (TrafficLight trafficLight : trafficLights) {
            getLog().infoSimTime(this, "Traffic light: {}", trafficLight.toString());
        }

        getOs().getEventManager().addEvent(getOs().getSimulationTime() + TIME.SECOND, this);
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown");
    }

    @Override
    public void processEvent(Event event) throws Exception {
        getLog().infoSimTime(this, "Current phase for the traffic light group {} is {} and the remaining duration is {}",
                getOs().getTrafficLightGroup().getGroupId(), getOs().getCurrentPhase(), getOs().getCurrentPhase().getRemainingDuration());

        //remaining duration is measured in milliseconds
        if (getOs().getCurrentProgram().getCurrentPhase().getRemainingDuration() == 1000 && !lengthened) {
            getOs().setRemainingDurationOfCurrentPhase(5_000);
            lengthened = true;
        }

        final Event nextEvent = new Event(getOs().getSimulationTime() + 1 * TIME.SECOND, this::checkTrafficLightPhase);
        getOs().getEventManager().addEvent(nextEvent);
    }

    public void checkTrafficLightPhase(Event event) {
        TrafficLightProgramPhase currentPhase = getOs().getCurrentPhase();
        getLog().infoSimTime(this, "Current phase for the traffic light group {} is {} and the remaining duration is {}",
                getOs().getTrafficLightGroup().getGroupId(), getOs().getCurrentPhase(), getOs().getCurrentPhase().getRemainingDuration());
    }
}
