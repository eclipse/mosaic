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

package org.eclipse.mosaic.test.app.sendandreceive;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CellModuleConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.ServerOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.objects.v2x.GenericV2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.util.scheduling.EventProcessor;
import org.eclipse.mosaic.rti.DATA;
import org.eclipse.mosaic.rti.TIME;

import javax.annotation.Nonnull;

/**
 * This application sends an empty cell message to a vehicle and logs this. The integration test checks whether the delay was
 * properly calculated.
 */
public class SendAndReceiveRoundTripMessage extends AbstractApplication<ServerOperatingSystem> implements CommunicationApplication {

    private final static long SEND_TIME = 310 * TIME.SECOND;

    private final String receiver;

    public SendAndReceiveRoundTripMessage(String receiver) {
        this.receiver = receiver;
    }

    /**
     * Setup {@link org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CellModule} and send message to
     * vehicle.
     */
    @Override
    public void onStartup() {
        getOs().getCellModule().enable(
                new CellModuleConfiguration()
                        .maxDownlinkBitrate(10 * DATA.GIGABYTE)
                        .maxUplinkBitrate(10 * DATA.GIGABYTE)
        );
        getLog().infoSimTime(this, "Setup TMC server {} at time {}", getOs().getId(), getOs().getSimulationTime());

        getOs().getEventManager().addEvent(new SendRoundTripMessageEvent(SEND_TIME, this));
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
        if (event instanceof SendRoundTripMessageEvent) {
            MessageRouting routing = getOs().getCellModule().createMessageRouting().tcp().topoCast(receiver);
            getOs().getCellModule().sendV2xMessage(new GenericV2xMessage(routing, 8));
            getLog().infoSimTime(this, "Message sent at time {}", getOs().getSimulationTime());
        }
    }

    private static class SendRoundTripMessageEvent extends Event {

        SendRoundTripMessageEvent(long time, @Nonnull EventProcessor processor) {
            super(time, processor);
        }
    }
}
