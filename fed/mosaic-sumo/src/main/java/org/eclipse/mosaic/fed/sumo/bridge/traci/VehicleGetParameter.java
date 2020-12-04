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
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveVehicleState;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class reads a parameter value for specific a vehicle.
 */
public class VehicleGetParameter
        extends AbstractTraciCommand<String>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleGetParameter {

    /**
     * Creates a new {@link VehicleGetParameter} traci command.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/VehicleType_Value_Retrieval.html">VehicleType Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleGetParameter() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveVehicleState.COMMAND)
                .variable(CommandChangeVehicleValue.VAR_PARAMETER)
                .writeVehicleIdParam()
                .writeStringParamWithType(); // parameter name

        read()
                .skipBytes(2)
                .skipString()
                .readStringWithType();
    }

    /**
     * This method executes the command with the given arguments in order to get the value of a specific parameter of the given vehicle.
     *
     * @param traciCon      Connection to Traci.
     * @param vehicleId     Id of the vehicle.
     * @param parameterName the name of the parameter to read
     * @return the value of the parameter, or {@code null} if not present
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public String execute(Bridge traciCon, String vehicleId, String parameterName) throws CommandException, InternalFederateException {
        return super.executeAndReturn(traciCon, vehicleId, parameterName).orElse(null);
    }

    @Override
    protected String constructResult(Status status, Object... objects) {
        return (String) objects[0];
    }
}
