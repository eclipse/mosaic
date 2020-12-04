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
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveVehicleState;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.Locale;

/**
 * This class represents the traci command which allows to get the Id of the vehicle type.
 */
public class VehicleGetVehicleTypeId
        extends AbstractTraciCommand<String>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleGetVehicleTypeId {

    /**
     * Creates a new {@link VehicleGetVehicleTypeId} traci command, which
     * will return the vehicle type id for the given vehicle once executed.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/VehicleType_Value_Retrieval.html">VehicleType Value Retrieval</a>
     */
    public VehicleGetVehicleTypeId() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveVehicleState.COMMAND)
                .variable(CommandRetrieveVehicleState.VAR_TYPE_ID)
                .writeVehicleIdParam();

        read()
                .skipBytes(2)
                .skipString()
                .readStringWithType();
    }

    /**
     * This method executes the command with the given arguments in order to get the Id of a vehicle route.
     *
     * @param con     Connection to Traci.
     * @param vehicle Id of the vehicle.
     * @return Id of the vehicle type.
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public String execute(Bridge con, String vehicle) throws CommandException, InternalFederateException {
        return executeAndReturn(con, vehicle).orElseThrow(
                () -> new CommandException(
                        String.format(Locale.ENGLISH, "Couldn't get TypeId for Vehicle: %s.", vehicle),
                        new Status((byte) Status.STATUS_ERR, "")
                )
        );
    }

    @Override
    protected String constructResult(Status status, Object... objects) {
        return (String) objects[0];
    }
}
