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

/**
 * This class represents the SUMO command which allows to set the new speed value for the vehicle for a specific time.
 */
public class VehicleSetSlowDown
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetSlowDown {

    /**
     * Creates a new {@link VehicleSetSlowDown} object.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    public VehicleSetSlowDown() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangeVehicleValue.COMMAND)
                .variable(CommandChangeVehicleValue.VAR_SLOW_DOWN)
                .writeVehicleIdParam()
                .writeByte(TraciDatatypes.COMPOUND)
                .writeInt(2)
                .writeDoubleParamWithType()
                .writeDoubleParamWithType();
    }

    /**
     * This method executes the command with the given arguments in order to slow down the vehicle to the new speed.
     *
     * @param bridge      Connection to SUMO.
     * @param vehicleId   The Id of the vehicle to change the route.
     * @param newSpeedMps The new speed of the vehicle.
     * @param timeInMs    The duration for the new speed.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String vehicleId, double newSpeedMps, int timeInMs) throws CommandException, InternalFederateException {
        super.execute(bridge, vehicleId, newSpeedMps, timeInMs / 1000d);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
