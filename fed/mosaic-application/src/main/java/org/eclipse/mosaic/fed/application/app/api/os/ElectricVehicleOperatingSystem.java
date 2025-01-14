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

package org.eclipse.mosaic.fed.application.app.api.os;

import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.objects.vehicle.BatteryData;

import javax.annotation.Nullable;

/**
 * This interface extends the basic {@link OperatingSystem} and
 * is implemented by the {@link org.eclipse.mosaic.fed.application.ambassador.simulation.AbstractSimulationUnit}
 * {@link org.eclipse.mosaic.fed.application.ambassador.simulation.ElectricVehicleUnit}.
 */
public interface ElectricVehicleOperatingSystem extends VehicleOperatingSystem {

    /**
     * Returns the electric vehicle information.
     *
     * @return the electric vehicle information.
     */
    @Nullable
    BatteryData getBatteryState();

    /**
     * Sends a request to start charging the battery of the vehicle.
     *
     * @param chargingStationId The id of the charging station to send the request to.
     */
    void sendChargingStartRequest(String chargingStationId);

    /**
     * Sends a request to stop charging the battery of the vehicle.
     */
    void sendChargingStopRequest();

    /**
     * Sends a request to locate all charging stations in the provided area.
     *
     * @param searchArea The area where the charging stations are search
     */
    void sendChargingStationDiscoveryRequest(GeoCircle searchArea);
}
