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

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.app.api.perception.LidarSensorModule;
import org.eclipse.mosaic.interactions.vehicle.VehicleSensorActivation;
import org.eclipse.mosaic.lib.spatial.PointCloud;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DefaultLidarSensorModule implements LidarSensorModule {

    private boolean enabled;
    private final String unitId;

    private PointCloud currentPointcloud;
    private List<Consumer<PointCloud>> callback = new ArrayList<>();

    public DefaultLidarSensorModule(String unitId) {
        this.unitId = unitId;
    }

    @Override
    public void enable(double range) {
        this.enabled = true;

        // Create a VehicleSensorActivation interaction to be sent to the RTI
        VehicleSensorActivation interaction = new VehicleSensorActivation(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                unitId,
                range,
                VehicleSensorActivation.SensorType.LIDAR
        );

        // Send the interaction to the RTI, thereby enabling the LiDAR sensor
        try {
            SimulationKernel.SimulationKernel.getInteractable().triggerInteraction(interaction);
        } catch (IllegalValueException | InternalFederateException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void disable() {
        this.enabled = false;

        // Create a VehicleSensorActivation interaction to be sent to the RTI that disables the LiDAR sensor
        VehicleSensorActivation interaction = new VehicleSensorActivation(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                unitId,
                0,
                VehicleSensorActivation.SensorType.LIDAR
        );

        // Send the interaction to the RTI, thereby disabling the LiDAR sensor
        try {
            SimulationKernel.SimulationKernel.getInteractable().triggerInteraction(interaction);
        } catch (IllegalValueException | InternalFederateException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void reactOnSensorUpdate(Consumer<PointCloud> callback) {
        this.callback.add(callback);
    }

    @Override
    public PointCloud getPointCloud() {
        if (!enabled) {
            return null;
        }
        return this.currentPointcloud;
    }

    public void updatePointCloud(PointCloud pointCloud) {
        this.currentPointcloud = pointCloud;
        for (Consumer<PointCloud> callback : callback) {
            callback.accept(pointCloud);
        }
    }
}
