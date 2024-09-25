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

package org.eclipse.mosaic.fed.application.app.api.sensor;

import org.eclipse.mosaic.lib.spatial.PointCloud;

public interface SensorModule {

    /**
     * Return a LiDAR sensor which is able to sense {@link PointCloud} objects. LIDAR sensor data
     * is currently only published by vehicle simulators PHABMACS or CARLA.
     */
    Sensor<PointCloud> getLidarSensor();

    /**
     * Returns a sensor which is able to sense environmental events, such as ice on the road,
     * or fog. Environmental events are currently published by the environmental simulator bundled
     * with MOSAIC.
     */
    Sensor<EnvironmentSensorData> getEnvironmentSensor();

}
