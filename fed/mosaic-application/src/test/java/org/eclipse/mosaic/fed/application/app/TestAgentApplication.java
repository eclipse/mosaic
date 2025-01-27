/*
 * Copyright (c) 2025 Fraunhofer FOKUS and others. All rights reserved.
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

import org.eclipse.mosaic.fed.application.app.api.Application;
import org.eclipse.mosaic.fed.application.app.api.OperatingSystemAccess;
import org.eclipse.mosaic.fed.application.app.api.os.AgentOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import org.mockito.Mockito;

public class TestAgentApplication extends AbstractApplication<AgentOperatingSystem>
        implements TestApplicationWithSpy<OperatingSystemAccess<AgentOperatingSystem>>, Application {

    private final OperatingSystemAccess<AgentOperatingSystem> thisApplicationSpy;

    public TestAgentApplication() {
        thisApplicationSpy = Mockito.mock(OperatingSystemAccess.class);
    }

    @Override
    public OperatingSystemAccess<AgentOperatingSystem> getApplicationSpy() {
        return thisApplicationSpy;
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
    public void processEvent(Event event) throws Exception {
        thisApplicationSpy.processEvent(event);
    }
}
