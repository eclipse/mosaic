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

package org.eclipse.mosaic.fed.application.ambassador.simulation;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.app.api.os.ServerOperatingSystem;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.mapping.ServerMapping;
import org.eclipse.mosaic.lib.util.scheduling.Event;

/**
 * This class represents a Server in the application simulator.
 */
public class ServerUnit extends AbstractSimulationUnit implements ServerOperatingSystem {

    /**
     * Constructor for {@link ServerUnit}, sets the operating system.
     * @param serverMapping mapping for the server
     */
    public ServerUnit(final ServerMapping serverMapping) {
        super(serverMapping.getName(), null);
        setRequiredOperatingSystem(ServerOperatingSystem.class);
    }


    @Override
    public GeoPoint getPosition() {
        throw new UnsupportedOperationException("Servers aren't mapped to a location.");
    }

    @Override
    public CamBuilder assembleCamMessage(CamBuilder camBuilder) {
        throw new UnsupportedOperationException("Servers can't send CAMs.");
    }

    @Override
    public AdHocModule getAdHocModule() {
        throw new UnsupportedOperationException("Servers can't access AdHoc functionality.");
    }

    @Override
    public void processEvent(Event event) throws Exception {
    }
}
