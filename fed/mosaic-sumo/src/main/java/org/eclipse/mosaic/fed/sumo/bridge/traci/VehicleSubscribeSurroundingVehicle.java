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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveVehicleState;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandVariableSubscriptions;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.VehicleContextSubscriptionTraciReader;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the SUMO command which allows to subscribe the vehicle to the application.
 * Several options for vehicle subscription are implemented in this class.
 */
public class VehicleSubscribeSurroundingVehicle
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSubscribeSurroundingVehicle {

    @SuppressWarnings("WeakerAccess")
    public VehicleSubscribeSurroundingVehicle() {
        super(TraciVersion.API_20);

        write()
                .command(CommandVariableSubscriptions.COMMAND_SUBSCRIBE_CONTEXT_VEHICLE_VALUES)
                .writeDoubleParam() // start time
                .writeDoubleParam() // end time
                .writeVehicleIdParam() // vehicle id
                .writeByte(CommandRetrieveVehicleState.COMMAND) // subscribe for vehicle values
                .writeDoubleParam() // range parameter
                .writeByte(3)
                .writeByte(CommandRetrieveVehicleState.VAR_SPEED.var)
                .writeByte(CommandRetrieveVehicleState.VAR_POSITION_3D.var)
                .writeByte(CommandRetrieveVehicleState.VAR_ANGLE.var);

        read()
                .expectByte(CommandVariableSubscriptions.RESPONSE_SUBSCRIBE_CONTEXT_VEHICLE_VALUES)
                .readComplex(new VehicleContextSubscriptionTraciReader());
    }

    /**
     * This method executes the command with the given arguments in order to subscribe the vehicle to the application.
     *
     * @param bridge    Connection to SUMO.
     * @param vehicleId The Id of the Vehicle.
     * @param startTime The time to subscribe the vehicle.
     * @param endTime   The end time of the subscription of the vehicle in the application.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String vehicleId, long startTime, long endTime, double range) throws CommandException, InternalFederateException {
        super.execute(bridge, ((double) startTime) / TIME.SECOND, ((double) endTime) / TIME.SECOND, vehicleId, range);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
