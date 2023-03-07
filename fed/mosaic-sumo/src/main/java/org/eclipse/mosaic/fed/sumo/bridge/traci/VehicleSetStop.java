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
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the SUMO command which allows to set the stop type for the vehicle
 * e.g. parking, chargingStation.
 */
public class VehicleSetStop
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetStop {

    /**
     * Creates a new {@link VehicleSetStop} object.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    public VehicleSetStop() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangeVehicleValue.COMMAND)
                .variable(CommandChangeVehicleValue.VAR_STOP)
                .writeVehicleIdParam()
                .writeByte(TraciDatatypes.COMPOUND)
                .writeInt(5)
                .writeStringParamWithType() // edge id
                .writeDoubleParamWithType() // position
                .writeByteParamWithType() // lane index
                .writeDoubleParamWithType() // duration
                .writeByteParamWithType(); // stop flag
    }

    /**
     * This method executes the command with the given arguments in order to set the stop mode for the vehicle.
     *
     * @param bridge    Connection to SUMO.
     * @param vehicleId The Id of the vehicle to change the route.
     * @param edgeId    The Id of the edge on which to stop.
     * @param position  The position of the stop.
     * @param laneIndex The index of the lane on which to stop.
     * @param duration  The duration for stop in [ns].
     * @param stopFlag  The flag indicating the type of the stop.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String vehicleId, String edgeId, double position, int laneIndex, long duration, int stopFlag) throws CommandException, InternalFederateException {
        super.execute(bridge, vehicleId, edgeId, position, laneIndex, duration / (double) TIME.SECOND, stopFlag);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
