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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.sumo.libsumo.Vehicle;

import java.util.Locale;

/**
 * This class writes a parameter value for specific a vehicle.
 */
public class VehicleSetParameter implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetParameter {
    /**
     * This method executes the command with the given arguments in order to set the
     * value (as double) of a specific parameter of the given vehicle.
     *
     * @param bridge             Connection to Traci.
     * @param vehicleId            Id of the vehicle.
     * @param parameterName        the name of the parameter to set
     * @param parameterDoubleValue the value of the parameter to set
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String vehicleId, String parameterName, double parameterDoubleValue) throws CommandException, InternalFederateException {
        Vehicle.setParameter(Bridge.VEHICLE_ID_TRANSFORMER.toExternalId(vehicleId), parameterName, String.format(Locale.ENGLISH, "%.4f", parameterDoubleValue));
    }

    /**
     * This method executes the command with the given arguments in order to set the
     * value (as String) of a specific parameter of the given vehicle.
     *
     * @param bridge       Connection to Traci.
     * @param vehicleId      Id of the vehicle.
     * @param parameterName  the name of the parameter to set
     * @param parameterValue the value of the parameter to set
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String vehicleId, String parameterName, String parameterValue) throws CommandException, InternalFederateException {
        Vehicle.setParameter(Bridge.VEHICLE_ID_TRANSFORMER.toExternalId(vehicleId), parameterName, parameterValue);
    }
}
