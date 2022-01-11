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

import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import org.slf4j.Logger;

import java.util.List;

public class CameraPerceptionModule implements PerceptionModule {

    protected final PerceptionModuleOwner owner;

    protected final Logger log;

    public CameraPerceptionModule(PerceptionModuleOwner owner, Logger log){
        this.owner = owner;
        this.log = log;
    }

    @Override
    public void enable(CameraPerceptionModuleConfiguration configuration) {

    }

    @Override
    public List<VehicleData> getEnvironmentVehicles() {

        owner.getHeading();
        return null;
    }
}
