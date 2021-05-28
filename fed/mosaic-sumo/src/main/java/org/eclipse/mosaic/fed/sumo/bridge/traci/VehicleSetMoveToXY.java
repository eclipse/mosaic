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
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the SUMO command which allows to set the vehicle move to explicit position.
 */
public class VehicleSetMoveToXY
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetMoveToXY {

    /**
     * Creates a new {@link VehicleSetMoveToXY} object.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleSetMoveToXY() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangeVehicleValue.COMMAND)
                .variable(CommandChangeVehicleValue.VAR_MOVE_TO_XY)
                .writeVehicleIdParam()
                .writeByte(TraciDatatypes.COMPOUND)
                .writeInt(6)
                .writeStringParamWithType() // edge id (to resolve ambiguities, may be arbitrary)
                .writeIntParamWithType() // lane index (to resolve ambiguities, may be arbitrary)
                .writeDoubleParamWithType() // x coordinate
                .writeDoubleParamWithType() // y coordinate
                .writeDoubleParamWithType() // angle
                .writeByteParamWithType(); //  0=keep on route, route switch is possible, 1=keep on route, 2=on exact x,y position
    }

    /**
     * This method executes the command with the given arguments in order to set the vehicle move to a position.
     *
     * @param bridge        Connection to SUMO.
     * @param vehicleId     The Id of the vehicle.
     * @param edgeId        The Id of the edge.
     * @param laneIndex     The index of the lane.
     * @param position      The cartesian position to move it.
     * @param angle         Angle of the move.
     * @param keepRouteMode Move mode.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String vehicleId, String edgeId, int laneIndex, CartesianPoint position, double angle, Mode keepRouteMode) throws CommandException, InternalFederateException {
        super.execute(bridge, vehicleId, edgeId, laneIndex, position.getX(), position.getY(), angle, keepRouteMode.mode);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
