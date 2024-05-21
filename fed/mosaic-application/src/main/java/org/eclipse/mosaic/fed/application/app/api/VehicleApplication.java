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

package org.eclipse.mosaic.fed.application.app.api;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.sensor.LidarData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * All applications on vehicles that should be informed about the vehicles movements should implement this interface.
 */
public interface VehicleApplication extends Application {

    /**
     * Is called when ever the vehicle has moved. That is, if the {@link VehicleData} of the unit
     * has been updated.
     *
     * @param previousVehicleData the previous state of the vehicle
     * @param updatedVehicleData  the updated state of the vehicle
     */
    void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData);
    //TODO create new method onLidarUpdated here, that every application has to implement??

    void onLidarUpdated(@Nonnull LidarData updatedLidarData);
}
