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
 *
 */

package org.eclipse.mosaic.test.app.sendandreceive;

import org.eclipse.mosaic.fed.application.ambassador.simulation.RoadSideUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Cam;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.test.app.sendandreceive.messages.TestMessage;

public abstract class AbstractReceiverApp extends AbstractApplication<RoadSideUnit> implements CommunicationApplication {

    private final long evaluationInterval;
    private int msgCountSinceLastEval = 0;

    protected abstract void configureCommunication();

    protected abstract void disableCommunication();

    AbstractReceiverApp() {
        evaluationInterval = TIME.SECOND;
    }

    @Override
    public void onStartup() {
        configureCommunication();
        printCommunicationState();
        sample();
    }

    @Override
    public void onShutdown() {
        disableCommunication();
        printCommunicationState();
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        V2xMessage msg = receivedV2xMessage.getMessage();

        if (msg instanceof Cam) {
            final Cam cam = (Cam) msg;
            final GeoPoint otherPos = cam.getPosition();
            final GeoPoint myPos = getOs().getPosition();
            double distance = myPos.distanceTo(otherPos);
            String senderName = cam.getUnitID();

            getLog().infoSimTime(this, "Received CAM from {}, over a distance of {} m", senderName, distance);

        } else if (msg instanceof TestMessage) {
            TestMessage testMsg = (TestMessage) msg;
            GeoPoint msgPos = testMsg.getSenderPosition();
            GeoPoint myPos = getOs().getPosition();

            double distance = myPos.distanceTo(msgPos);

            getLog().infoSimTime(this, "Received V2X TestMessage, {}, {}", testMsg.getTimeStamp(), distance);

            getLog().debug(" CT=" + getOs().getSimulationTime());
            getLog().debug(" PT=" + testMsg.getTimeStamp());
            getLog().debug(" T=" + testMsg.getTimeStamp() + "[SenderUnit:" + testMsg.getSenderName() + "]");
            getLog().debug(" othPosition:(" + msgPos + ")]");
            getLog().debug(" ownPosition:(" + myPos + ")]");
            getLog().debug(" distance:   (" + distance + ")]");

            msgCountSinceLastEval++;
        } else {
            getLog().infoSimTime(this, "Received V2X message ({})", msg.getSimpleClassName());
        }
    }

    private void evaluate() {
        if (msgCountSinceLastEval > 0) {
            getLog().infoSimTime(this, "Received {} message(s) within the last {} ns", msgCountSinceLastEval, evaluationInterval);
        } else {
            getLog().infoSimTime(this, "No message received within the last {} ns", evaluationInterval);
        }
        msgCountSinceLastEval = 0;
    }


    private void printCommunicationState() {
        getLog().debugSimTime(this, "communicationState - adhocEnabled={}, cellEnabled={}",
                getOs().getAdHocModule().isEnabled(),
                getOs().getCellModule().isEnabled()
        );
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgedMessage) {
    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {
    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {
    }

    @Override
    public void processEvent(Event event) {
        if (!isValidStateAndLog()) {
            return;
        }
        sample();
        evaluate();
    }

    private void sample() {
        getOs().getEventManager().addEvent(
                getOs().getSimulationTime() + evaluationInterval, this
        );
    }

}
