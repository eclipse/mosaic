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

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.ServerOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;

/**
 * This dummy app is used to validate that servers without a configuration in the network.json are properly handled in the CellAmbassador.
 */
public class NoCellCommunicationServer extends AbstractApplication<ServerOperatingSystem> {
    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "NoCellCommunicationServer setup.");
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void processEvent(Event event) {

    }
}
