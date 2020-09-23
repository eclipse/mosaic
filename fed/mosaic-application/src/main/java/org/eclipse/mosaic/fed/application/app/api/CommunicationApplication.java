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

package org.eclipse.mosaic.fed.application.app.api;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;

/**
 * All applications that implement some form of V2x communication
 * are to implement this interface.
 */
public interface CommunicationApplication extends Application {

    /**
     * Receive a V2X Message.
     *
     * @param receivedV2xMessage the received message container.
     */
    void onMessageReceived(ReceivedV2xMessage receivedV2xMessage);

    /**
     * Receive an acknowledgement from a previously sent V2X Message.
     *
     * @param acknowledgement the acknowledgement object which contains the sent V2X Message and the acknowledgement status.
     */
    void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgement);

    /**
     * This method is called when a CAM is build by different Simulation Units that support CAM connectivity.
     *
     * @param camBuilder the builder for the CAM
     */
    void onCamBuilding(CamBuilder camBuilder);

    /**
     * This method is called when a V2X message is transmitted.
     *
     * @param v2xMessageTransmission the container for the V2XMessage to be transmitted
     */
    void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission);
}
