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

package org.eclipse.mosaic.rti;

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.Monitor;
import org.eclipse.mosaic.rti.api.RtiAmbassador;

import com.google.common.collect.ImmutableCollection;

import javax.annotation.Nonnull;

/**
 * Implementation of <code>RtiAmbassador</code> that forwards all requests to
 * responsible service bundle.
 */
public class MosaicRtiAmbassador implements RtiAmbassador {

    private final String federateId;
    private final ComponentProvider componentProvider;


    public MosaicRtiAmbassador(ComponentProvider componentProvider, String federateId) {
        this.federateId = federateId;
        this.componentProvider = componentProvider;
    }

    @Override
    public synchronized void requestAdvanceTime(long time) throws IllegalValueException {
        requestAdvanceTime(time, 0, (byte) 0);
    }

    @Override
    public synchronized void requestAdvanceTime(long time, long lookahead, byte priority) throws IllegalValueException {
        componentProvider.getTimeManagement().requestAdvanceTime(federateId, time, lookahead, priority);
    }

    @Override
    public synchronized void triggerInteraction(Interaction interaction) throws IllegalValueException, InternalFederateException {
        // Attach sender information to interaction
        interaction.setSenderId(federateId);
        componentProvider.getInteractionManagement().publishInteraction(interaction);
    }

    @Override
    public synchronized long getNextEventTimestamp() throws IllegalValueException {
        return componentProvider.getTimeManagement().getNextEventTimestamp();
    }

    @Override
    public synchronized ImmutableCollection<String> getSubscribedInteractions() {
        return componentProvider.getInteractionManagement().getSubscribedInteractions(federateId);
    }

    @Nonnull
    @Override
    public synchronized RandomNumberGenerator createRandomNumberGenerator() {
        return componentProvider.createRandomNumberGenerator();
    }

    @Nonnull
    @Override
    public synchronized Monitor getMonitor() {
        return componentProvider.getMonitor();
    }
}
