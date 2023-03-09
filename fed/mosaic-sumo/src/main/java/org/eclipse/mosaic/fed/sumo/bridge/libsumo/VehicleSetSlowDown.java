/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.rti.TIME;

import org.eclipse.sumo.libsumo.Vehicle;

public class VehicleSetSlowDown implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetSlowDown {

    /**
     * This method executes the command with the given arguments in order to slow down the vehicle to the new speed.
     *
     * @param bridge    Connection to SUMO.
     * @param vehicleId The Id of the vehicle to change the route.
     * @param newSpeed  The new speed of the vehicle. [m/s]
     * @param time      The duration for the new speed. [ns]
     */
    public void execute(Bridge bridge, String vehicleId, double newSpeed, long time) {
        Vehicle.slowDown(Bridge.VEHICLE_ID_TRANSFORMER.toExternalId(vehicleId), newSpeed, (double) time / TIME.SECOND);
    }

}
