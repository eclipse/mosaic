/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveVehicleState;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.sumo.libsumo.IntVector;
import org.eclipse.sumo.libsumo.Vehicle;

/**
 * This class represents the SUMO command which allows to subscribe the vehicle to the application.
 * Several options for vehicle subscription are implemented in this class.
 */
public class VehicleSubscribeSurroundingVehicle
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSubscribeSurroundingVehicle {

    public void execute(Bridge bridge, String vehicleId, long startTime, long endTime, double range) throws CommandException, InternalFederateException {
        Vehicle.subscribeContext(
                Bridge.VEHICLE_ID_TRANSFORMER.toExternalId(vehicleId),
                CommandRetrieveVehicleState.COMMAND,
                range,
                new IntVector(new int[]{CommandRetrieveVehicleState.VAR_SPEED.var, CommandRetrieveVehicleState.VAR_POSITION_3D.var, CommandRetrieveVehicleState.VAR_ANGLE.var}),
                ((double) startTime) / TIME.SECOND,
                ((double) endTime) / TIME.SECOND
        );
    }
}
