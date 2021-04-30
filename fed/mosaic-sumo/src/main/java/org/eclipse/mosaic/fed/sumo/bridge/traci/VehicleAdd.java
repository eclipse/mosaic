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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandChangeVehicleValue;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.rti.api.InternalFederateException;

public class VehicleAdd
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleAdd {

    /**
     * Creates a {@link VehicleAdd} command.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleAdd() {
        super(TraciVersion.HIGHEST);

        write()
                .command(CommandChangeVehicleValue.COMMAND)
                .variable(CommandChangeVehicleValue.VAR_ADD)
                .writeVehicleIdParam() // vehicle id
                .writeByte(TraciDatatypes.COMPOUND) // compound
                .writeInt(14) // number of elements (=14)
                .writeStringParamWithType() // route id
                .writeStringParamWithType() // vehicle type id
                .writeStringWithType("now") // depart time
                .writeStringParamWithType() // depart lane
                .writeStringParamWithType() // depart position
                .writeStringParamWithType() // depart speed
                .writeStringWithType("current") // arrival lane
                .writeStringWithType("max") // arrival position
                .writeStringWithType("current") // arrival speed
                .writeStringWithType("") // from taz
                .writeStringWithType("") // to taz
                .writeStringWithType("") // line (for public transport)
                .writeIntWithType(0) // person capacity
                .writeIntWithType(0); // person number

    }

    /**
     * This method executes the command with the given arguments in order to add a vehicle with its specific properties.
     * This overload of {@link #execute} uses some default values
     *
     * @param con            Connection to TraCI.
     * @param vehicleId      Id of the vehicle.
     * @param routeId        Id of the route.
     * @param vehicleType    Type of the vehicle.
     * @param departLane     Lane of the departure. Possible values: [int] or random", "free", "allowed", "best", "first"
     * @param departPosition Position of the departure. Possible values: [int] or "random", "free", "base", "last", "random free"
     * @param departSpeed    Speed at departure. Possible Values: [double] or "max", "random"
     * @throws CommandException     If the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException If some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge con, String vehicleId, String routeId, String vehicleType,
                        String departLane, String departPosition, String departSpeed)
            throws CommandException, InternalFederateException {

        super.execute(con,
                vehicleId,
                routeId,
                vehicleType,
                departLane,
                departPosition,
                departSpeed
        );
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
