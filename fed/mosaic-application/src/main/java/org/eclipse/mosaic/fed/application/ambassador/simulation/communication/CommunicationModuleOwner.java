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

package org.eclipse.mosaic.fed.application.ambassador.simulation.communication;

import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Cam;
import org.eclipse.mosaic.rti.api.Interaction;

/**
 * Interface to be implemented by classes providing a communication module.
 * Requires callback methods for the modules.
 */
public interface CommunicationModuleOwner {

    String getId();

    /**
     * Returns the current position as a {@link GeoPoint}.
     *
     * @return the current position.
     */
    GeoPoint getPosition();

    long getSimulationTime();

    /**
     * Sends the given {@link Interaction} to the runtime infrastructure.
     *
     * @param interaction the {@link Interaction} to be send
     */
    void sendInteractionToRti(Interaction interaction);

    /**
     * Assembles a {@link CamBuilder} (Cooperative Awareness Message) using the
     * given {@link MessageRouting} and the units' status information.
     *
     * @param camBuilder the routing for the {@link Cam}
     * @return the assembled {@link Cam}, which can be sent
     */
    CamBuilder assembleCamMessage(CamBuilder camBuilder);

    /**
     * This function is called when a Message has been sent.
     *
     * @param message the sent message
     */
    void triggerOnSendMessage(V2xMessageTransmission message);
}
