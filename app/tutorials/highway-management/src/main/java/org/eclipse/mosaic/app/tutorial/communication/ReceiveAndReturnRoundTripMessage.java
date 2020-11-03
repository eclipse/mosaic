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

package org.eclipse.mosaic.app.tutorial.communication;

import static org.eclipse.mosaic.app.tutorial.communication.SendAndReceiveRoundTripMessage.RECEIVER_NAME;
import static org.eclipse.mosaic.app.tutorial.communication.SendAndReceiveRoundTripMessage.SERVER_NAME;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.util.scheduling.Event;

public class ReceiveAndReturnRoundTripMessage extends AbstractApplication<VehicleOperatingSystem> implements CommunicationApplication {
    @Override
    public void onStartup() {
        if (getOs().getId().equals(RECEIVER_NAME)) {
            getOs().getCellModule().enable();
        }
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        getLog().infoSimTime(
                this,
                "Received round trip message #{} at time {} using protocol {}",
                receivedV2xMessage.getMessage().getId(),
                getOs().getSimulationTime(),
                receivedV2xMessage.getMessage().getRouting().getDestination().getProtocolType()
        );
        MessageRouting routing = getOs().getCellModule().createMessageRouting().tcp().topoCast(SERVER_NAME);
        getOs().getCellModule().sendV2xMessage(new RoundTripMessage(routing));
        getLog().infoSimTime(this, "Send V2xMessage to {} at time {}", SERVER_NAME, getOs().getSimulationTime());
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgement) {
        getLog().infoSimTime(
                this,
                "Received acknowledgement for round trip message #{} and [acknowledged={}]",
                acknowledgement.getSentMessage().getId(),
                acknowledgement.isAcknowledged()
        );
    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {

    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {

    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void processEvent(Event event) {

    }
}
