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

package org.eclipse.mosaic.fed.application.app;

import org.eclipse.mosaic.fed.application.app.api.TrafficLightApplication;
import org.eclipse.mosaic.fed.application.app.api.os.TrafficLightOperatingSystem;
import org.eclipse.mosaic.fed.application.app.empty.TrafficLightNoopApp;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import org.mockito.Mockito;

public class TestTrafficLightApplication extends AbstractApplication<TrafficLightOperatingSystem>
        implements TestApplicationWithSpy<TrafficLightNoopApp>, TrafficLightApplication {

    private TrafficLightNoopApp thisApplicationSpy;

    public TestTrafficLightApplication() {
        // We use this mock to later count calls of the class' methods
        thisApplicationSpy = Mockito.mock(TrafficLightNoopApp.class);
    }

    public TrafficLightNoopApp getApplicationSpy() {
        return thisApplicationSpy;
    }

    @Override
    public void processEvent(Event event) throws Exception {
        thisApplicationSpy.processEvent(event);
    }

    @Override
    public void onStartup() {
        thisApplicationSpy.onStartup();
    }

    @Override
    public void onShutdown() {
        thisApplicationSpy.onShutdown();
    }

    @Override
    public void onTrafficLightGroupUpdated(TrafficLightGroupInfo previousState, TrafficLightGroupInfo updatedState) {
        this.thisApplicationSpy.onTrafficLightGroupUpdated(previousState, updatedState);
    }
}
