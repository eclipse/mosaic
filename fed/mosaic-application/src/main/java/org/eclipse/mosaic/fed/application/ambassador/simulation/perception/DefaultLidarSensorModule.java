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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import org.eclipse.mosaic.fed.application.app.api.perception.LidarSensorModule;
import org.eclipse.mosaic.lib.spatial.PointCloud;

import java.util.function.Consumer;

public class DefaultLidarSensorModule implements LidarSensorModule {

    private PointCloud currentPointcloud;
    private Consumer<PointCloud> callback;

    @Override
    public void enable(double range) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void disable() {

    }

    @Override
    public void reactOnSensorUpdate(Consumer<PointCloud> callback) {
        this.callback = callback;
    }

    @Override
    public PointCloud getPointCloud() {
        return this.currentPointcloud;
    }

    public void addLidarUpdate(PointCloud pointCloud) {
        this.currentPointcloud = pointCloud;
        if (callback != null) {
            callback.accept(currentPointcloud);
        }
    }
}
