/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.TrafficLightObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.lib.database.Database;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of {@link AbstractPerceptionModule} which gets instantiated if the vehicle index provider wasn't configured
 * in the application configuration.
 */
public class DisabledPerceptionModule extends AbstractPerceptionModule {

    public DisabledPerceptionModule(PerceptionModuleOwner owner, Database database, Logger log) {
        super(owner, database, log);
        log.debug("No vehicle index provider configured, leading to disabled perception.");
    }

    @Override
    List<VehicleObject> getVehiclesInRange() {
        log.debug("No vehicle index provider configured, leading to disabled perception.");
        return new ArrayList<>();
    }

    @Override
    List<TrafficLightObject> getTrafficLightsInRange() {
        log.debug("No vehicle index provider configured, leading to disabled perception.");
        return new ArrayList<>();
    }

    @Override
    List<SpatialObject> getObjectsInRange() {
        log.debug("No vehicle index provider configured, leading to disabled perception.");
        return new ArrayList<>();
    }
}
