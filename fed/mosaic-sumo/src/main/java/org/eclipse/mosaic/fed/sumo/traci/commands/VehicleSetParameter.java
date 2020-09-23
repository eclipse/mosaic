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
 */

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandChangeVehicleValue;
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.Locale;

/**
 * This class writes a parameter value for specific a vehicle.
 */
public class VehicleSetParameter extends AbstractTraciCommand<Void> {

    /**
     * Creates a new {@link VehicleSetParameter} object.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleSetParameter() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangeVehicleValue.COMMAND)
                .variable(CommandChangeVehicleValue.VAR_PARAMETER)
                .writeVehicleIdParam()
                .writeByte(TraciDatatypes.COMPOUND)
                .writeInt(2)
                .writeStringParamWithType() // parameter name
                .writeStringParamWithType(); // parameter value
    }

    /**
     * This method executes the command with the given arguments in order to set the
     * value (as double) of a specific parameter of the given vehicle.
     *
     * @param traciCon             Connection to Traci.
     * @param vehicleId            Id of the vehicle.
     * @param parameterName        the name of the parameter to set
     * @param parameterDoubleValue the value of the parameter to set
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(TraciConnection traciCon, String vehicleId, String parameterName, double parameterDoubleValue) throws TraciCommandException, InternalFederateException {
        execute(traciCon, vehicleId, parameterName, String.format(Locale.ENGLISH, "%.5f", parameterDoubleValue));
    }

    /**
     * This method executes the command with the given arguments in order to set the
     * value (as String) of a specific parameter of the given vehicle.
     *
     * @param traciCon       Connection to Traci.
     * @param vehicleId      Id of the vehicle.
     * @param parameterName  the name of the parameter to set
     * @param parameterValue the value of the parameter to set
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(TraciConnection traciCon, String vehicleId, String parameterName, String parameterValue) throws TraciCommandException, InternalFederateException {
        super.execute(traciCon, vehicleId, parameterName, parameterValue);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
