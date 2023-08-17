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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.TrafficLightObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Edge;

import java.util.Collection;
import java.util.List;

public interface PerceptionModule<ConfigT extends PerceptionModuleConfiguration> {
    /**
     * Enables and configures this perception module.
     *
     * @param configuration the configuration object
     */
    void enable(ConfigT configuration);

    /**
     * Returns The configuration of the {@link PerceptionModule}.
     */
    ConfigT getConfiguration();

    /**
     * Returns {@code true} if {@link PerceptionModule} is enabled, otherwise {@code false}.
     */
    boolean isEnabled();

    /**
     * Returns a list of all {@link VehicleObject}s inside the perception range of this vehicle.
     */
    List<VehicleObject> getPerceivedVehicles();

    /**
     * Call to get all traffic lights within perception range.
     *
     * @return a list of all {@link TrafficLightObject}s inside the perception range of this vehicle.
     */
    List<TrafficLightObject> getPerceivedTrafficLights();

    /**
     * Call to get all perceived traffic objection within perception range.
     * Note: That these will all be the type of {@link SpatialObject} and you need to properly
     * check types using instanceof.
     *
     * @return a list of all {@link SpatialObject}s inside the perception range
     */
    <T extends SpatialObject<T>> List<SpatialObject<T>> getPerceivedObjects();

    /**
     * Call to get surrounding building walls.
     *
     * @return a list of all surrounding building walls in the for of {@link Edge}s
     */
    Collection<Edge<Vector3d>> getSurroundingWalls();
}
