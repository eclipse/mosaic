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

package org.eclipse.mosaic.fed.application.app.api.perception;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.VehicleObject;

import java.util.List;

public interface PerceptionModule<ConfigT extends PerceptionModuleConfiguration> {

    /**
     * Enables and configures this perception module.
     *
     * @param configuration the configuration object
     */
    void enable(ConfigT configuration);

    /**
     * @return a list of all {@link VehicleObject}s inside the perception range of this vehicle.
     */
    List<VehicleObject> getPerceivedVehicles();
}
