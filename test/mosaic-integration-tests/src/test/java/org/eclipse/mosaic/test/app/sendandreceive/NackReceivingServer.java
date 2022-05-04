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
import org.eclipse.mosaic.rti.TIME;

import javax.annotation.Nonnull;

/**
 * Server App sending messages to different servers via TCP to assert for negative acknowledgements.
 */
public class NackReceivingServer extends AbstractApplication<ServerOperatingSystem> implements CommunicationApplication {

    private static final String LIMITED_CAPACITY_SERVER = "server_1";
    private static final String LOSSY_SERVER = "server_2";

    private final static long SEND_TIME = 5 * TIME.SECOND;

    @Override
    public void onStartup() {
        getOs().getCellModule().enable();
        getLog().infoSimTime(this, "Setup server {} at time {}", getOs().getId(), getOs().getSimulationTime());

        getOs().getEventManager().addEvent(new SendSimpleMessage(SEND_TIME, this, LIMITED_CAPACITY_SERVER));
        getOs().getEventManager().addEvent(new SendSimpleMessage(SEND_TIME  + TIME.NANO_SECOND, this, LIMITED_CAPACITY_SERVER));
        getOs().getEventManager().addEvent(new SendSimpleMessage(SEND_TIME, this, LOSSY_SERVER));

    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgement) {
        getLog().infoSimTime(
                this,
                "Received acknowledgement={} for message={} from={} with nackReasons={}",
                acknowledgement.isAcknowledged(),
                acknowledgement.getSentMessage().getId(),
                acknowledgement.getSentMessage().getRouting().getDestination().getAddress(),
                acknowledgement.getNegativeAckReasons());
    }

    @Override
    public void processEvent(Event event) {
        if (event instanceof SendSimpleMessage) {
            MessageRouting routing = getOs().getCellModule().createMessageRouting().tcp().topoCast(((SendSimpleMessage) event).receiver);
            getOs().getCellModule().sendV2xMessage(new GenericV2xMessage(routing, 8));
            getLog().infoSimTime(this, "Message sent at time {}", getOs().getSimulationTime());
        }
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
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

    private static class SendSimpleMessage extends Event {

        private final String receiver;

        SendSimpleMessage(long time, @Nonnull EventProcessor processor, String receiver) {
            super(time, processor);
            this.receiver = receiver;
        }
    }
}
