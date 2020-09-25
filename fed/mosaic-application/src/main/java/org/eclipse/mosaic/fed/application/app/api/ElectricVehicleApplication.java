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

import org.eclipse.mosaic.interactions.electricity.ChargingDenialResponse;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleBatteryState;

/**
 * All applications accessing electric vehicle functionality
 * are to implement this interface.
 */
public interface ElectricVehicleApplication extends VehicleApplication, Application {

    /**
     * This method is called whenever {@link VehicleBatteryState} of the vehicle unit has changed.
     * (this requires the BatteryAmbassador to be activated and configured properly)
     *
     * @param previousState the {@link VehicleBatteryState} before the update
     * @param updatedState  the {@link VehicleBatteryState} after the update
     */
    void onBatteryStateUpdated(VehicleBatteryState previousState, VehicleBatteryState updatedState);

    /**
     * This method is called after a charging request has been rejected by the battery ambassador.
     *
     * @param chargingDenialResponse The interaction containing further information about the rejected charging request
     */
    void onChargingRequestRejected(final ChargingDenialResponse chargingDenialResponse);
}
