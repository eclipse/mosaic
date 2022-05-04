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

package org.eclipse.mosaic.app.tutorial.cam;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.SerializationUtils;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

import java.io.IOException;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is a simple application that shows sending a CAM (Cooperative Awareness Message) with an additional information (user tagged value)
 * by using the {@link CamBuilder#userTaggedValue(byte[])}) method.
 * In this way an additional byte field can be sent via CAM, nevertheless this is often connected with some serious work.
 * You may also want to safely serialize / deserialize objects.
 * <p>
 * The CAMs will be sent by an ad hoc module so that only vehicles with an enabled ad hoc module can receive it.
 **/
public class CamSendingApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication, CommunicationApplication {

    /**
     * If the control of every byte is not needed, the
     * {@link SerializationUtils} can be used. This class converts an
     * object into a byte field and vice versa.
     */
    public static final SerializationUtils<MyComplexTaggedValue> DEFAULT_OBJECT_SERIALIZATION = new SerializationUtils<>();

    private static class MyComplexTaggedValue implements Serializable {
        public int fooInt;
        public String fooString;

        @Override
        public String toString() {
            return "MyComplexTaggedValue: fooInt=" + fooInt + ", " + "fooString=" + fooString;
        }
    }

    /**
     * Setting up the communication module and scheduling next event for the next second.
     */
    @Override
    public void onStartup() {
        getOs().getAdHocModule().enable(new AdHocModuleConfiguration()
                .camMinimalPayloadLength(200L)
                .addRadio().channel(AdHocChannel.CCH).power(50).create()
        );
        getLog().infoSimTime(this, "Set up");
        //sendCam(); Don't do this here! Sending CAMs only makes
        // sense when we have access to vehicle info of sender, which is not ready at the set up stage.

        getOs().getEventManager().addEvent(getOs().getSimulationTime() + TIME.SECOND, this);
    }

    /**
     * Sending CAM and scheduling next events every second.
     */
    @Override
    public void processEvent(Event event) {
        sendCam();

        getOs().getEventManager().addEvent(getOs().getSimulationTime() + TIME.SECOND, this);
    }

    private void sendCam() {
        getLog().infoSimTime(this, "Sending CAM");
        getOs().getAdHocModule().sendCam();
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgedMessage) {
    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {
        // this method will be triggered from the operating system (may a CAM or DENM will be prepared to send)
        // create a new object
        CamSendingApp.MyComplexTaggedValue exampleContent = new CamSendingApp.MyComplexTaggedValue();
        exampleContent.fooInt = 5;
        exampleContent.fooString = "Hello from " + (getOs().getVehicleData() != null
                ? getOs().getVehicleData().getName()
                : "unknown vehicle"
        );

        try {
            byte[] byteArray = DEFAULT_OBJECT_SERIALIZATION.toBytes(exampleContent);
            camBuilder.userTaggedValue(byteArray);
        } catch (IOException ex) {
            getLog().error("Error during a serialization.", ex);
        }
    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Tear down");
    }

    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {

    }

}
