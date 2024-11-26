/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

import org.eclipse.mosaic.lib.spatial.PointCloud;

import java.util.function.Consumer;

public interface LidarSensorModule {

    /**
     * Enables this LiDAR sensor by giving the sensor range.
     *
     * @param unitId the id of the unit this LidarSensorModule is placed upon.
     * @param range the range of the LiDAR sensor.
     */
    void enable(String unitId, double range);

    /**
     * @return {@code true}, if this module has been enabled.
     */
    boolean isEnabled();

    /**
     * Disables this basic sensor module. {@link #getPointCloud()} will always return {@code null}.
     */
    void disable();

    /**
     * Registers a {@link Consumer} which is called with the most recent {@link PointCloud} object
     * as soon as it is published to this {@link LidarSensorModule}.
     */
    void reactOnSensorUpdate(Consumer<PointCloud> callback);

    /**
     * Returns the most recent {@link PointCloud} measured by this sensor.
     */
    PointCloud getPointCloud();
}
