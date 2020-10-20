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

package org.eclipse.mosaic.app.tutorial;

import org.eclipse.mosaic.app.tutorial.message.GreenWaveMsg;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

public final class VehicleToTrafficLightApp extends AbstractApplication<VehicleOperatingSystem> {
    private final static long  TIME_INTERVAL = TIME.SECOND;

    private void sendGeocastMessage() {
        final double range = 15;
        final GeoCircle geoCircle = new GeoCircle(getOs().getPosition(), range);
        final MessageRouting routing = getOperatingSystem()
                .getAdHocModule()
                .createMessageRouting()
                .geoBroadCast(geoCircle);
        getOs().getAdHocModule().sendV2xMessage(new GreenWaveMsg(routing, TrafficLightApp.SECRET));
        getLog().infoSimTime(this, "Sent secret passphrase");
    }

    private void sample() {
        getOs().getEventManager().addEvent(
                getOs().getSimulationTime() + TIME_INTERVAL, this
        );
        sendGeocastMessage();
    }

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "Initialize application");
        getOs().getAdHocModule().enable();
        getLog().infoSimTime(this, "Activated WLAN Module");
        sample();
    }

    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

    @Override
    public void processEvent(Event event) throws Exception {
        if (!isValidStateAndLog()) {
            return;
        }
        getLog().infoSimTime(this, "Processing event");
        sample();
    }

}
