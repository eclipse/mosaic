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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandChangeVehicleValue;
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the traci command which allows to set the vehicle move to explicit position.
 */
public class VehicleSetMoveToXY extends AbstractTraciCommand<Void> {

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
     * Enum class that represents the different mode.
     */
    public enum Mode {
        SWITCH_ROUTE(0),
        KEEP_ROUTE(1),
        EXACT_POSITION(2);

        private final int mode;

        Mode(int mode) {
            this.mode = mode;
        }
    }

    /**
     * This method executes the command with the given arguments in order to set the vehicle move to a position.
     *
     * @param traciCon      Connection to Traci.
     * @param vehicleId     The Id of the vehicle.
     * @param edgeId        The Id of the edge.
     * @param laneIndex     The index of the lane.
     * @param position      The cartesian position to move it.
     * @param angle         Angle of the move.
     * @param keepRouteMode Move mode.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(TraciConnection traciCon, String vehicleId, String edgeId, int laneIndex, CartesianPoint position, double angle, Mode keepRouteMode) throws TraciCommandException, InternalFederateException {
        super.execute(traciCon, vehicleId, edgeId, laneIndex, position.getX(), position.getY(), angle, keepRouteMode.mode);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
