/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.test.app.sendonshutdown;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;

public class SendOnShutdownApp extends AbstractApplication<VehicleOperatingSystem> {

    @Override
    public void onStartup() {
        getOs().getCellModule().enable();
        getOs().getAdHocModule().enable();

        getOs().getAdHocModule().sendV2xMessage(new ShutdownMessage(
                getOs().getAdHocModule().createMessageRouting().destination("rsu_0").topological()
        ));
    }

    @Override
    public void onShutdown() {
        getOs().getCellModule().sendV2xMessage(new ShutdownMessage(
                getOs().getCellModule().createMessageRouting().topoCast("server_0")
        ));

        getOs().getAdHocModule().sendV2xMessage(new ShutdownMessage(
                getOs().getAdHocModule().createMessageRouting().destination("rsu_0").topological()
        ));
    }

    @Override
    public void processEvent(Event event) throws Exception {

    }
}
