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

package org.eclipse.mosaic.fed.application.ambassador.simulation.sensor;

import org.eclipse.mosaic.fed.application.app.api.sensor.Sensor;
import org.eclipse.mosaic.lib.spatial.PointCloud;

import java.util.function.Consumer;
import javax.annotation.Nullable;

public class LidarSensor implements Sensor<PointCloud> {

    @Override
    public void enable() {
        
    }

    @Override
    public void disable() {

    }

    @Override
    public @Nullable PointCloud getSensorData() {
        return null;
    }

    @Override
    public void reactOnSensorDataUpdate(Consumer<PointCloud> consumer) {

    }

    public final void addPointCloud(PointCloud pointCloud) {
        
    }
}
