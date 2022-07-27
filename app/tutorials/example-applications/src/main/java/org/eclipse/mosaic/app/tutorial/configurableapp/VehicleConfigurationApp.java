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

package org.eclipse.mosaic.app.tutorial.configurableapp;

import org.eclipse.mosaic.fed.application.app.ConfigurableApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

/**
 * This is a simple application to demonstrate a configurable application.
 * <p>
 * A configuration file(s) should be placed in "application" folder.
 * The filename can end with "_unitId" (e.g. VehicleConfigurationApp_veh_1.json) and will then only be used by the specified unit.
 * It allows configuring one application in different ways for different vehicles.
 * If the configuration filename doesn't include any unit id, it will be used for all unspecified units.
 */
public class VehicleConfigurationApp extends ConfigurableApplication<CExample, VehicleOperatingSystem> {

    /**
     * Configuration object.
     */
    private CExample config;

    public VehicleConfigurationApp() {
        super(CExample.class, "VehicleConfigurationApp");
    }

    @Override
    public void onStartup() {
        this.config = this.getConfiguration();

        this.getOs().getEventManager()
                .newEvent(getOs().getSimulationTime() + 3 * TIME.SECOND, this)
                .withResource("Configuration is created at the time " + getOs().getSimulationTime())
                .schedule();
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void processEvent(Event event) throws Exception {
        Object resource = event.getResource();
        if (resource instanceof String) {
            String message = (String) resource;
            getLog().infoSimTime(this, "Received message: \"{}\"", message);
        }
        getLog().info("Wanted speed from config equals " + this.config.fooInteger);
        getLog().info("Configs fooString equals " + this.config.fooString);
        getLog().info("Configs fooStringList contains " + this.config.fooStringList.toString());

        if (this.config.fooInteger != null) {
            int wantedSpeed = this.config.fooInteger;
            if (getOs().getVehicleData().getSpeed() > wantedSpeed) {
                getLog().info("The current speed equals {}m/s will be slowed down to the wanted speed equals {}m/s",
                        getOs().getVehicleData().getSpeed(), wantedSpeed);
                getOs().slowDown(wantedSpeed, 10 * TIME.SECOND);
            }
        } else {
            getLog().warn("Configuration wasn't loaded from json file!");
        }
    }

}
