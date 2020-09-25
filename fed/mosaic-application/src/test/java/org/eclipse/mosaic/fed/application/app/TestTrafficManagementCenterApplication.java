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

import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.InductionLoop;
import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.LaneAreaDetector;
import org.eclipse.mosaic.fed.application.app.api.TrafficManagementCenterApplication;
import org.eclipse.mosaic.fed.application.app.api.os.TrafficManagementCenterOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import org.mockito.Mockito;

import java.util.Collection;

public class TestTrafficManagementCenterApplication extends AbstractApplication<TrafficManagementCenterOperatingSystem>
        implements TestApplicationWithSpy<TrafficManagementCenterApplication>, TrafficManagementCenterApplication {

    private TrafficManagementCenterApplication thisApplicationSpy;

    public TestTrafficManagementCenterApplication() {
        // We use this mock to later count calls of the class' methods
        thisApplicationSpy = Mockito.mock(TrafficManagementCenterApplication.class);
    }

    public TrafficManagementCenterApplication getApplicationSpy() {
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
    public void onInductionLoopUpdated(Collection<InductionLoop> updatedInductionLoops) {
        thisApplicationSpy.onInductionLoopUpdated(updatedInductionLoops);
    }

    @Override
    public void onLaneAreaDetectorUpdated(Collection<LaneAreaDetector> updatedLaneAreaDetectors) {
        thisApplicationSpy.onLaneAreaDetectorUpdated(updatedLaneAreaDetectors);
    }
}
