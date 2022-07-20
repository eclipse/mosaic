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

package org.eclipse.mosaic.app.tutorial;

import org.eclipse.mosaic.app.tutorial.message.GreenWaveMsg;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.TrafficLightOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

import org.apache.commons.lang3.Validate;

public final class TrafficLightApp extends AbstractApplication<TrafficLightOperatingSystem> implements CommunicationApplication {
    public final static String SECRET = "open sesame!";
    private final static short GREEN_DURATION = 10;

    private static final String DEFAULT_PROGRAM = "1";
    private static final String GREEN_PROGRAM = "0";

    private static final Integer MIN_DISTANCE = 15;

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "Initialize application");
        getOs().getAdHocModule().enable();
        getLog().infoSimTime(this, "Activated Wifi Module");
        setRed();
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

    private void setGreen() {
        getOs().switchToProgram(GREEN_PROGRAM);
        getLog().infoSimTime(this, "Setting traffic lights to GREEN");

        getOs().getEventManager().addEvent(
                getOs().getSimulationTime() + GREEN_DURATION * TIME.SECOND,
                (e) -> setRed()
        );
    }

    private void setRed() {
        getOs().switchToProgram(DEFAULT_PROGRAM);
        getLog().infoSimTime(this, "Setting traffic lights to RED");
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        if (!(receivedV2xMessage.getMessage() instanceof GreenWaveMsg)) {
            return;
        }
        getLog().infoSimTime(this, "Received GreenWaveMsg");

        if (!((GreenWaveMsg) receivedV2xMessage.getMessage()).getMessage().equals(SECRET)) {
            return;
        }
        getLog().infoSimTime(this, "Received correct passphrase: {}", SECRET);

        Validate.notNull(receivedV2xMessage.getMessage().getRouting().getSource().getSourcePosition(),
                "The source position of the sender cannot be null");
        if (!(receivedV2xMessage.getMessage().getRouting().getSource().getSourcePosition()
                .distanceTo(getOs().getPosition()) <= MIN_DISTANCE)) {
            getLog().infoSimTime(this, "Vehicle that sent message is too far away.");
            return;
        }

        if (DEFAULT_PROGRAM.equals(getOs().getCurrentProgram().getProgramId())) {
            setGreen();
        }

    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgedMessage) {
        // nop
    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {
        // nop
    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {
        // nop
    }

    @Override
    public void processEvent(Event event) throws Exception {
        // nop
    }
}

