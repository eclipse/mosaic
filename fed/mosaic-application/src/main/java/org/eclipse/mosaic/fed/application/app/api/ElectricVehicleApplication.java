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

import org.eclipse.mosaic.interactions.electricity.ChargingStationDiscoveryResponse;
import org.eclipse.mosaic.interactions.electricity.VehicleChargingDenial;
import org.eclipse.mosaic.lib.objects.vehicle.BatteryData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * All applications accessing electric vehicle functionality
 * are to implement this interface.
 */
public interface ElectricVehicleApplication extends VehicleApplication, Application {

    /**
     * This method is called whenever {@link BatteryData} of the vehicle unit has changed.
     * (this requires the BatteryAmbassador to be activated and configured properly)
     *
     * @param previousBatteryData the {@link BatteryData} before the update
     * @param updatedBatteryData  the {@link BatteryData} after the update
     */
    void onBatteryDataUpdated(@Nullable BatteryData previousBatteryData, @Nonnull BatteryData updatedBatteryData);

    /**
     * This method is called after a charging request has been rejected by the battery ambassador.
     *
     * @param vehicleChargingDenial The interaction containing further information about the rejected charging request
     */
    void onVehicleChargingDenial(final VehicleChargingDenial vehicleChargingDenial);

    void onChargingStationDiscoveryResponse(ChargingStationDiscoveryResponse response);
}
