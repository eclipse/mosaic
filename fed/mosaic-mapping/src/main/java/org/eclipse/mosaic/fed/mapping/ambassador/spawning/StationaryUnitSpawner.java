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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning;

import org.eclipse.mosaic.fed.mapping.ambassador.SpawningFramework;
import org.eclipse.mosaic.rti.api.InternalFederateException;

public interface StationaryUnitSpawner {

    /**
     * Initializes the unit for the simulation.
     * TODO: Right now traffic lights are the only stationary units not implementing this interface, this is due to the
     *  way the {@link org.eclipse.mosaic.interactions.mapping.advanced.ScenarioTrafficLightRegistration} works. For more cleanliness
     *  this can be readdressed in the future.
     * @param spawningFramework the framework handling the spawning
     * @throws InternalFederateException if unit couldn't be initialized
     */
    void init(SpawningFramework spawningFramework) throws InternalFederateException;
}
