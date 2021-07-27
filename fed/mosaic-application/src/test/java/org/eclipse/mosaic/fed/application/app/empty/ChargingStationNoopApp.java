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

package org.eclipse.mosaic.fed.application.app.empty;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.ChargingStationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.ChargingStationOperatingSystem;
import org.eclipse.mosaic.lib.objects.electricity.ChargingStationData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

public class ChargingStationNoopApp extends AbstractApplication<ChargingStationOperatingSystem> implements ChargingStationApplication {

    @Override
    public void onStartup() {
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void onChargingStationUpdated(ChargingStationData previousInfo, ChargingStationData updatedInfo) {
    }

    @Override
    public void processEvent(Event event) throws Exception {
    }
}
